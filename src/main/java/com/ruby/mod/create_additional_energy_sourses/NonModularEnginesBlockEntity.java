package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

// Наследуемся напрямую от твоего идеального работающего эталона!
public class NonModularEnginesBlockEntity extends V8EngineBlockEntity {

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
            this.maxMeltingTemp = 660f;
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
        // Базовый безопасный порог без охлаждения из твоих чертежей
        float baseSafeSpeed = switch (this.engineMaterial != null ? this.engineMaterial : "cast_iron") {
            case "cast_iron" -> 512f;
            case "aluminum" -> 1024f;
            case "titanium" -> 2048f;
            default -> 512f;
        };

        // Сканируем радиатор перед ДВС (как в твоем эталоне V8)
        if (this.level != null) {
            net.minecraft.world.level.block.state.BlockState blockState = this.getBlockState();
            if (blockState.hasProperty(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING)) {
                net.minecraft.core.Direction facing = blockState.getValue(com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING);
                net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
                net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

                // Если перед ДВС стоит радиатор — ЖЕСТКО УДВАИВАЕМ БЕЗОПАСНЫЙ ПОРОГ!
                if (neighborBE instanceof BaseRadiatorBlockEntity) {
                    baseSafeSpeed *= 2.0f;
                }
            }
        }

        return baseSafeSpeed;
    }

    // ЭТОТ ОВЕРРАЙД ПОЛНОСТЬЮ ОТКЛЮЧИТ СТАРЫЙ МЕТОД ИЗ V8 И УБЕРЕТ МИЛЛИОНЫ SU!
    @Override
    public float calculateAddedStressCapacity() {
        // Забираем текущую скорость всей сети Create
        float speed = Math.abs(getSpeed());

        // Если бак пустой или валы стоят — выдаем ровно 0 SU
        if (speed <= 0 || this.burnTimeRemaining <= 0) return 0;

        // Строгий баланс Diesel Generators: выставляем чистую базовую силу на один поршень
        float powerPerPiston = switch (this.engineMaterial != null ? this.engineMaterial : "cast_iron") {
            case "cast_iron" -> 30f * 64;   // Чугун (для I4 даст базовые 64 SU)
            case "aluminum" -> 60f * 64;    // Алюминий (для I4 даст базовые 128 SU)
            case "titanium" -> 100f * 64;    // Титан (для I4 даст базовые 256 SU)
            default -> 16f;
        };

        // Умножаем на количество поршней, которое мы успешно определили в конструкторе!
        float totalStaticPower = powerPerPiston * this.pistonCount;

        // Если турбонаддув включен — жестко удваиваем крутящий момент
        if (this.isTurboCharged) {
            return totalStaticPower *= 2.0f;
        }

        // Возвращаем ГОТОВУЮ базовую константу.
        // Больше никакого родительского кода! Create сам умножит её на скорость сети,
        // и цифры на табло мгновенно рухнут до адекватных, красивых значений.
        return totalStaticPower / speed;
    }
    @Override
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        // Убираем super.addToGoggleTooltip, чтобы на экране не дублировался старый текст!
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
        String matColor = "titanium".equals(this.engineMaterial) ? "§b" : ("aluminum".equals(this.engineMaterial) ? "§7" : "§8");
        tooltip.add(net.minecraft.network.chat.Component.literal("§e📦 Материал: " + matColor + (this.engineMaterial != null ? this.engineMaterial.toUpperCase() : "CAST_IRON")));

        // 3. Турбонаддув
        String turboText = this.isTurboCharged ? "§a✔ АКТИВИРОВАН (x2 Мощность)" : "§c✖ ОТСУТСТВУЕТ";
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
                    if (beName.contains("copper")) radiatorText = "§6Медный (+25% Охлаждения)";
                    else if (beName.contains("steel")) radiatorText = "§7Стальной (+50% Охлаждения)";
                    else if (beName.contains("brass")) radiatorText = "§eЛатунный (+75% Охлаждения)";
                    else if (beName.contains("ultimate")) radiatorText = "§bУЛЬТИМАТИВНЫЙ (+100% Охлаждения)";
                    else radiatorText = "§fСтандартный радиатор";
                }
            }
        }
        tooltip.add(net.minecraft.network.chat.Component.literal("§b❄ Охлаждение: " + radiatorText));

        tooltip.add(net.minecraft.network.chat.Component.literal("§8--------------------------------"));

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

        // 6. Выводим сбалансированную мощность SU (без двойного умножения скорости!)
        float currentSU = calculateAddedStressCapacity();
        tooltip.add(net.minecraft.network.chat.Component.literal("§8 • Мощность генератора: §e" + String.format("%.0f", currentSU) + " SU"));

        return true;
    }

}