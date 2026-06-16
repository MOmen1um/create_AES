package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
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

    // --- НАСТРОЙКИ СУПЕР-ТЮНИНГА V8 ---
    public int engineQuality = 100;         // Визуальное качество (20-100%)
    public float secretEfficiency = 1.0f;   // Скрытый коэффициент удачи (0.80 - 1.20)
    public String engineMaterial = "iron";   // iron (1024), aluminum (4096), titanium (8192)
    public float engineTemperature = 20.0f; // Температура мотора в °C
    public boolean isTurboCharged = false;  // Статус наддува

    private int accelerationTicks = 0;

    public V8EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Градусы биома (Тайга -30°C, Пустыня +60°C, Остальные +20°C)
    // ИСПРАВЛЕНИЕ: Используем правильный метод getTemperature без передачи BlockPos!
    public float getAmbientTemperature() {
        if (level == null) return 20.0f;
        // Используем официальный метод-обходчик от NeoForge:
        float vanillaTemp = level.getBiome(worldPosition).value().getModifiedClimateSettings().temperature();

        if (vanillaTemp <= 0.0f) return -30.0f; // Тайга / Зима
        if (vanillaTemp >= 1.5f) return 60.0f;  // Пустыня / Незер
        return 20.0f; // Обычный биом
    }




    // Расчет лимита RPM (Заслонка редстоуна + Сплав + Скрытый тюнинг)
    private float getMaxEngineSpeed() {
        float alloyLimit = 1024f;
        if (engineMaterial.equals("aluminum")) alloyLimit = 4096f;
        if (engineMaterial.equals("titanium")) alloyLimit = 8192f;

        float potentialMax = alloyLimit * (engineQuality / 100.0f) * secretEfficiency;

        int redstoneSignal = level.getBestNeighborSignal(worldPosition);
        float throttleProgress = redstoneSignal / 15.0f;

        float limitModifier = isTurboCharged ? 1.0f : 0.5f;

        return potentialMax * throttleProgress * limitModifier;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        float ambientTemp = getAmbientTemperature();
        float maxAllowedTemp = getMaterialMeltingPoint();
        float maxSpeed = getMaxEngineSpeed();
        int maxAccTicks = 40;

        // 1. ТЕРМОДИНАМИКА И ПАРАБОЛИЧЕСКИЙ РАЗГОН
        if (burnTimeRemaining > 0) {
            burnTimeRemaining--;

            if (currentSpeed < maxSpeed) {
                if (accelerationTicks < maxAccTicks) accelerationTicks++;
                float progress = (float) accelerationTicks / maxAccTicks;
                currentSpeed = maxSpeed * (progress * progress);
                if (currentSpeed > maxSpeed) currentSpeed = maxSpeed;
            }

            // Нагрев зависит от RPM, плотности топлива и скрытой лотереи
            Fluid fluidInTank = fuelTank.getFluid().getFluid();
            float fuelHeatMultiplier = getFuelHeatMultiplier(fluidInTank);

            // Скрытый коэффициент secretEfficiency гасит тепловыделение!
            float heatGen = (currentSpeed / 256f) * fuelHeatMultiplier * (2.0f - secretEfficiency);
            engineTemperature += heatGen * 0.4f;

        } else {
            // Потребление 100 mB солярки
            if (fuelTank.getFluidAmount() >= 100) {
                Fluid fluidInTank = fuelTank.getFluid().getFluid();
                float fuelEfficiency = getFuelBurnTime(fluidInTank);

                if (fuelEfficiency > 0) {
                    fuelTank.drain(100, FluidAction.EXECUTE);
                    burnTimeRemaining = (int) (200 * (fuelEfficiency / 100f));
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    setChanged();
                }
            } else {
                // Плавное затухание по обратной параболе
                if (accelerationTicks > 0) {
                    accelerationTicks--;
                    float progress = (float) accelerationTicks / maxAccTicks;
                    currentSpeed = maxSpeed * (progress * progress);
                } else {
                    currentSpeed = 0;
                }
            }

            // Остывание до температуры окружающей среды
            if (engineTemperature > ambientTemp) {
                engineTemperature -= 0.5f;
            } else if (engineTemperature < ambientTemp) {
                engineTemperature += 0.2f;
            }
        }

        // 2. ВЗРЫВ ПРИ ПЛАВЛЕНИИ МОТОРНОГО СПЛАВА 💥
        if (engineTemperature >= maxAllowedTemp) {
            level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 8.0f, true, net.minecraft.world.level.Level.ExplosionInteraction.TNT);
            level.destroyBlock(worldPosition, false);
            return;
        }

        // 3. АНТИ-СПАМ СИНХРОНИЗАЦИЯ СЕТИ CREATE (Шаг 32 RPM)
        if (Math.abs(currentSpeed - lastSentSpeed) >= 32f || (currentSpeed == 0 && lastSentSpeed != 0)) {
            updateGeneratedRotation();
            lastSentSpeed = currentSpeed;
        }

        // Синхронизация данных для очков раз в секунду
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }

        // 4. ЭФФЕКТ ВЫХЛОПА (Дым густеет от перегрева)
        float smokeChance = 0.1f + (engineTemperature / maxAllowedTemp) * 0.7f;
        if (currentSpeed > 0 && level.random.nextFloat() < smokeChance) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 1.1;
            double z = worldPosition.getZ() + 0.5;
            level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.1, 0.0);
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
        if (fluidId.equals("createdieselgenerators:ethanol")) return 0.6f;
        if (fluidId.equals("createdieselgenerators:biodiesel")) return 1.0f;
        if (fluidId.equals("createdieselgenerators:diesel")) return 1.4f;
        if (fluidId.equals("createdieselgenerators:gasoline")) return 2.2f;
        return 1.0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (currentSpeed <= 0) return 0;
        float materialMultiplier = engineMaterial.equals("iron") ? 12.0f : 8.0f;
        return currentSpeed * materialMultiplier * (engineQuality / 100.0f);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(""));

        String matColor = engineMaterial.equals("titanium") ? "§b" : (engineMaterial.equals("aluminum") ? "§7" : "§8");
        tooltip.add(Component.literal("§6Спецификация ДВС V8:"));
        tooltip.add(Component.literal(" §eМатериал блока: " + matColor + engineMaterial.toUpperCase()));
        tooltip.add(Component.literal(" §eКачество сборки: §a" + engineQuality + "%"));

        String tempColor = engineTemperature > (getMaterialMeltingPoint() - 200) ? "§c" : (engineTemperature > 100 ? "§6" : "§a");
        tooltip.add(Component.literal(" §eТемпература ДВС: " + tempColor + String.format("%.1f", engineTemperature) + "°C / " + getMaterialMeltingPoint() + "°C"));
        tooltip.add(Component.literal(" §7(Окружающая среда: " + String.format("%.1f", getAmbientTemperature()) + "°C)"));

        if (!fuelTank.isEmpty()) {
            String fuelName = fuelTank.getFluid().getHoverName().getString();
            tooltip.add(Component.literal(" §eТопливо в картере: §7" + fuelName + " (" + fuelTank.getFluidAmount() + " mB)"));
        } else {
            tooltip.add(Component.literal(" §cБак пуст"));
        }
        return true;
    }

    // Открытые методы сохранения NBT без ключевого слова protected
    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("EngineQuality", engineQuality);
        tag.putFloat("SecretEfficiency", secretEfficiency);
        tag.putString("EngineMaterial", engineMaterial);
        tag.putFloat("EngineTemp", engineTemperature);
        tag.putBoolean("IsTurboCharged", isTurboCharged);
    }
    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        this.engineQuality = tag.getInt("EngineQuality");
        this.secretEfficiency = tag.getFloat("SecretEfficiency");
        this.engineMaterial = tag.getString("EngineMaterial");
        this.engineTemperature = tag.getFloat("EngineTemp");
        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("EngineQuality", engineQuality);
        tag.putFloat("SecretEfficiency", secretEfficiency);
        tag.putString("EngineMaterial", engineMaterial);
        tag.putFloat("EngineTemp", engineTemperature);
        tag.putBoolean("IsTurboCharged", isTurboCharged);
        return tag;
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}


