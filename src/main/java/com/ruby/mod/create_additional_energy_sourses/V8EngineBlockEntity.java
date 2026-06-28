package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
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

    public int engineQuality = 100;
    public float secretEfficiency = 1.0f;
    public String engineMaterial = "iron";
    public float engineTemperature = 20.0f;
    public boolean isTurboCharged = false;

    private int accelerationTicks = 0;

    public V8EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getAmbientTemperature() {
        if (level == null) return 20.0f;
        float vanillaTemp = level.getBiome(worldPosition).value().getModifiedClimateSettings().temperature();
        if (vanillaTemp <= 0.0f) return -30.0f;
        if (vanillaTemp >= 1.5f) return 60.0f;
        return 20.0f;
    }

    public float getSafeEngineSpeed() {
        float baseLimit = 1024f;
        if (engineMaterial.equals("aluminum")) baseLimit = 4096f;
        if (engineMaterial.equals("titanium")) baseLimit = 8192f;
        float turboModifier = isTurboCharged ? 2.0f : 1.0f;
        return baseLimit * (engineQuality / 100.0f) * secretEfficiency * turboModifier;
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

        // Трансформатор со всеми абстрактными методами Create 1.21.1
        com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform customTransform =
                new com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform() {
                    @Override
                    public net.minecraft.world.phys.Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                        // Ползунок ровно в центре блока ДВС
                        return new net.minecraft.world.phys.Vec3(0.5, 1.0, 0.5);
                    }

                    @Override
                    public void rotate(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, com.mojang.blaze3d.vertex.PoseStack ms) {
                        // Стандартный угол разворота: плашка ползунка смотрит вверх/вперед
                        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
                    }
                };

        com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour slider =
                new com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour(
                        net.minecraft.network.chat.Component.literal("Обороты двигателя (RPM)"),
                        this,
                        customTransform
                );

        // Устанавливаем границы: от 0 до 32768 RPM
        slider.between(0, 128);

        slider.withCallback(value -> {
            this.targetSliderSpeed = value;
            this.updateGeneratedRotation();
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
        float targetSpeed = Math.min(Math.abs(targetSliderSpeed), maxSpeed);

        if (burnTimeRemaining > 0 && targetSpeed > 0) {
            burnTimeRemaining--;

            if (currentSpeed < targetSpeed) {
                if (accelerationTicks < 40) accelerationTicks++;
                float progress = (float) accelerationTicks / 40;
                currentSpeed = targetSpeed * (progress * progress);
            } else {
                currentSpeed = targetSpeed;
            }

            Fluid fluidInTank = fuelTank.getFluid().getFluid();
            float fuelHeat = getFuelHeatMultiplier(fluidInTank);
            float speedRatio = currentSpeed / safeSpeed;
            float heatGeneration = (float) Math.pow(speedRatio, 3) * fuelHeat * 1.5f;

            engineTemperature += heatGeneration;

        } else {
            if (fuelTank.getFluidAmount() >= 100 && targetSpeed > 0) {
                Fluid fluidInTank = fuelTank.getFluid().getFluid();
                burnTimeRemaining = (int) (200 * (getFuelBurnTime(fluidInTank) / 100f));
                fuelTank.drain(100, FluidAction.EXECUTE);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                setChanged();
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
        return currentSpeed * materialMultiplier * (engineQuality / 100.0f);
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
        tooltip.add(Component.literal(" §eКачество сборки: §a" + engineQuality + "%"));

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
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("EngineQuality", engineQuality);
        tag.putFloat("SecretEfficiency", secretEfficiency);
        tag.putString("EngineMaterial", engineMaterial);
        tag.putFloat("EngineTemp", engineTemperature);
        tag.putBoolean("IsTurboCharged", isTurboCharged);
        tag.putFloat("SliderSpeed", targetSliderSpeed);

    }


    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        this.engineQuality = tag.getInt("EngineQuality");
        this.secretEfficiency = tag.getFloat("SecretEfficiency");
        this.engineMaterial = tag.getString("EngineMaterial");
        this.engineTemperature = tag.getFloat("EngineTemp");
        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
        this.targetSliderSpeed = tag.getFloat("SliderSpeed");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("EngineQuality", engineQuality);
        tag.putFloat("SecretEfficiency", secretEfficiency);
        tag.putString("EngineMaterial", engineMaterial);
        tag.putFloat("EngineTemp", engineTemperature);
        tag.putBoolean("IsTurboCharged", isTurboCharged);
        tag.putFloat("SliderSpeed", targetSliderSpeed);

        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public float targetSliderSpeed = 0f;
}





