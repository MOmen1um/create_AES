package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Наследуемся напрямую от твоего идеального работающего эталона!
public class NonModularEnginesBlockEntity extends V8EngineBlockEntity {

    // НОВЫЙ КОНСТРУКТОР: Позволяет модульным наследникам передавать свой тип сущности!
    public NonModularEnginesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public NonModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        // Прокидываем наверх в эталон ПРАВИЛЬНЫЙ общий тип сущности, позицию и стейт!
        super(ModBlocks.NON_MODULAR_ENGINE_ENTITY.get(), pos, state);

        // Твоя утренняя логика определения параметров из имени блока...
        String blockId = state.getBlock().toString().toLowerCase();

        if (blockId.contains("titanium")) {
            this.engineMaterial = "titanium";
            this.maxMeltingTemp = 1668f;
        } else if (blockId.contains("aluminum")) {
            this.engineMaterial = "aluminum";
            this.maxMeltingTemp = 700f;
        } else {
            this.engineMaterial = "cast_iron";
            this.maxMeltingTemp = 1200f;
        }

        if (blockId.contains("i4")) {
            this.engineType = "I";
            this.pistonCount = 4;
        } else if (blockId.contains("w16")) {
            this.engineType = "W";
            this.pistonCount = 16;
        } else if (blockId.contains("r32")) {
            this.engineType = "R";
            this.pistonCount = 32;
        } else {
            this.engineType = "V";
            this.pistonCount = 8;
        }
    }

    @Override
    public float getSafeEngineSpeed() {
        // 1. Базовая безопасная скорость
        float baseSafeSpeed = switch (this.engineMaterial != null ? this.engineMaterial : "cast_iron") {
            case "cast_iron" -> 1024f;
            case "aluminum"  -> 2048f;
            case "titanium"  -> 4096f;
            default -> 512f;
        };

        // 2. Умножение х2 от радиатора
        if (hasRadiatorConnected()) {
            baseSafeSpeed *= 2.0f;
        }

        // 3. Умножение х2 от турбонаддува (если он тоже должен разгонять вал)
        if (this.isTurboCharged) {
            baseSafeSpeed *= 2.0f;
        }

        return baseSafeSpeed;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getSpeed());

        // Защита от загрузки чанка (пока сеть Create не проснулась)
        if (speed < 8.0f || this.burnTimeRemaining <= 0) return 0f;

        // 1. Базовая фиксированная мощность материала
        float targetFixedSU = 1920f*4;
        if (this.engineMaterial != null) {
            if (this.engineMaterial.equals("aluminum")) targetFixedSU = 3840f*4;
            if (this.engineMaterial.equals("titanium")) targetFixedSU = 6400f*8;
        }

        // 2. Безопасный расчёт множителя поршней (без вложенных тернарников)
        float typeMultiplier = 1.0f;
        String currentType = this.engineType != null ? this.engineType : "I";

        if (currentType.equals("V")) typeMultiplier = 8.0f;
        else if (currentType.equals("W")) typeMultiplier = 16.0f;
        else if (currentType.equals("R")) typeMultiplier = 32.0f;
        else typeMultiplier = 4.0f; // Для рядного "I" (I4)

        // 3. Честное умножение х2 от турбины (теперь работает для всех типов!)
        if (this.isTurboCharged) {
            targetFixedSU *= 2.0f;
        }

        targetFixedSU *= typeMultiplier;

        float maxRPM = 16384;
        float calculatedTotalSU;
        if (speed >= maxRPM) {
            calculatedTotalSU = targetFixedSU * (speed / maxRPM);
        } else {
            float rpmRatio = speed / maxRPM;

            float balancaModifier = 1.0f + 0.10f * (1.0f - rpmRatio);

            calculatedTotalSU = targetFixedSU * rpmRatio * balancaModifier;
        }


        if (hasRadiatorConnected()) {
            return calculatedTotalSU / (speed / 2.0f);
        }

        // Если радиатора нет, возвращаем стандартное деление
        return calculatedTotalSU / speed;
    }
    @Override
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            tooltip.add(Component.literal("§c⚠ АВАРИЙНАЯ БЛОКИРОВКА!"));
            tooltip.add(Component.literal("§7Повысьте 'maxRotationSpeed' в конфиге Create до 32768!"));
            return true;
        }
        // Убираем super.addToGoggleTooltip, чтобы на экране не дублировался старый текст!
        tooltip.add(net.minecraft.network.chat.Component.literal("§8----------------------------------------------"));

        // 1. Архитектура и Конфигурация ДВС
        String readableType = switch (this.engineType != null ? this.engineType : "I") {
            case "I" -> "Inline-4 (Рядный)";
            case "V" -> "V8 Engine (V-образный)";
            case "W" -> "W16 Engine (W-образный)";
            case "R" -> "Radial R-32 (Авиационный монстр)";
            default -> "Стандартный ДВС";
        };
        tooltip.add(net.minecraft.network.chat.Component.literal("§6⚙ Спецификация: §7" + readableType));

        // 2. Материал Блока
        String matColor = "titanium".equals(this.engineMaterial) ? "§b" : ("aluminum".equals(this.engineMaterial) ? "§7" : "§8");
        tooltip.add(net.minecraft.network.chat.Component.literal("§e📦 Материал: " + matColor + (this.engineMaterial != null ? this.engineMaterial.toUpperCase() : "CAST_IRON")));

        // 3. Турбонаддув
        String turboText = this.isTurboCharged ? "§a✔ АКТИВИРОВАН (x2 Мощность,Скорость)" : "§c✖ ОТСУТСТВУЕТ";
        tooltip.add(net.minecraft.network.chat.Component.literal("§d🚀 Турбонаддув: " + turboText));

        // 3.5 Динамическое сканирование РАДИАТОРА из твоего эталона V8!
        String radiatorText = "§cПассивное (Нет радиатора)";
        if (this.level != null) {
            net.minecraft.world.level.block.state.BlockState blockState = this.getBlockState();
            // Вычисляем, с какой стороны искать радиатор (как на строках 406-409 в V8)
            if (blockState.hasProperty(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING)) {
                net.minecraft.core.Direction facing = blockState.getValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING);
                net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
                net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

                // Если перед ДВС действительно стоит твой радиатор
                if (neighborBE instanceof BaseRadiatorBlockEntity radiator) {
                    String beName = net.minecraft.world.level.block.entity.BlockEntityType.getKey(radiator.getType()).toString();
                    if (beName.contains("copper")) radiatorText = "§6Медный +25%";
                    else if (beName.contains("steel")) radiatorText = "§7Стальной +50%";
                    else if (beName.contains("brass")) radiatorText = "§eЛатунный +75%";
                    else if (beName.contains("ultimate")) radiatorText = "§bТитановый +100%";
                    else radiatorText = "§fСтандартный радиатор";
                }
            }
        }

        tooltip.add(net.minecraft.network.chat.Component.literal("§8----------------------------------------------"));

        // 4. Живая Телеметрия Ядра
        tooltip.add(net.minecraft.network.chat.Component.literal("§f📊 ТЕЛЕМЕТРИЯ ЯДРА:"));

        String tempColor = (this.engineTemperature > (this.maxMeltingTemp - 200f)) ? "§c" : "§a";
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Температура: " + tempColor + String.format("%.1f", this.engineTemperature) + "°C §7/ §4" + this.maxMeltingTemp + "°C"));

        float currentRpm = Math.abs(getSpeed());
        float safeSpeed = getSafeEngineSpeed();
        String speedColor = (currentRpm > safeSpeed) ? "§c⚠ " : "§a";
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Обороты вала: " + speedColor + String.format("%.0f", currentRpm) + " §7/ §2" + safeSpeed + " RPM"));

        // 5. Считываем топливо из бака fuelTank родителя
        if (this.fuelTank != null) {
            String fluidName = this.fuelTank.getFluid().getHoverName().getString();
            int amount = this.fuelTank.getFluidAmount();
            int capacity = this.fuelTank.getCapacity();

            if (amount > 0) {
                tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Топливо в ДВС: §6" + fluidName + " (" + amount + " / " + capacity + " mB)"));
            } else {
                tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Топливо в ДВС: §c✖ ПУСТОЙ БАК"));
            }
        } else {
            tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Топливо в ДВС: §c✖ БАК НЕ ИНИЦИАЛИЗИРОВАН"));
        }


