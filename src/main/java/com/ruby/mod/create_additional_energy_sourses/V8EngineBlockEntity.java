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

public class V8EngineBlockEntity extends GeneratingKineticBlockEntity {

    public final FluidTank fuelTank = new FluidTank(4000);
    private int burnTimeRemaining = 0;
    private float currentSpeed = 0;
    private float lastSentSpeed = -1f;

    public float engineQuality = 1.0f;
    public float secretEfficiency = 1.0f;
    public String engineMaterial;
    public float engineTemperature = 20.0f;
    public boolean isTurboCharged = false;

    private int accelerationTicks = 0;

    // Конструктор по умолчанию
    public V8EngineBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlocks.V8_ENGINE_ENTITY.get(), pos, state, "iron");
    }

    // Главный конструктор
    public V8EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String material) {
        super(type, pos, state);
        this.engineMaterial = material;

        // УБРАЛИ РАНДОМ: Теперь при создании у всех двигателей базовые 100% параметров
        this.engineQuality = 1.0f;
        this.secretEfficiency = 1.0f;
    }

    public float getAmbientTemperature() {
        if (level == null) return 20.0f;
        float vanillaTemp = level.getBiome(worldPosition).value().getModifiedClimateSettings().temperature();
        if (vanillaTemp <= 0.0f) return -30.0f;
        if (vanillaTemp >= 1.5f) return 60.0f;
        return 20.0f;
    }

    public float getSafeEngineSpeed() {
        // 1. ВОССТАНОВИЛИ ТВОЮ ОРИГИНАЛЬНУЮ ЛОГИКУ СКОРОСТЕЙ
        float baseLimit = 1024f;
        if (this.engineMaterial != null) {
            if (this.engineMaterial.equals("aluminum")) baseLimit = 4096f;
            if (this.engineMaterial.equals("titanium")) baseLimit = 8192f;
        }

        // Учёт турбонаддува (твоя оригинальная тернарная строчка)
        float turboModifier = isTurboCharged ? 2.0f : 1.0f;

        // Перемножаем базовый лимит на турбину
        float baseSafeSpeed = baseLimit * turboModifier;

        // --- 2. ИНТЕГРИРУЕМ РАДИАТОРЫ THE LONG DRIVE СТРОГО СПЕРЕДИ ---
        float currentMultiplier = 1.0f;

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (state.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = state.getValue(V8EngineBlock.HORIZONTAL_FACING);

            // Ищем блок радиатора строго на морде двигателя
            net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
            net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

            if (neighborBE != null) {
                String beName = net.minecraft.world.level.block.entity.BlockEntityType.getKey(neighborBE.getType()).toString();

                // Проверяем тиры радиаторов и выставляем множители скорости
                if (beName.startsWith("create_additional_energy_sourses:radiator_")) {
                    if (beName.contains("copper")) currentMultiplier = 1.25f;
                    if (beName.contains("steel")) currentMultiplier = 1.50f;
                    if (beName.contains("brass")) currentMultiplier = 1.75f;
                    if (beName.contains("ultimate")) currentMultiplier = 2.00f;
                }
            }
        }

        // Применяем множитель охлаждения к финальной скорости материала
        float rawCalculatedSpeed = baseSafeSpeed * currentMultiplier;

        // Железное округление до ближайшего числа, кратного 64, под сетку слайдера Create!
        return Math.round(rawCalculatedSpeed / 64.0f) * 64.0f;
    }


    private float getMaxEngineSpeed() {
        float safeSpeed = getSafeEngineSpeed();
        float fuelSpeedMultiplier = 1.0f;
        if (burnTimeRemaining > 0 && !fuelTank.isEmpty()) {
            String fluidId = BuiltInRegistries.FLUID.getKey(fuelTank.getFluid().getFluid()).toString();
            if (fluidId.equals("createdieselgenerators:ethanol")) fuelSpeedMultiplier = 0.6f;
            if (fluidId.equals("createdieselgenerators:biodiesel")) fuelSpeedMultiplier = 1.0f;
            if (fluidId.equals("createdieselgenerators:diesel")) fuelSpeedMultiplier = 1.4f;
            if (fluidId.equals("createdieselgenerators:gasoline")) fuelSpeedMultiplier = 2.0f;
        }
        return safeSpeed * fuelSpeedMultiplier;
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

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            currentSpeed = 0;
            return;
        }

        float ambientTemp = getAmbientTemperature();
        float meltingPoint = getMaterialMeltingPoint();
        float safeSpeed = getSafeEngineSpeed();
        float maxSpeed = getMaxEngineSpeed();
        float targetSpeed = Math.min(Math.abs(this.targetSliderSpeed * 64f), getMaxEngineSpeed());

        if (this.burnTimeRemaining > 0 && targetSpeed > 0) {
            // --- РАСЧЁТ ДИНАМИЧЕСКОГО РАСХОДА ТОПЛИВА ---
            // Находим отношение текущей скорости к безопасной (speedRatio)
            float speedRatio = this.currentSpeed / this.getSafeEngineSpeed();

            // Рассчитываем множитель расхода:
            // Если мотор стоит или еле крутится, расход минимальный (0.2).
            // На полной безопасной скорости расход равен 1.0.
            // В зоне оверспида (наддува) расход растёт квадратично, заставляя мотор «жрать» бензин ведрами!
            float fuelConsumptionMultiplier = 0.2f + (float) Math.pow(speedRatio, 2) * 0.8f;

            // Переводим множитель в целые тики и уменьшаем время горения.
            // Минимальный расход — 1 тик за тик, чтобы топливо не зависало бесконечно.
            int ticksToBurn = Math.max(1, Math.round(fuelConsumptionMultiplier));
            this.burnTimeRemaining = Math.max(0, this.burnTimeRemaining - ticksToBurn);
            // ---------------------------------------------




            this.setChanged();
            this.sendData(); // Это заставит Create посылать точный объем топлива с сервера на твой экран!
            if (currentSpeed < targetSpeed) {
                if (accelerationTicks < 40) accelerationTicks++;
                float progress = (float) accelerationTicks / 40;
                currentSpeed = targetSpeed * (progress * progress);
            } else {
                currentSpeed = targetSpeed;
            }

            Fluid fluidInTank = fuelTank.getFluid().getFluid();
            float fuelHeat = getFuelHeatMultiplier(fluidInTank);
            float heatGeneration = (float) Math.pow(speedRatio, 3) * fuelHeat * 1.5f;



            engineTemperature += heatGeneration;

            if (this.currentSpeed <= safeSpeed) {
                // Если игрок держит скорость в норме, температура балансирует в районе 595 - 600 градусов
                if (this.engineTemperature > 600.0f) {
                    // Добавляем реалистичное колебание вокруг 600°C (от 598.0 до 600.0)
                    float vibration = (float) Math.sin(this.level.getGameTime() * 0.2f) * 1.0f;
                    this.engineTemperature = 599.0f + vibration;
                }
            }

        } else {
            if (fuelTank.getFluidAmount() >= 100 && targetSpeed > 0) {
                Fluid fluidInTank = fuelTank.getFluid().getFluid();
                burnTimeRemaining = (int) (200 * (getFuelBurnTime(fluidInTank) / 100f));
                fuelTank.drain(100, FluidAction.EXECUTE);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                setChanged();

                this.setChanged();
                this.sendData(); // Это заставит Create посылать точный объем топлива с сервера на твой экран!
            } else {
                if (accelerationTicks > 0) {
                    accelerationTicks--;
                    float progress = (float) accelerationTicks / 40;
                    currentSpeed = targetSpeed * (progress * progress);
                } else {
                    currentSpeed = 0;
                }
            }

            if (engineTemperature > ambientTemp) engineTemperature -= 0.6f;
            else if (engineTemperature < ambientTemp) engineTemperature += 0.2f;
        }

        if (engineTemperature >= meltingPoint) {
            level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 9.0f, true, net.minecraft.world.level.Level.ExplosionInteraction.TNT);
            level.destroyBlock(worldPosition, false);
            return;
        }

        if (Math.abs(currentSpeed - lastSentSpeed) >= 16f || (currentSpeed == 0 && lastSentSpeed != 0)) {
            updateGeneratedRotation();
            lastSentSpeed = currentSpeed;
        }

        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }

        float smokeChance = 0.05f + (engineTemperature / meltingPoint) * 0.75f;
        if (currentSpeed > 0 && level.random.nextFloat() < smokeChance && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, 1, 0, 0.08, 0, 0);
        }

        if (this.level.getGameTime() % 20 == 0) {
            this.setChanged();
            this.sendData(); // Синхронизирует безопасную скорость и литры топлива с клиентом!
        }
    }

    private float getMaterialMeltingPoint() {
        if (engineMaterial.equals("aluminum")) return 660.0f;
        if (engineMaterial.equals("titanium")) return 1660.0f;
        return 1200.0f;
    }

    @Override
    public float getGeneratedSpeed() {
        return currentSpeed;
    }

    private float getFuelBurnTime(Fluid fluid) {
        String fluidId = BuiltInRegistries.FLUID.getKey(fluid).toString();
        if (fluidId.equals("createdieselgenerators:diesel")) return 100.0f;
        if (fluidId.equals("createdieselgenerators:biodiesel")) return 80.0f;
        if (fluidId.equals("createdieselgenerators:gasoline")) return 160.0f;
        if (fluidId.equals("createdieselgenerators:ethanol")) return 60.0f;
        return 0;
    }

    private float getFuelHeatMultiplier(Fluid fluid) {
        String fluidId = BuiltInRegistries.FLUID.getKey(fluid).toString();
        if (fluidId.equals("createdieselgenerators:ethanol")) return 0.5f;
        if (fluidId.equals("createdieselgenerators:biodiesel")) return 1.0f;
        if (fluidId.equals("createdieselgenerators:diesel")) return 1.5f;
        if (fluidId.equals("createdieselgenerators:gasoline")) return 2.5f;
        return 1.0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (currentSpeed <= 0) return 0;
        float materialMultiplier = engineMaterial.equals("iron") ? 15.0f : 10.0f;
        return currentSpeed * materialMultiplier * secretEfficiency;
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
        String tempColor = engineTemperature > (getMaterialMeltingPoint() - 200) ? "§c" : (engineTemperature > 90 ? "§6" : "§a");
        tooltip.add(Component.literal(" §eТемпература ядра: " + tempColor + String.format("%.1f", engineTemperature) + "°C / " + getMaterialMeltingPoint() + "°C"));
        tooltip.add(Component.literal(" §eБезопасная зона: §aдо " + String.format("%.0f", getSafeEngineSpeed()) + " RPM"));

        if (!fuelTank.isEmpty()) {
            tooltip.add(Component.literal(" §eТопливо: §7" + fuelTank.getFluid().getHoverName().getString() + " (" + fuelTank.getFluidAmount() + " mB)"));
        } else {
            tooltip.add(Component.literal(" §cБак пуст"));
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // ИСПРАВЛЕНО: Добавили registries в super.write
        super.write(tag, registries, clientPacket);

        tag.putBoolean("IsTurboCharged", this.isTurboCharged);
        tag.putFloat("CurrentSpeed", this.currentSpeed);
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);
        tag.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        tag.putFloat("EngineTemperature", this.engineTemperature);
        tag.putInt("AccelerationTicks", this.accelerationTicks);

        // ИСПРАВЛЕНО: В 1.21.1 бак требует registries для записи жидкостей
        CompoundTag fluidTag = new CompoundTag();
        this.fuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // ИСПРАВЛЕНО: Добавили registries в super.read
        super.read(tag, registries, clientPacket);


        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
        this.currentSpeed = tag.getFloat("CurrentSpeed");
        this.targetSliderSpeed = tag.getFloat("TargetSliderSpeed");
        this.burnTimeRemaining = tag.getInt("BurnTimeRemaining");
        this.engineTemperature = tag.getFloat("EngineTemperature");
        this.accelerationTicks = tag.getFloat("AccelerationTicks") != 0 ? (int)tag.getFloat("AccelerationTicks") : tag.getInt("AccelerationTicks"); // Безопасное чтение типов
        // ИСПРАВЛЕНО: Бак требует registries для чтения в 1.21.1
        if (tag.contains("FuelTank")) {
            this.fuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
        }
    }

    // --- 2. СИНХРОНИЗАЦИЯ ПАКЕТОВ ДЛЯ ОЧКОВ ИНЖЕНЕРА (ОБНОВЛЕНИЕ БАКА НА ЭКРАНЕ) ---
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("EngineMaterial", engineMaterial);
        tag.putFloat("EngineTemp", engineTemperature);
        tag.putBoolean("IsTurboCharged", isTurboCharged);
        tag.putFloat("SliderSpeed", targetSliderSpeed);

        // Упаковываем бак в сетевой тег обновления с поддержкой провайдера реестров
        CompoundTag fluidTag = new CompoundTag();
        this.fuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Предоставляем бак для NeoForge BlockCapability системы труб
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidTank() {
        return this.fuelTank;
    }

    public float targetSliderSpeed = 0f;
}





