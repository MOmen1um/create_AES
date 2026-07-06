package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;

public class NonModularEnginesBlockEntity extends GeneratingKineticBlockEntity {

    // Внутренние параметры, зависящие от выбора игрока
    private final String engineMaterial; // iron, aluminum, titanium
    private final String engineType;     // i4, v8, w16, r32

    // Свойства ДВС, рассчитываемые в switch-case
    protected int countOfPistons = 4;
    protected float materialPower = 30f;
    protected float maxSafeSpeed = 512f;
    protected float maxMeltingTemp = 300f;

    // Состояние двигателя
    public float currentSpeed = 0f;
    public float targetSliderSpeed = 0f; // Обороты, выбранные игроком (0-512)
    public float engineTemperature = 20f; // Начальная температура (комнатная)
    public boolean isTurboCharged = false;

    // Твой бак для топлива из оригинального V8 (на 4 ведра)
    public final FluidTank fuelTank = new FluidTank(4000);

    // КОНСТРУКТОР: Идеально совпадает с требованиями Create 1.21.1
    public NonModularEnginesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String material, String engineType) {
        super(type, pos, state);
        this.engineMaterial = material;
        this.engineType = engineType;
        setupEngineProperties();
    }

    // --- 1. СЕТКА SWITCH-CASE: НАЗНАЧЕНИЕ ХАРАКТЕРИСТИК ---
    private void setupEngineProperties() {
        switch (this.engineMaterial) {
            case "iron" -> {
                this.materialPower = 30f;
                this.maxSafeSpeed = this.isTurboCharged ? 1024f : 512f;
                this.maxMeltingTemp = 300f;
            }
            case "aluminum" -> {
                this.materialPower = 60f;
                this.maxSafeSpeed = this.isTurboCharged ? 4096f : 2048f;
                this.maxMeltingTemp = 450f;
            }
            case "titanium" -> {
                this.materialPower = 100f;
                this.maxSafeSpeed = this.isTurboCharged ? 16384f : 8192f;
                this.maxMeltingTemp = 600f;
            }
        }

        switch (this.engineType) {
            case "i4" -> this.countOfPistons = 4;
            case "v8" -> this.countOfPistons = 8;
            case "w16" -> this.countOfPistons = 16;
            case "r32" -> this.countOfPistons = 32;
        }
    }

    // --- 2. ТВОЯ ГЕНИАЛЬНАЯ ФОРМУЛА МОЩНОСТИ (Чистый Create 1.21.1) ---
    // В 1.21.1 этот метод не принимает аргументов и возвращает базовый стресс
    @Override
    public float calculateAddedStressCapacity() {
        if (this.currentSpeed == 0) return 0;
        float turboCoefficient = this.isTurboCharged ? 2.0f : 1.0f;
        float horsepowerToSU = 64.0f;

        // Твоя формула: Поршни * Дурь Металла * Нагнетатель * 64 SU
        return this.countOfPistons * this.materialPower * turboCoefficient * horsepowerToSU;
    }

    // --- 3. ИНЕРЦИОННЫЙ РАЗГОН И НЬЮТОНОВСКОЕ ОХЛАЖДЕНИЕ ---
    public static void tick(Level level, BlockPos pos, BlockState state, NonModularEnginesBlockEntity be) {
        if (level.isClientSide) return;

        float realTargetSpeed = be.targetSliderSpeed * 64.0f;

        if (be.currentSpeed != realTargetSpeed) {
            float massFactor = (be.countOfPistons * 0.4f) * (be.engineMaterial.equals("iron") ? 1.6f : 1.0f);
            float accelerationRate = (be.countOfPistons * be.materialPower) / (massFactor * 100f);

            if (be.currentSpeed < realTargetSpeed) {
                be.currentSpeed = Math.min(realTargetSpeed, be.currentSpeed + accelerationRate);
            } else {
                be.currentSpeed = Math.max(realTargetSpeed, be.currentSpeed - accelerationRate * 1.5f);
            }
        }

        // Термодинамика
        float ambientTemp = 20.0f;
        float heatGeneration = (Math.abs(be.currentSpeed) / be.maxSafeSpeed) * 3.5f;
        float naturalCooling = (be.engineTemperature - ambientTemp) * 0.015f;

        be.engineTemperature += (heatGeneration - naturalCooling);

        if (be.engineTemperature > be.maxMeltingTemp) {
            be.engineTemperature = be.maxMeltingTemp;
        }

        // Обновляем вращение в Create
        be.updateGeneratedRotation();
        be.setChanged();
    }

    // Сообщаем Create текущую скорость генерации
    @Override
    public float getGeneratedSpeed() {
        return this.currentSpeed;
    }

    // --- 4. СОХРАНЕНИЕ ДАННЫХ (Точная копия твоей рабочей логики Create 1.21.1) ---
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putBoolean("IsTurboCharged", this.isTurboCharged);
        tag.putFloat("CurrentSpeed", this.currentSpeed);
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);
        tag.putFloat("EngineTemperature", this.engineTemperature);

        // В 1.21.1 бак требует registries для записи жидкостей
        CompoundTag fluidTag = new CompoundTag();
        this.fuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
        this.currentSpeed = tag.getFloat("CurrentSpeed");
        this.targetSliderSpeed = tag.getFloat("TargetSliderSpeed");
        this.engineTemperature = tag.getFloat("EngineTemperature");

        if (tag.contains("FuelTank")) {
            this.fuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
        }
        setupEngineProperties(); // Не забываем пересчитать ТТХ при загрузке чанка
    }


    // --- 5. МОНОЛИТНЫЕ ОЧКИ ИНЖЕНЕРА (Полная версия со всеми проверками) ---
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // 1. ПРОВЕРКА КРИТИЧЕСКОЙ СКОРОСТИ ИЗ КОНФИГА CREATE
        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (Math.abs(this.currentSpeed) > createMaxSpeed) {
            tooltip.add(Component.literal("  §c⚠ АВАРИЙНАЯ БЛОКИРОВКА! ⚠"));
            tooltip.add(Component.literal("  §7Ограничение maxRotationSpeed в конфиге Create до " + createMaxSpeed + " RPM"));
            return true; // Сразу выходим, показывая только аварию
        }

        // Вызываем базовые подсказки кинетики Create
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(""));

        // 2. ЦВЕТОВАЯ МАРКИРОВКА МАТЕРИАЛА (Твоя логика подкрашивания тиров)
        String matColor = this.engineMaterial.equals("titanium") ? "§b" : (this.engineMaterial.equals("aluminum") ? "§7" : "§6");
        tooltip.add(Component.literal(" §6Спецификация ДВС V8:"));
        tooltip.add(Component.literal("  §eМатериал блока: " + matColor + this.engineMaterial.toUpperCase()));

        // Нагнетатель
        String turboText = this.isTurboCharged ? "§aУСТАНОВЛЕН" : "§cОТСУТСТВУЕТ";
        tooltip.add(Component.literal("  §eТурбонаддув: " + turboText));

        tooltip.add(Component.literal("  §6Телеметрия температур:"));

        // Твоя логика изменения цвета температуры ядра
        String tempColor = this.engineTemperature > (this.maxMeltingTemp - 200f) ? "§c" : "§a";
        tooltip.add(Component.literal("  §eТемпература ядра: " + tempColor + String.format("%.1f", this.engineTemperature) + "°C / " + this.maxMeltingTemp + "°C"));
        tooltip.add(Component.literal("  §eБезопасная зона: §aдо " + String.format("%.0f", this.maxSafeSpeed) + " RPM"));

        // 3. ПРОВЕРКА РАДИАТОРА ПЕРЕД ДВИГАТЕЛЕМ
        BlockState blockState = this.getBlockState();
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
                tooltip.add(Component.literal("  §6Состояние охлаждения V8:"));
                tooltip.add(Component.literal("   §eРадиатор: " + tierColor + tierName + " (§a" + efficiency + " RPM§e)"));

                if (!radiator.waterTank.isEmpty()) {
                    tooltip.add(Component.literal("   §eЗаполнение бака: §b" + radiator.waterTank.getFluidAmount() + " / " + radiator.waterTank.getCapacity() + " mB"));
                } else {
                    tooltip.add(Component.literal("   §c⚠ РАДИАТОР СУХОЙ (НЕТ ОХЛАЖДЕНИЯ!)"));
                }
            }
        }

        // 4. ИНФОРМАЦИЯ О ТОПЛИВЕ
        if (!this.fuelTank.isEmpty()) {
            tooltip.add(Component.literal("  §eТопливо: §7" + this.fuelTank.getFluid().getHoverName().getString() + " (" + this.fuelTank.getFluidAmount() + " mB)"));
        } else {
            tooltip.add(Component.literal("  §cБак пуст"));
        }

        return true;
    }
}
