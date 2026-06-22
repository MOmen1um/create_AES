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

    public void tick() {
        // ---- 1. СЧИТЫВАЕМ РЕДСТОУН ДЛЯ ПЕРЕДАЧИ ОБОРОТОВ ----
        float targetSpeed = 0.0f;
        if (this.level != null) {
            // Проверяем силу редстоуна, поступающую на наш блок
            int redstoneSignal = this.level.getBestNeighborSignal(this.worldPosition);

            // Масштабируем сигнал: сила 15 выдаст хардкорные 3840 RPM!
            // Безопасный чугунный лимит (1024 RPM) будет достигаться примерно на 4-й силе сигнала.
            targetSpeed = redstoneSignal * 256.0f;
        }

        float safeSpeed = 1024.0f;         // ТЕСТОВЫЙ ЧУГУН: лимит до 1024 RPM
        float meltingPoint = 1200.0f;      // ТЕСТОВЫЙ ЧУГУН: температура взрыва 1200°C

        // ---- 2. ПАРАБОЛИЧЕСКИЙ РАЗГОН И ИНЕРЦИЯ ----
        boolean hasFuel = this.fuelTank != null && !this.fuelTank.getFluid().isEmpty()
                && this.fuelTank.getFluid().getAmount() >= 10;

        if (hasFuel && targetSpeed > 0) {
            if (accelerationTicks < 40) { // 2 секунды на разгон
                accelerationTicks++;
            }
            float progress = (float) accelerationTicks / 40.0f;
            this.currentSpeed = targetSpeed * (progress * progress);
        } else {
            // Если убрали редстоун или кончилось топливо — плавно глохнет
            if (accelerationTicks > 0) {
                accelerationTicks--;
            }
            float progress = (float) accelerationTicks / 40.0f;
            // Чтобы инерция работала правильно, плавно снижаем скорость от последних оборотов
            this.currentSpeed = (this.currentSpeed > 0) ? this.currentSpeed * 0.95f : 0.0f;
            if (this.currentSpeed < 1.0f) this.currentSpeed = 0.0f;
        }

        // Передаем текущие обороты на валы Create
        this.updateGeneratedRotation();

        // ---- 3. КУБИЧЕСКАЯ ТЕРМОДИНАМИКА ----
        if (this.level != null && !this.level.isClientSide) {
            float biomeTemperature = this.level.getBiome(this.worldPosition).value().getBaseTemperature();
            float baseWorldTemp = (biomeTemperature * 40.0f) - 10.0f;

            if (hasFuel && this.currentSpeed > 0) {
                float fuelHeatMultiplier = 1.5f;
                float speedRatio = this.currentSpeed / safeSpeed;

                // Кубический нагрев с балансовым делителем
                float heatGeneration = ((float) Math.pow(speedRatio, 3) * fuelHeatMultiplier) / 4.0f;
                this.engineTemperature += heatGeneration;

                // Расход топлива (NeoForge 1.21.1)
                this.fuelTank.drain(10, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            } else {
                // Естественное охлаждение до комнатной температуры
                if (this.engineTemperature > baseWorldTemp) {
                    this.engineTemperature -= 1.5f;
                } else if (this.engineTemperature < baseWorldTemp) {
                    this.engineTemperature += 0.1f;
                }
            }

            // ---- 4. АВАРИЙНЫЙ ВЗРЫВ ----
            if (this.engineTemperature >= meltingPoint) {
                this.level.explode(null, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), 4.0f, true, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
                this.level.destroyBlock(this.worldPosition, false);
                return;
            }

            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
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



