package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
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

    // Основные характеристики архитектуры двигателя
    public String engineType;      // "I", "V", "W", "R"
    public String engineMaterial;  // "cast_iron", "aluminum", "titanium"
    public boolean isTurboCharged = false; // Бывшая hasTurbo — возвращаем твое оригинальное имя!
    public int radiatorType = 0;   // 0 - нет, 1 - медный, 2 - стальной, 3 - латунный, 4 - ультимативный

    // Твой родной бак для топлива
    public final FluidTank FuelTank = new FluidTank(4000);

    // Логика работы и тайминги
    public float targetSliderSpeed = 0f;
    private int burnTimeRemaining = 0;
    private float currentSpeed = 0;
    private float lastSentSpeed = -1f;
    public float engineQuality = 1.0f;
    public float secretEfficiency = 1.0f;

    // Термодинамика (Твои оригинальные переменные)
    public float engineTemperature = 20.0f;
    public static final float BASE_TEMP = 20.0f;
    private int accelerationTicks = 0;
    private int overheatMeltingTimer = 0;
    public float maxMeltingTemp; // Вот она! Без опечаток

    public NonModularEnginesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String engineType, String engineMaterial) {
        super(type, pos, state);
        this.engineType = engineType.toUpperCase();
        this.engineMaterial = engineMaterial.toLowerCase();

        this.maxMeltingTemp = switch (this.engineMaterial) {
            case "cast_iron" -> 1200f;   // Чугун плавится при ~1200°C
            case "aluminum" -> 660f;     // Алюминий плавится при 660°C
            case "titanium" -> 1668f;    // Титан держит до 1668°C
            default -> 1200f;
        };
    }

    /**
     * 1. РАСЧЕТ МАКСИМАЛЬНОЙ СКОРОСТИ (RPM)
     */
    public float getMaxSafeSpeed() {
        float baseSpeed = switch (this.engineMaterial) {
            case "cast_iron" -> 1024f;
            case "aluminum" -> 4096f;
            case "titanium" -> 8192f;
            default -> 1024f;
        };
        // Турбина удваивает максимальные обороты
        return isTurboCharged ? baseSpeed * 2.0f : baseSpeed;
    }


    /**
     * 2. ЛОГИКА ОХЛАЖДЕНИЯ (РАДИАТОРЫ)
     */
    public float getCoolingEfficiency() {
        // Множитель эффективности рассеивания тепла
        return switch (this.radiatorType) {
            case 1 -> 0.15f; // Медный (базовый)
            case 2 -> 0.30f; // Стальной (средний)
            case 3 -> 0.50f; // Латунный (хороший)
            case 4 -> 1.00f; // Ультимативный (мощный)
            default -> 0.05f; // Без радиатора (пассивное остывание воздуха)
        };
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
        });

        behaviours.add(slider);
    }

    /**
     * 3. ТИК ДВИГАТЕЛЯ И ТЕРМОДИНАМИКА
     */
    @Override
    public void tick() {
        super.tick();

        // Если мир еще не загрузился или мы на стороне клиента — ничего не делаем
        if (level == null || level.isClientSide) return;
        this.burnTimeRemaining = 99999;

        // 1. ЛОГИКА ТОПЛИВА (Burn Time)
        // Если топливо горит, уменьшаем таймер каждый тик
        if (burnTimeRemaining > 0) {
            burnTimeRemaining--;

            if (level.getGameTime() % 20 == 0) {
                this.setChanged();
                this.notifyUpdate();
            }
        }

        // Получаем текущую скорость вала из Create
        float speed = Math.abs(getSpeed());
        float maxSpeed = getMaxSafeSpeed();

        // 2. ДИНАМИЧЕСКИЙ НАГРЕВ (Твоя откалиброванная механика!)
        if (speed > 0) {
            // Коэффициент подстраивается под материал: на макс. оборотах будет ровно 700°C
            float heatCoefficient = 700.0f / maxSpeed;
            float targetTemperature = BASE_TEMP + (speed * heatCoefficient);

            // Радиатор замедляет нагрев двигателя
            if (engineTemperature < targetTemperature) {
                engineTemperature += 0.2f * (1.0f - getCoolingEfficiency());
            } else if (engineTemperature > targetTemperature) {
                // Если сбросили газ, радиатор помогает остыть быстрее
                engineTemperature -= 0.1f * (1.0f + getCoolingEfficiency());
            }
            this.updateGeneratedRotation();
        } else {
            // Двигатель заглушен — плавно остывает до комнатной температуры
            if (engineTemperature > BASE_TEMP) {
                engineTemperature = Math.max(BASE_TEMP, engineTemperature - (0.5f * (1.0f + getCoolingEfficiency())));
            }
        }

        // 3. ПРОВЕРКА НА КРИТИЧЕСКИЙ ПЕРЕГРЕВ AND ПЛАВЛЕНИЕ
        // Используем твою переменную maxMeltingTemp, которую посчитали в конструкторе!
        if (engineTemperature >= maxMeltingTemp) {
            overheatMeltingTimer++;
            // Если движок кипит дольше 5 секунд (100 тиков) — вызываем бабах
            if (overheatMeltingTimer >= 100) {
                triggerOverheat();
            }
        } else {
            // Если температура упала ниже опасной, сбрасываем таймер разрушения
            if (overheatMeltingTimer > 0) {
                overheatMeltingTimer--;
            }
        }

        // Отправляем пакеты обновлений, если скорость изменилась (для рендеров поршней)
        if (speed != lastSentSpeed) {
            lastSentSpeed = speed;
            notifyUpdate();
        }
        if (level != null && !level.isClientSide) {
            // Если скорость на ползунке изменилась по сравнению с тем, что сейчас выдает вал
            if (this.targetSliderSpeed != this.currentSpeed) {
                this.currentSpeed = this.targetSliderSpeed;

                // ЭТИ ДВЕ СТРОЧКИ ЗАСТАВЯТ CREATE МГНОВЕННО ПЕРЕСЧИТАТЬ СЕТЬ:
                this.updateGeneratedRotation();
                this.setChanged();
            }
        }
    }

    /**
     * 4 РАСЧЕТ МОЩНОСТИ (SU) НА КИНЕТИЧЕСКУЮ СЕТЬ CREATE
     */
    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getSpeed());
        // Если двигатель стоит или в баке нет топлива (burnTimeRemaining == 0), мощность не генерируется
        if (speed <= 0 || burnTimeRemaining <= 0) return 0;

        // 1. Базовая мощность на 1 поршень (при эталонных 64 RPM, как ты расписал в л.с.)
        float powerPerPiston = switch (this.engineMaterial) {
            case "cast_iron" -> 30f * 64f;  // Чугун: 30 л.с. -> 1920 SU
            case "aluminum" -> 60f * 64f;   // Алюминий: 60 л.с. -> 3840 SU
            case "titanium" -> 100f * 64f;  // Титан: 100 л.с. -> 6400 SU
            default -> 30f * 64f;
        };

        // 2. Количество поршней в зависимости от архитектуры (I, V, W, R)
        int pistonCount = switch (this.engineType) {
            case "I" -> 4;   // Рядный
            case "V" -> 8;   // V-образный
            case "W" -> 12;  // W-образный
            case "R" -> 32;  // Твой кастомный 32-поршневой монстр
            default -> 4;
        };

        // Считаем общую базовую мощность мотора
        float totalPower = powerPerPiston * pistonCount;

        // 3. Учитываем множитель турбины
        if (isTurboCharged) {
            totalPower *= 2.0f; // Жёсткий x2 к мощности!
        }

        // В Create итоговый Stress Capacity масштабируется от текущей скорости блока
        // Формула: Общая мощность * (текущая скорость / 64)
        return totalPower * (speed / 64.0f);
    }

    @Override
    protected void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        // Сохраняем базовые характеристики двигателя
        tag.putString("EngineType", this.engineType);
        tag.putString("EngineMaterial", this.engineMaterial);

        // Используем точные методы из твоего эталона!
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);
        tag.putBoolean("IsTurboCharged", this.isTurboCharged);
        tag.putFloat("EngineTemperature", this.engineTemperature);
        tag.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        tag.putFloat("CurrentSpeed", this.currentSpeed);

        // Сохранение твоего бака FuelTank строго по эталону
        net.minecraft.nbt.CompoundTag fluidTag = new net.minecraft.nbt.CompoundTag();
        this.FuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);
    }

    @Override
    protected void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (tag.contains("EngineType")) this.engineType = tag.getString("EngineType");
        if (tag.contains("EngineMaterial")) this.engineMaterial = tag.getString("EngineMaterial");

        // Восстановление логики температурного климата из твоего эталона!
        if (tag.contains("EngineTemperature")) {
            this.engineTemperature = tag.getFloat("EngineTemperature");
        } else {
            // Если данных нет, оставляем климат биома (20°C)
            this.engineTemperature = 20.0f;
        }

        // Считываем остальные переменные
        if (tag.contains("TargetSliderSpeed")) this.targetSliderSpeed = tag.getFloat("TargetSliderSpeed");

        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
        this.burnTimeRemaining = tag.getInt("BurnTimeRemaining");
        this.currentSpeed = tag.getFloat("CurrentSpeed");

        // Загрузка бака FuelTank строго по эталону
        if (tag.contains("FuelTank")) {
            this.FuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            tooltip.add(Component.literal("§c⚠ АВАРИЙНАЯ БЛОКИРОВКА!"));
            tooltip.add(Component.literal("§7Повысьте 'maxRotationSpeed' в конфиге Create до 32768!"));
            return true;
        }

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(""));

        String matColor = engineMaterial.equals("titanium") ? "§b" : (engineMaterial.equals("aluminum") ? "§7" : "§8");
        tooltip.add(Component.literal("§6Спецификация ДВС V8:"));
        tooltip.add(Component.literal(" §eМатериал блока: " + matColor + engineMaterial.toUpperCase()));

        String turboText = isTurboCharged ? "§aУСТАНОВЛЕН" : "§cОТСУТСТВУЕТ";
        tooltip.add(Component.literal(" §eТурбонаддув: " + turboText));

        tooltip.add(Component.literal(" §6Телеметрия температур:"));
        String tempColor = engineTemperature > (maxMeltingTemp - 200) ? "§c" : (engineTemperature > 90 ? "§6" : "§a");
        tooltip.add(Component.literal(" §eТемпература ядра: " + tempColor + String.format("%.1f", engineTemperature) + "°C / " + maxMeltingTemp + "°C"));
        tooltip.add(Component.literal(" §eБезопасная зона: §aдо " + String.format("%.0f", getMaxSafeSpeed()) + " RPM"));
        net.minecraft.world.level.block.state.BlockState blockState = this.getBlockState();
        if (blockState.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = blockState.getValue(V8EngineBlock.HORIZONTAL_FACING);
            net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
            net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

            if (neighborBE instanceof BaseRadiatorBlockEntity radiator) {
                String beName = net.minecraft.world.level.block.entity.BlockEntityType.getKey(radiator.getType()).toString();
                String tierName = "ОБЫЧНЫЙ";
                String tierColor = "§7";
                String efficiency = "100%";

                if (beName.contains("copper")) { tierName = "МЕДНЫЙ"; tierColor = "§6"; efficiency = "+25%"; }
                else if (beName.contains("steel")) { tierName = "СТАЛЬНОЙ"; tierColor = "§f"; efficiency = "+50%"; }
                else if (beName.contains("brass")) { tierName = "ЛАТУННЫЙ"; tierColor = "§e"; efficiency = "+75%"; }
                else if (beName.contains("ultimate")) { tierName = "УЛЬТИМАТИВНЫЙ"; tierColor = "§b"; efficiency = "+100%"; }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("§6Состояние охлаждения V8:"));
                tooltip.add(Component.literal(" §eРадиатор: " + tierColor + tierName + " (§a" + efficiency + " RPM§e)"));

                if (!radiator.waterTank.isEmpty()) {
                    tooltip.add(Component.literal(" §eЗаполнение бака: §b" + radiator.waterTank.getFluidAmount() + " / " + radiator.waterTank.getCapacity() + " mB"));
                } else {
                    tooltip.add(Component.literal(" §c⚠ РАДИАТОР СУХОЙ (НЕТ ОХЛАЖДЕНИЯ!)"));
                }
            }
        }

        if (!FuelTank.isEmpty()) {
            tooltip.add(Component.literal(" §eТопливо: §7" + FuelTank.getFluid().getHoverName().getString() + " (" + FuelTank.getFluidAmount() + " mB)"));
        } else {
            tooltip.add(Component.literal(" §cБак пуст"));
        }
        return true;
    }

    private void triggerOverheat() {
        // Логика взрыва или поломки блока при перегреве
        // level.explode(...) или превращение блока в разрушенный аналог
    }

    // 1. Метод отправляет данные клиенту при первой загрузке чанка
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    // 3. Метод заставляет Create обновлять рендер, когда скорость вала меняется
    @Override
    public void notifyUpdate() {
        super.notifyUpdate();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        net.minecraft.nbt.CompoundTag tag = super.getUpdateTag(registries);
        // Записываем абсолютно всё состояние для клиента, чтобы меню не "слетало"
        tag.putString("EngineType", this.engineType != null ? this.engineType : "I");
        tag.putString("EngineMaterial", this.engineMaterial != null ? this.engineMaterial : "cast_iron");
        tag.putInt("RadiatorType", this.radiatorType);
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);
        tag.putFloat("EngineTemperature", this.engineTemperature);
        tag.putInt("BurnTimeRemaining", this.burnTimeRemaining);

        // Синхронизируем жидкость в баке для инженеров
        net.minecraft.nbt.CompoundTag fluidTag = new net.minecraft.nbt.CompoundTag();
        this.FuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);

        return tag;
    }

    // Включаем/выключаем турбину (можно вызывать при установке кастомного апгрейда на блок)
    public void setTurbo(boolean isTurboCharged) {
        this.isTurboCharged = isTurboCharged;
        notifyUpdate();
    }

    // Устанавливаем тип радиатора
    public void setRadiatorType(int type) {
        this.radiatorType = Mth.clamp(type, 0, 4);
        notifyUpdate();
    }

    public float getEngineTemperature() {
        return this.engineTemperature;
    }
    // Предоставляем бак для NeoForge BlockCapability системы труб
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidTank() {
        return this.FuelTank;
    }
    private void onFluidTankChanged(net.neoforged.neoforge.fluids.FluidStack stack) {
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