// === 🌊 НОВЫЙ БЛОК: СТАТУС ОХЛАЖДЕНИЯ И ОБЪЕМ ВОДЫ ===
        if (this.level != null) {
            net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
            if (state.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) {
                net.minecraft.core.Direction facing = state.getValue(V8EngineBlock.HORIZONTAL_FACING);
                net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
                net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

                if (neighborBE instanceof BaseRadiatorBlockEntity radiator) {
                    int currentWater = radiator.waterTank.getFluidAmount();
                    int maxWater = radiator.waterTank.getCapacity(); // Максимальный объём бака радиатора

                    if (currentWater > 0) {
                        tooltip.add(net.minecraft.network.chat.Component.literal("§b❄ Охлаждение: " + radiatorText));
                        // Выводим объём воды красивым сине-голубым цветом, например: "Вода: 950 / 1000 mB"
                        tooltip.add(net.minecraft.network.chat.Component.literal("▪ Хладагент (Вода): §9" + currentWater + " §8/ §3" + maxWater + " mB"));
                    } else {
                        tooltip.add(net.minecraft.network.chat.Component.literal(" ▪ Охлаждение: §4НЕТ ВОДЫ!"));
                    }
                } else {
                    tooltip.add(net.minecraft.network.chat.Component.literal(" ▪ §4НЕТ РАДИАТОРА"));
                    tooltip.add(net.minecraft.network.chat.Component.literal(" ▪ §4ИЛИ ОН НЕПРАВИЛЬНО ПОДКЛЮЧЕН"));
                }
            }
        }

        tooltip.add(net.minecraft.network.chat.Component.literal("§8----------------------------------------------"));
        return true;
    }
}