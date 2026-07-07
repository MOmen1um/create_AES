package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.util.Mth;

public class NonModularEnginesBlockEntity extends GeneratingKineticBlockEntity {

    // 1. Параметры конкретного экземпляра (задаются при установке блока)
    public String engineType;      // "I", "V", "W", "R"
    public String engineMaterial;  // "cast_iron", "aluminum", "titanium"

    // 2. Оригинальные переменные из твоего эталона V8 (проверь названия букву в букву!)
    public final FluidTank FuelTank = new FluidTank(4000);
    private int burnTimeRemaining = 0;
    private float currentSpeed = 0;
    private float lastSentSpeed = -1f;
    public float engineQuality = 1.0f;
    public float secretEfficiency = 1.0f;

    public float engineTemperature = 20.0f;
    public boolean isTurboCharged = false;
    private int accelerationTicks = 0;
    private int overheatMeltingTimer = 0;
    public float maxMeltingTemp;
    public float targetSliderSpeed = 16.0f;
    public int radiatorType = 0;

    // Главный универсальный конструктор
    public NonModularEnginesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String engineType, String engineMaterial) {
        super(type, pos, state);
        this.engineType = engineType.toUpperCase();
        this.engineMaterial = engineMaterial.toLowerCase();

        // Переносим расчет критической температуры из логики эталона, но делаем его динамическим
        this.maxMeltingTemp = switch (this.engineMaterial) {
            case "aluminum" -> 660f;
            case "titanium" -> 1668f;
            default -> 1200f;
        };
    }
    private void triggerOverheat() {
        if (level != null && !level.isClientSide) {
            // Удаляем блок двигателя, чтобы он не остался после взрыва
            level.destroyBlock(worldPosition, false);
            // Устраиваем красивый бабах мощностью 4 блока (как динамит), который поджигает окружение
            level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                    4.0F, true, net.minecraft.world.level.Level.ExplosionInteraction.TNT);
        }
    }
    public float getCoolingEfficiency() {
        return switch (this.radiatorType) {
            case 1 -> 0.15f; // Медный
            case 2 -> 0.30f; // Стальной
            case 3 -> 0.50f; // Латунный
            case 4 -> 1.00f; // Ультимативный
            default -> 0.05f; // Без радиатора
        };
    }
    private float getAmbientTemperature() {
        if (level != null) {
            // Берем базовую температуру биома в этой точке мира и переводим в условные градусы
            return level.getBiome(worldPosition).value().getBaseTemperature() * 25.0f;
        }
        return 20.0f; // Дефолт, если мира еще нет
    }
    public float getSafeEngineSpeed() {
        float baseSpeed = switch (this.engineMaterial) {
            case "cast_iron" -> 1024f;
            case "aluminum" -> 4096f;
            case "titanium" -> 8192f;
            default -> 1024f;
        };
        // Если стоит турбина — лимит оборотов удваивается!
        return isTurboCharged ? baseSpeed * 2.0f : baseSpeed;
    }
    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getSpeed());
        // Если двигатель заглушен или топливо кончилось — он не держит нагрузку сети
        if (speed <= 0 || burnTimeRemaining <= 0) return 0;

        // Л.С. на один поршень при базовых 64 RPM
        float powerPerPiston = switch (this.engineMaterial) {
            case "cast_iron" -> 30f * 64f;  // 30 л.с.
            case "aluminum" -> 60f * 64f;   // 60 л.с.
            case "titanium" -> 100f * 64f;  // 100 л.с.
            default -> 30f * 64f;
        };

        // Считаем поршни по твоей архитектуре
        int pistonCount = switch (this.engineType) {
            case "I" -> 4;
            case "V" -> 8;
            case "W" -> 12;
            case "R" -> 32;
            default -> 4;
        };

        float totalPower = powerPerPiston * pistonCount;

        // Множитель турбины х2
        if (isTurboCharged) {
            totalPower *= 2.0f;
        }

        // Масштабируем итоговый Stress Capacity от текущей скорости
        return totalPower * (speed / 64.0f);
    }
    @Override
    public float getGeneratedSpeed() {
        // Если двигатель работает и в нем горит топливо
        if (this.burnTimeRemaining > 0) {
            // Возвращаем текущую скорость (которая разгоняется в tick())
            return this.currentSpeed;
        }
        return 0; // Если заглох — скорость ноль
    }
    @Override
    public void tick() {
        super.tick();

        // Работаем только на стороне логики (сервера)
        if (level == null || level.isClientSide) return;

        // 1. ЛОГИКА СЖИГАНИЯ ТОПЛИВА
        if (this.burnTimeRemaining > 0) {
            this.burnTimeRemaining--;
            // Каждую секунду синхронизируем бак с клиентом, чтобы шкала не дергалась
            if (level.getGameTime() % 20 == 0) {
                this.setChanged();
                this.notifyUpdate();
            }
        }

        // 2. СИСТЕМЫ РАЗГОНА И СКОРОСТИ (Твой эталонный плавный разгон!)
        if (this.burnTimeRemaining > 0 && this.targetSliderSpeed > 0) {
            // Если текущая скорость меньше выставленной на ползунке — плавно разгоняем
            if (this.currentSpeed < this.targetSliderSpeed) {
                this.accelerationTicks++;
                // Каждые 10 тиков (полсекунды) прибавляем обороты
                if (this.accelerationTicks >= 10) {
                    this.currentSpeed = Math.min(this.targetSliderSpeed, this.currentSpeed + 8.0f);
                    this.accelerationTicks = 0;
                    // Пинаем сеть Create обновить крутящий момент вала!
                    this.updateGeneratedRotation();
                }
            } else if (this.currentSpeed > this.targetSliderSpeed) {
                // Если игрок скрутил ползунок вниз — плавно сбрасываем обороты
                this.currentSpeed = Math.max(this.targetSliderSpeed, this.currentSpeed - 8.0f);
                this.updateGeneratedRotation();
            }
        } else {
            // Топливо кончилось или ползунок в нуле — плавно глушим мотор до полной остановки
            if (this.currentSpeed > 0) {
                this.currentSpeed = Math.max(0, this.currentSpeed - 16.0f);
                this.updateGeneratedRotation();
            }
            this.accelerationTicks = 0;
        }

        // 3. ТЕРМОДИНАМИКА И КАЛИБРОВКА НАГРЕВА
        float ambientTemp = getAmbientTemperature(); // Твой рабочий метод температуры биома!
        float maxSpeed = getSafeEngineSpeed();

        if (this.currentSpeed > 0) {
            // Динамический коэффициент нагрева под любой металл: на макс. оборотах будет ~700°C
            float heatCoefficient = 700.0f / maxSpeed;
            float targetTemperature = ambientTemp + (this.currentSpeed * heatCoefficient);

            // Плавное изменение стрелки термометра с учетом твоих радиаторов
            if (this.engineTemperature < targetTemperature) {
                // Радиатор замедляет нагрев двигателя
                this.engineTemperature += 0.2f * (1.0f - getCoolingEfficiency());
            } else if (this.engineTemperature > targetTemperature) {
                // Радиатор помогает остывать быстрее при сбросе газа
                this.engineTemperature -= 0.1f * (1.0f + getCoolingEfficiency());
            }
        } else {
            // Двигатель полностью заглушен — плавно остывает до температуры биома
            if (this.engineTemperature > ambientTemp) {
                this.engineTemperature = Math.max(ambientTemp, this.engineTemperature - (0.5f * (1.0f + getCoolingEfficiency())));
            }
        }

        // 4. ТАЙМЕР ПЛАВЛЕНИЯ И ПЕРЕГРЕВА (overheatMeltingTimer из твоего V8)
        if (this.engineTemperature >= this.maxMeltingTemp) {
            this.overheatMeltingTimer++;
            if (this.overheatMeltingTimer >= 100) { // 5 секунд критического перегрева
                this.triggerOverheat();
            }
        } else {
            if (this.overheatMeltingTimer > 0) {
                this.overheatMeltingTimer--;
            }
        }

        // 5. ОТПРАВКА ОБНОВЛЕНИЙ НА КЛИЕНТ ДЛЯ АНИМАЦИИ ПОРШНЕЙ
        if (this.currentSpeed != this.lastSentSpeed) {
            this.lastSentSpeed = this.currentSpeed;
            this.setChanged();
            this.notifyUpdate();
        }
    }
    @Override
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        // 1. Аварийная блокировка из твоего эталона, если скорость в конфиге Create занижена
        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            tooltip.add(net.minecraft.network.chat.Component.literal("§c⚠ АВАРИЙНАЯ БЛОКИРОВКА!"));
            tooltip.add(net.minecraft.network.chat.Component.literal("§7Повысьте 'maxRotationSpeed' в конфиге Create до 32768!"));
            return true;
        }

        // Вызываем базовые строки Create (Stress/SU)
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(net.minecraft.network.chat.Component.literal("§8--------------------------------"));

        // 2. Спецификация ДВС
        String readableType = switch (this.engineType != null ? this.engineType : "I") {
            case "I" -> "Inline-4";
            case "V" -> "V8";
            case "W" -> "W12";
            case "R" -> "Radial R-32 Monster";
            default -> "Unknown";
        };
        tooltip.add(net.minecraft.network.chat.Component.literal("§6Спецификация ДВС: §7" + readableType));

        // 3. Материал блока и динамический цвет текста (как в твоем эталоне)
        String matColor = this.engineMaterial.equals("titanium") ? "§b" : (this.engineMaterial.equals("aluminum") ? "§7" : "§8");
        tooltip.add(net.minecraft.network.chat.Component.literal("§eМатериал блока: " + matColor + this.engineMaterial.toUpperCase()));

        // 4. Турбонаддув
        String turboText = isTurboCharged ? "§aУСТАНОВЛЕН" : "§cОТСУТСТВУЕТ";
        tooltip.add(net.minecraft.network.chat.Component.literal("§eТурбонаддув: " + turboText));

        // 5. Телеметрия температур
        tooltip.add(net.minecraft.network.chat.Component.literal("§7Телеметрия температур:"));

        // Меняем цвет цифр: если до плавления осталось меньше 200 градусов — подсвечиваем красным
        String tempColor = (this.engineTemperature > (this.maxMeltingTemp - 200f)) ? "§c" : "§a";
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 Температура ядра: " + tempColor + String.format("%.1f", this.engineTemperature) + "°C / " + this.maxMeltingTemp + "°C"));
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 Безопасная зона: ядро < " + String.format("%.0f", getSafeEngineSpeed()) + " RPM"));

        return true;
    }
    // Предоставляем бак для NeoForge BlockCapability системы труб
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidTank() {
        return this.FuelTank;
    }
}