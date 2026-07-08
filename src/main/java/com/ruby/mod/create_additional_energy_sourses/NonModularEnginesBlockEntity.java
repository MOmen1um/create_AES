package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
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

    public NonModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        // Сами жестко передаем тип из ModBlocks прямо в супер-конструктор!
        super(ModBlocks.NON_MODULAR_ENGINE_ENTITY.get(), pos, state);

        // 1. АВТОМАТИЧЕСКИ ОПРЕДЕЛЯЕМ МАТЕРИАЛ ПО ИМЕНИ БЛОКА
        String blockId = state.getBlock().toString().toLowerCase();
        if (blockId.contains("titanium")) {
            this.engineMaterial = "titanium";
            this.maxMeltingTemp = 1668f;
        } else if (blockId.contains("aluminum")) {
            this.engineMaterial = "aluminum";
            this.maxMeltingTemp = 660f;
        } else {
            this.engineMaterial = "cast_iron";
            this.maxMeltingTemp = 1200f;
        }

        // 2. АВТОМАТИЧЕСКИ ОПРЕДЕЛЯЕМ АРХИТЕКТУРУ ПО ИМЕНИ БЛОКА
        if (blockId.contains("v8")) {
            this.engineType = "V";
        } else if (blockId.contains("w16")) {
            this.engineType = "W";
        } else if (blockId.contains("r32")) {
            this.engineType = "R";
        } else {
            this.engineType = "I"; // Дефолт для i4
        }
    }

    @Override
    public void addBehaviours(java.util.List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        // Ограничиваем физическую шкалу ползунка, чтобы полоса была маленькой и аккуратной
        int sliderMaxSteps = 512;

        // Ручной трансформатор положения окошка
        com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform customTransform =
                new com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform() {
                    @Override
                    public net.minecraft.world.phys.Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                        // Окошко настроек ровно по центру верхней грани блока мотора
                        return new net.minecraft.world.phys.Vec3(0.5, 1.01, 0.5);
                    }

                    @Override
                    public void rotate(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, com.mojang.blaze3d.vertex.PoseStack ms) {
                        // Поворачиваем плашку, чтобы она смотрела вверх на игрока
                        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
                    }
                };

        // Создаем ползунок Create
        com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour slider =
                new com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour(
                        net.minecraft.network.chat.Component.literal("Обороты двигателя (RPM)"),
                        this,
                        customTransform
                );

        // Задаем внутренние рамки шкалы (0-512)
        slider.between(0, sliderMaxSteps);

        // Коллбек: считываем значение (0-512) и умножаем на шаг 16, получая до 32768 RPM!
        slider.withCallback(value -> {
            this.targetSliderSpeed = (float) value;
            this.setChanged();
            this.updateGeneratedRotation();
            if (level != null && level.isClientSide) {
                this.notifyUpdate();
            }
        });

        behaviours.add(slider);
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
        // КРИТИЧЕСКИ ВАЖНО: Считаем мощность ОТ ТАХОМЕТРА ПОРШНЕЙ, а не от ползунка!
        // Если поршни стоят (currentSpeed == 0) — генератор выдаст ровно 0 SU,
        // и космические миллионы на экране мгновенно исчезнут!
        float speed = Math.abs(this.currentSpeed);

        // ВРЕМЕННО ДЛЯ ТЕСТА: Оставляем топливо активным
        int testBurnTime = 100;
        if (speed <= 0 || testBurnTime <= 0) return 0;

        // Дальше идет весь твой остальной рабочий switch-case по поршням и металлам...
        float powerPerPiston = switch (this.engineMaterial) {
            case "cast_iron" -> 30f * 64f;
            case "aluminum" -> 60f * 64f;
            case "titanium" -> 100f * 64f;
            default -> 30f * 64f;
        };

        int pistonCount = switch (this.engineType) {
            case "I" -> 4;
            case "V" -> 8;
            case "W" -> 12;
            case "R" -> 32;
            default -> 4;
        };

        float totalPower = powerPerPiston * pistonCount;
        if (isTurboCharged) totalPower *= 2.0f;

        return totalPower * (speed / 64.0f);
    }
    @Override
    public void tick() {
        // ТЕСТОВАЯ ЗАПРАВКА: всегда заправлен!
        this.burnTimeRemaining = 100;

        super.tick();

        // Все расчеты ведем на сервере
        if (level == null || level.isClientSide) return;

        // Расход топлива
        if (this.burnTimeRemaining > 0) {
            this.burnTimeRemaining--;
        }

        // СИСТЕМА ПЛАВНОГО РАЗГОНА ИЗ ТВОЕГО ЭТАЛОНА V8
        if (this.burnTimeRemaining > 0 && this.targetSliderSpeed > 0) {

            // ⚡ ЭЛЕКТРОСТАРТЕР: если мотор должен крутиться, но намертво заклинен в нуле
            if (this.currentSpeed == 0) {
                this.currentSpeed = 0.1f; // Даем микро-толчок, чтобы Create проснулся!
                this.updateGeneratedRotation();
            }

            // Плавный разгон поршней
            if (this.currentSpeed < this.targetSliderSpeed) {
                this.accelerationTicks++;
                if (this.accelerationTicks >= 10) { // Разгон каждые 10 тиков (полсекунды)
                    // Прибавляем по 8 RPM, но не выше ползунка
                    this.currentSpeed = Math.min(this.targetSliderSpeed, this.currentSpeed + 8.0f);
                    this.accelerationTicks = 0;

                    // Сами пинаем сеть Create на каждом шаге разгона, как в V8!
                    this.updateGeneratedRotation();
                }
            } else if (this.currentSpeed > this.targetSliderSpeed) {
                // Плавный сброс оборотов
                this.currentSpeed = Math.max(this.targetSliderSpeed, this.currentSpeed - 8.0f);
                this.updateGeneratedRotation();
            }
        } else {
            // Топливо кончилось или ползунок в нуле — плавно глушим мотор по инерции
            if (this.currentSpeed > 0) {
                this.currentSpeed = Math.max(0, this.currentSpeed - 16.0f);
                this.updateGeneratedRotation();
            }
            this.accelerationTicks = 0;
        }

        // ТЕРМОДИНАМИКА НА ОСНОВЕ РЕАЛЬНЫХ ОБОРОТОВ (currentSpeed)
        float ambientTemp = getAmbientTemperature();
        float maxSpeed = getSafeEngineSpeed();

        if (this.currentSpeed > 0) {
            float heatCoefficient = 700.0f / maxSpeed;
            float targetTemperature = ambientTemp + (this.currentSpeed * heatCoefficient);

            if (this.engineTemperature < targetTemperature) {
                this.engineTemperature += 0.2f * (1.0f - getCoolingEfficiency());
            } else if (this.engineTemperature > targetTemperature) {
                this.engineTemperature -= 0.1f * (1.0f + getCoolingEfficiency());
            }
        } else {
            if (this.engineTemperature > ambientTemp) {
                this.engineTemperature = Math.max(ambientTemp, this.engineTemperature - (0.5f * (1.0f + getCoolingEfficiency())));
            }
        }

        // ТАЙМЕР ПЛАВЛЕНИЯ И ВЗРЫВА
        if (this.engineTemperature >= this.maxMeltingTemp) {
            this.overheatMeltingTimer++;
            if (this.overheatMeltingTimer >= 100) {
                this.triggerOverheat();
            }
        } else {
            if (this.overheatMeltingTimer > 0) this.overheatMeltingTimer--;
        }

        // ЖЁСТКАЯ ОТПРАВКА ДАННЫХ В ОЧКИ ИНЖЕНЕРА (sendData из 1.21.1)
        if (level.getGameTime() % 10 == 0) {
            this.setChanged();
            this.sendData();
        }
    }
    @Override
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        // Проверка аварийной блокировки Create из твоего эталона
        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            tooltip.add(net.minecraft.network.chat.Component.literal("§c⚠ АВАРИЙНАЯ БЛОКИРОВКА!"));
            tooltip.add(net.minecraft.network.chat.Component.literal("§7Повысьте 'maxRotationSpeed' в конфиге Create до 32768!"));
            return true;
        }

        // Вызываем базовые строки Create (Stress/SU)
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(net.minecraft.network.chat.Component.literal("§8--------------------------------"));

        // 1. Архитектура и Конфигурация ДВС
        String readableType = switch (this.engineType != null ? this.engineType : "I") {
            case "I" -> "Inline-4 (Рядный)";
            case "V" -> "V8 Engine (V-образный)";
            case "W" -> "W12 Engine (W-образный)";
            case "R" -> "Radial R-32 (Авиационный монстр)";
            default -> "Стандартный ДВС";
        };
        tooltip.add(net.minecraft.network.chat.Component.literal("§6⚙ Спецификация: §7" + readableType));

        // 2. Материал Блока
        String matColor = this.engineMaterial.equals("titanium") ? "§b" : (this.engineMaterial.equals("aluminum") ? "§7" : "§8");
        tooltip.add(net.minecraft.network.chat.Component.literal("§e📦 Материал: " + matColor + this.engineMaterial.toUpperCase()));

        // 3. Турбонаддув и Нагнетатели
        String turboText = isTurboCharged ? "§a✔ АКТИВИРОВАН (x2 Мощность)" : "§c✖ ОТСУТСТВУЕТ";
        tooltip.add(net.minecraft.network.chat.Component.literal("§d🚀 Турбонаддув: " + turboText));

        // 4. Эффективность Радиатора
        String radiatorText = switch (this.radiatorType) {
            case 1 -> "§6Медный (15%)";
            case 2 -> "§7Стальной (30%)";
            case 3 -> "§eЛатунный (50%)";
            case 4 -> "§bУльтимативный (100%)";
            default -> "§cПассивное (5%)";
        };
        tooltip.add(net.minecraft.network.chat.Component.literal("§b❄ Охлаждение: " + radiatorText));

        tooltip.add(net.minecraft.network.chat.Component.literal("§8--------------------------------"));

        // 5. Телеметрия и Оверклокинг
        tooltip.add(net.minecraft.network.chat.Component.literal("§f📊 ТЕЛЕМЕТРИЯ ЯДРА:"));

        // Цвет температуры в зависимости от опасности плавления
        String tempColor = (this.engineTemperature > (this.maxMeltingTemp - 200f)) ? "§c" : "§a";
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Температура: " + tempColor + String.format("%.1f", this.engineTemperature) + "°C §7/ §4" + this.maxMeltingTemp + "°C"));

        // Выводим обороты ползунка/вала, которые реально крутят шестеренку
        // Показываем игроку плавный, живой разгон оборотов!
        float safeSpeed = getSafeEngineSpeed();
        String speedColor = (this.currentSpeed > safeSpeed) ? "§c⚠ " : "§a";
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Обороты: " + speedColor + String.format("%.0f", this.currentSpeed) + " §7/ §2" + safeSpeed + " RPM"));

        // Показываем остаток топлива в баке в тиках или процентах
        String fuelStatus = (this.burnTimeRemaining > 0) ? "§6" + this.burnTimeRemaining + " тиков" : "§cПУСТОЙ БАК";
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Топливо: " + fuelStatus));

        // Мощность SU тоже привязываем к реальным оборотам поршней
        float currentSU = calculateAddedStressCapacity();
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Мощность сети: §e" + String.format("%.0f", currentSU) + " SU"));

        return true;
    }

    @Override
    public float getGeneratedSpeed() {
        // Пробиваем блокировку Create: выдаем скорость ползунка напрямую!
        return this.targetSliderSpeed;
    }
    @Override
    protected void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        // 1. Сохраняем текстовые параметры архитектуры ДВС
        if (this.engineType != null) tag.putString("EngineType", this.engineType);
        if (this.engineMaterial != null) tag.putString("EngineMaterial", this.engineMaterial);

        // 2. Сохраняем переменные телеметрии
        tag.putBoolean("IsTurboCharged", this.isTurboCharged);
        tag.putFloat("EngineTemperature", this.engineTemperature);
        tag.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        tag.putFloat("CurrentSpeed", this.currentSpeed);
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);
        tag.putInt("RadiatorType", this.radiatorType);

        // 3. Безопасное сохранение твоего бака FuelTank без вызова конфликтующих методов Create!
        if (this.FuelTank != null) {
            net.minecraft.nbt.CompoundTag fluidTag = new net.minecraft.nbt.CompoundTag();
            // Используем стандартный NeoForge-метод сохранения жидкостей, он не зависит от обфускации Create!
            this.FuelTank.writeToNBT(registries, fluidTag);
            tag.put("FuelTankData", fluidTag);
        }
    }

    @Override
    protected void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        // 1. Считываем параметры архитектуры
        if (tag.contains("EngineType")) this.engineType = tag.getString("EngineType");
        if (tag.contains("EngineMaterial")) this.engineMaterial = tag.getString("EngineMaterial");

        // 2. Считываем переменные
        if (tag.contains("EngineTemperature")) {
            this.engineTemperature = tag.getFloat("EngineTemperature");
        } else {
            this.engineTemperature = 20.0f;
        }

        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
        this.burnTimeRemaining = tag.getInt("BurnTimeRemaining");
        this.currentSpeed = tag.getFloat("CurrentSpeed");
        if (tag.contains("TargetSliderSpeed")) this.targetSliderSpeed = tag.getFloat("TargetSliderSpeed");
        this.radiatorType = tag.getInt("RadiatorType");

        // 3. Безопасное считывание бака жидкостей
        if (this.FuelTank != null && tag.contains("FuelTankData")) {
            this.FuelTank.readFromNBT(registries, tag.getCompound("FuelTankData"));
        }
    }
    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        net.minecraft.nbt.CompoundTag tag = super.getUpdateTag(registries);
        // Записываем абсолютно всё состояние для клиента, чтобы анимация не зависала
        tag.putFloat("EngineTemperature", this.engineTemperature);
        tag.putFloat("CurrentSpeed", this.currentSpeed);
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);
        tag.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        tag.putString("EngineType", this.engineType != null ? this.engineType : "I");
        tag.putString("EngineMaterial", this.engineMaterial != null ? this.engineMaterial : "cast_iron");
        return tag;
    }
    // Предоставляем бак для NeoForge BlockCapability системы труб
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidTank() {
        return this.FuelTank;
    }
    // 1. Говорим Create, с какой стороны блока находится крутящийся вал

    // 2. Метод, который Create вызывает для проверки активности источника
    // ЭТОТ МЕТОД РАЗБЛОКИРУЕТ ПЕРЕДАЧУ КРУТЯЩЕГО МОМЕНТА ДЛЯ ВСЕХ 24 ДВИГАТЕЛЕЙ!
    public boolean isSource(net.minecraft.world.level.block.state.BlockState state) {
        // Жестко говорим Create: ДА, этот физический блок является генератором!
        return true;
    }
    // 1. Метод собирает актуальные кастомные данные (скорость, радиатор) и отправляет пакет на клиент
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    // 2. Метод принудительно заставляет клиент перерисовать интерфейс и обновить переменные при получении пакета
    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, net.minecraft.core.HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        // Загружаем прилетевшие с сервера данные прямо в память клиента
        net.minecraft.nbt.CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.read(tag, registries, true);
        }
    }
    // Убираем @Override, чтобы компилятор не ругался на суперкласс
    public net.minecraft.core.Direction getSourceFacing() {
        BlockState blockState = this.getBlockState();
        // Используем встроенное свойство направления Create блоков
        if (blockState.hasProperty(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING)) {
            return blockState.getValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING);
        }
        return net.minecraft.core.Direction.NORTH; // Дефолт, если что-то пошло не так
    }
    public net.minecraft.core.Direction.Axis getRotationAxis() {
        BlockState blockState = this.getBlockState();
        if (blockState.hasProperty(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING)) {
            return blockState.getValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING).getAxis();
        }
        return net.minecraft.core.Direction.Axis.X;
    }



}