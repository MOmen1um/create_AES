package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    // Store the controller status inside the brain of our block
    private boolean isController = true;

    // Fixed constructor that accepts 3 arguments from the Block class
    public ModularEnginesBlockEntity(BlockPos pos, BlockState state, boolean isController) {
        super(pos, state);
        this.isController = isController;

        // =====================================================================
        // ВСТАВЛЯЙ СЮДА — СЕТАПИМ МОДУЛЬНЫЕ ТИПЫ ДВИГАТЕЛЕЙ:
        // =====================================================================
        String blockId = state.getBlock().toString().toLowerCase();

        if (blockId.contains("inline2") || blockId.contains("i2")) {
            this.engineType = "I";
            this.pistonCount = 2;
        } else if (blockId.contains("w8")) {
            this.engineType = "W";
            this.pistonCount = 8;
        } else if (blockId.contains("radial16") || blockId.contains("r16")) {
            this.engineType = "Radial";
            this.pistonCount = 16;
        } else {
            // Default configuration for a standard modular block is V4!
            this.engineType = "V";
            this.pistonCount = 4;
        }
        // =====================================================================
    }

    // Getter to check if this block can open GUI and hold fuel
    public boolean isController() {
        return this.isController;
    }
    public void setController(boolean controller) {
        this.isController = controller;
        this.setChanged();
    }
    public ModularEnginesBlockEntity getController() {
        // Если этот блок уже флагнут как контроллер, возвращаем его
        if (this.isController()) {
            return this;
        }

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (!state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) {
            return this; // Безопасный возврат, если что-то пошло не так со стейтом
        }

        net.minecraft.core.Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);
        // Двигаемся НАЗАД (в противоположную сторону от взгляда двигателя) в поисках лидера
        net.minecraft.core.BlockPos behindPos = this.worldPosition.relative(facing.getOpposite());

        if (this.level != null && this.level.getBlockEntity(behindPos) instanceof ModularEnginesBlockEntity neighbor) {
            return neighbor.getController(); // Рекурсивно идем по цепочке блоков назад
        }

        // Если цепочка оборвалась, а блок не контроллер (странное состояние),
        // делаем его контроллером для безопасности
        this.setController(true);
        return this;
    }
    // Метод перераспределяет воду поровну между всеми радиаторами в ряду
    private void balanceWaterInRow(net.minecraft.core.BlockPos startPos, net.minecraft.core.Direction scanDir) {
        if (this.level == null) return;

        // 1. Сначала собираем общую информацию о ряде радиаторов
        int totalWater = getRadiatorRowWaterAndCount(startPos, scanDir, true);
        int radiatorCount = getRadiatorRowWaterAndCount(startPos, scanDir, false);

        // Если радиаторов в ряду нет, балансировать нечего
        if (radiatorCount == 0) return;

        // 2. Рассчитываем, сколько жидкости должно быть в каждом радиаторе
        int waterPerRadiator = totalWater / radiatorCount;
        int remainder = totalWater % radiatorCount; // Остаток от деления, чтобы капли не пропадали

        // 3. Бежим по ряду еще раз и жестко прописываем новое количество воды в баки
        for (int i = 0; i < radiatorCount; i++) {
            net.minecraft.core.BlockPos currentPos = startPos.relative(scanDir, i);
            if (this.level.getBlockEntity(currentPos) instanceof BaseRadiatorBlockEntity radiator) {

                // Сколько воды заливаем в этот конкретный бак
                int targetAmount = waterPerRadiator;
                if (remainder > 0) {
                    targetAmount += 1; // Равномерно раскидываем остаток по первым блокам
                    remainder--;
                }

                // --- ВАЖНО: Манипуляция с баком радиатора ---
                // Сбрасываем старое количество и устанавливаем новое сбалансированное значение.
                // (Замени setFluid/setAmount под реальные методы вашего FluidTank, если они другие)
                if (radiator.waterTank.getFluid() != null) {
                    radiator.waterTank.getFluid().setAmount(targetAmount);
                    radiator.setChanged(); // Флаг для майнкрафта, что данные блока обновились
                }
            }
        }
    }
    // Переменная будет хранить количество блоков в собранной структуре двигателя
    private int currentEngineLength = 1;

    public int getCurrentEngineLength() {
        return this.currentEngineLength;
    }
    @Override
    public void tick() {
        // 1. Сначала вызываем базовую логику дедушки, чтобы двигатель работал
        super.tick();

        // 2. А теперь на сервере запускаем нашу кастомную модульную логику
        if (this.level != null && !this.level.isClientSide() && this.isController()) {
            net.minecraft.world.level.block.state.BlockState state = this.getBlockState();

            if (state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) {
                net.minecraft.core.Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);

                net.minecraft.core.Direction leftDir = facing.getCounterClockWise();
                net.minecraft.core.Direction rightDir = facing.getClockWise();

                net.minecraft.core.BlockPos leftPos = this.worldPosition.relative(leftDir);
                net.minecraft.core.BlockPos rightPos = this.worldPosition.relative(rightDir);

                // Балансируем воду по рядам каждый игровой такт!
                this.balanceWaterInRow(leftPos, leftDir);
                this.balanceWaterInRow(rightPos, rightDir);
            }
        }
    }
    @Override
    protected float getRadiatorCoolingEffect() {
        if (this.level == null || this.level.isClientSide()) return 0.0f;

        // Только контроллер считает и тратит воду! Пасы ничего не делают
        ModularEnginesBlockEntity controller = this.getController();
        if (controller != null && controller != this) {
            return controller.getRadiatorCoolingEffect();
        }

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (!state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) return 0.0f;

        net.minecraft.core.Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);

        net.minecraft.core.Direction leftDir = facing.getCounterClockWise();
        net.minecraft.core.Direction rightDir = facing.getClockWise();

        net.minecraft.core.BlockPos leftPos = this.worldPosition.relative(leftDir);
        net.minecraft.core.BlockPos rightPos = this.worldPosition.relative(rightDir);

        // 1. Считаем сколько ВСЕГО рабочих радиаторов с водой в обоих рядах
        int leftRowWater = getRadiatorRowWaterAndCount(leftPos, leftDir, true);
        int rightRowWater = getRadiatorRowWaterAndCount(rightPos, rightDir, true);

        int leftRowCount = getRadiatorRowWaterAndCount(leftPos, leftDir, false);
        int rightRowCount = getRadiatorRowWaterAndCount(rightPos, rightDir, false);

        int totalWaterAvailable = leftRowWater + rightRowWater;
        int totalRadiatorsCount = leftRowCount + rightRowCount;

        // Если радиаторов нет или во всех пусто — охлаждения нет
        if (totalRadiatorsCount == 0 || totalWaterAvailable == 0) return 0.0f;

        // 2. Рассчитываем общую эффективность охлаждения (наш буст x2 или x4 из тултипа)
        float efficiency = 0.0f;
        int activeSidesWithWater = 0;
        if (leftRowCount > 0 && leftRowWater > 0) activeSidesWithWater++;
        if (rightRowCount > 0 && rightRowWater > 0) activeSidesWithWater++;

        if (activeSidesWithWater == 1) efficiency = 2.0f;
        else if (activeSidesWithWater == 2) efficiency = 4.0f;

        // 3. РАССЧИТЫВАЕМ РАСХОД ВОДЫ (Логика дедушки со скриншота)
        int totalFluidDrain = 0;

        // Проверяем скорость (как на твоем скриншоте: Math.abs(currentSpeed) > 1.0f)
        if (Math.abs(this.getSpeed()) > 1.0f) {
            // Базовый расход дедушки (у тебя там идет расчет от прогрессивного износа/температуры)
            int baseWaterUsage = 10; // Временный фикс расхода воды

            // Модульный двигатель может быть длиннее, поэтому увеличиваем расход от длины структуры
            int totalRequiredDrain = baseWaterUsage * this.getCurrentEngineLength();

            // Ограничиваем расход тем количеством воды, которое у нас реально есть во всех рядах
            totalFluidDrain = Math.min(totalRequiredDrain, totalWaterAvailable);
        }

        // 4. ТРАТИМ ВОДУ ИЗ РЯДОВ РАВНОМЕРНО
        if (totalFluidDrain > 0) {
            int drainPerRadiator = totalFluidDrain / totalRadiatorsCount;
            int remainder = totalFluidDrain % totalRadiatorsCount;

            // Осушаем левый ряд
            remainder = drainWaterFromRow(leftPos, leftDir, leftRowCount, drainPerRadiator, remainder);
            // Осушаем правый ряд
            remainder = drainWaterFromRow(rightPos, rightDir, rightRowCount, drainPerRadiator, remainder);
        }

        // 5. Возвращаем итоговое охлаждение по закону Ньютона (из конца дедушкиного метода)
        return (this.engineTemperature - 20.0f) * efficiency;
    }

    // Хелпер для равномерного осушения баков в ряду радиаторов
    private int drainWaterFromRow(net.minecraft.core.BlockPos startPos, net.minecraft.core.Direction scanDir, int count, int baseDrain, int remainder) {
        if (this.level == null) return remainder;

        for (int i = 0; i < count; i++) {
            net.minecraft.core.BlockPos currentPos = startPos.relative(scanDir, i);
            if (this.level.getBlockEntity(currentPos) instanceof BaseRadiatorBlockEntity radiator) {

                int amountToDrain = baseDrain;
                if (remainder > 0) {
                    amountToDrain += 1;
                    remainder--;
                }

                if (amountToDrain > 0) {
                    // Вызываем дедушкин метод drain() напрямую из бака радиатора
                    radiator.waterTank.drain(amountToDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    radiator.setChanged();

                    // Сетевой пакет обновления блока для плавных полосок воды (как в конце дедушкиного метода)
                    if (this.level.getGameTime() % 5 == 0) {
                        this.level.sendBlockUpdated(radiator.getBlockPos(), radiator.getBlockState(), radiator.getBlockState(), 3);
                    }
                }
            }
        }
        return remainder;
    }

    // Метод сканирует длину структуры двигателя назад от контроллера
    public void updateEngineStructure() {
        if (!this.isController()) return;

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (this.level == null || !state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) return;

        net.minecraft.core.Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);
        int length = 1;

        // Бежим назад против направления взгляда двигателя, считая блоки
        for (int i = 1; i < 10; i++) { // Заложим цикл до 10, чтобы поймать превышение лимита
            net.minecraft.core.BlockPos checkPos = this.worldPosition.relative(facing.getOpposite(), i);

            if (this.level.getBlockEntity(checkPos) instanceof ModularEnginesBlockEntity neighbor) {
                // Если сосед смотрит туда же, значит это наш модуль!
                if (neighbor.getBlockState().getValue(ModularEnginesBlock.HORIZONTAL_FACING) == facing) {
                    length++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        this.currentEngineLength = length;
    }
    // FIX TRIGGER: Force Create API to recognize this block as a true modular entity!
    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<?> getType() {
        // Return your exact registered modular entity holder!
        return ModBlocks.MODULAR_ENGINE_ENTITY.get();
    }
    @Override
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        ModularEnginesBlockEntity controller = this.getController();

        if (controller == null) {
            tooltip.add(net.minecraft.network.chat.Component.literal("§c⚠ Модульный двигатель не собран!"));
            return true;
        }

        if (controller != this) {
            return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        for (int i = tooltip.size() - 1; i >= 0; i--) {
            String lineText = tooltip.get(i).getString();

            // Перехватываем любую строчку, которая содержит слово спецификация у отца
            if (lineText.toLowerCase().contains("спецификация")) {
                String modularType = "Модульный ДВС";

                if (this.engineType != null) {
                    switch (this.engineType) {
                        case "I" -> modularType = "Inline-2 (Рядный)";
                        case "V" -> modularType = "V-4 Engine (V-образный)";
                        case "W" -> modularType = "W-8 Engine (W-образный)";
                        case "Radial" -> modularType = "Radial-16 (Авиационный монстр)";
                    }
                }

                tooltip.set(i, net.minecraft.network.chat.Component.literal("§6⚙ Спецификация: §7МОДУЛЬНЫЙ " + modularType));
            }

            if (lineText.contains("НЕТ РАДИАТОРА") || lineText.contains("ИЛИ ОН НЕПРАВИЛЬНО ПОДКЛЮЧЕН")) {
                tooltip.remove(i);
            }
        }
        for (int i = tooltip.size() - 1; i >= 0; i--) {
            String lineText = tooltip.get(i).getString();

            // УМНАЯ ЗАМЕНА ЗАГОЛОВКА: Ищем только слово "спецификация"
            if (lineText.toLowerCase().contains("спецификация")) {
                tooltip.set(i, net.minecraft.network.chat.Component.literal("§6Спецификация МОДУЛЬНОГО ДВС:"));
            }

            if (lineText.contains("НЕТ РАДИАТОРА") || lineText.contains("ИЛИ ОН НЕПРАВИЛЬНО ПОДКЛЮЧЕН")) {
                tooltip.remove(i);
            }
        }

        this.updateEngineStructure(); // Обновляем данные о длине

        tooltip.add(net.minecraft.network.chat.Component.literal("📦 §7СТРУКТУРА МОДУЛЕЙ ---"));

        if (this.currentEngineLength == 1) {
            tooltip.add(net.minecraft.network.chat.Component.literal(" • Конфигурация: §eОдиночный блок (Не определена)"));
        } else if (this.currentEngineLength <= 4) {
            tooltip.add(net.minecraft.network.chat.Component.literal(" • Длина структуры: §a" + this.currentEngineLength + " / 4 блоков"));
        } else {
            tooltip.add(net.minecraft.network.chat.Component.literal(" • Длина структуры: §c" + this.currentEngineLength + " / 4 §c(КРИТИЧЕСКОЕ ПРЕВЫШЕНИЕ)"));
            tooltip.add(net.minecraft.network.chat.Component.literal(" §c⚠ Двигатель слишком длинный! Мощность заблокирована."));
        }
        tooltip.add(net.minecraft.network.chat.Component.literal("§8----------------------------------------------"));

        // --- ПОЛУЧАЕМ НАПРАВЛЕНИЯ И КООРДИНАТЫ СНАЧАЛА ---
        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (this.level != null && state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);

            // Сначала считаем сами позиции влево и вправо
            net.minecraft.core.BlockPos leftPos = this.worldPosition.relative(facing.getCounterClockWise());
            net.minecraft.core.BlockPos rightPos = this.worldPosition.relative(facing.getClockWise());

            // ТЕПЕРЬ ПОЛУЧАЕМ БЛОКИ (теперь leftPos и rightPos известны игре!)
            net.minecraft.world.level.block.entity.BlockEntity leftBE = this.level.getBlockEntity(leftPos);
            net.minecraft.world.level.block.entity.BlockEntity rightBE = this.level.getBlockEntity(rightPos);

            // 1. Проверяем геометрию подключения
            boolean leftFacingCorrect = isRadiatorFacingCorrectly(leftPos, facing.getCounterClockWise().getOpposite());
            boolean rightFacingCorrect = isRadiatorFacingCorrectly(rightPos, facing.getClockWise().getOpposite());

            // Дальше идет твой обнуленный счетчик радиаторов
            int activeRadiatorsCount = 0;
            int waterFilledRadiatorsCount = 0;

// Проверяем левый радиатор
            if (leftFacingCorrect && leftBE instanceof BaseRadiatorBlockEntity leftRadiator) {
                activeRadiatorsCount++;
                // Если в левом радиаторе есть вода, засчитываем его как рабочий!
                if (leftRadiator.waterTank.getFluidAmount() > 0) {
                    waterFilledRadiatorsCount++;
                }
            }

// Проверяем правый радиатор
            if (rightFacingCorrect && rightBE instanceof BaseRadiatorBlockEntity rightRadiator) {
                activeRadiatorsCount++;
                // Если в правом радиаторе есть вода, засчитываем его как рабочий!
                if (rightRadiator.waterTank.getFluidAmount() > 0) {
                    waterFilledRadiatorsCount++;
                }
            }

            tooltip.add(net.minecraft.network.chat.Component.literal("§7❄ СТАТУС ОХЛАЖДЕНИЯ ---"));

            // 1. BRINGING BACK THE CONNECTION INFO LINES:
            if (leftFacingCorrect) {
                tooltip.add(net.minecraft.network.chat.Component.literal(" • Левый радиатор: §aПОДКЛЮЧЕН"));
            } else {
                tooltip.add(net.minecraft.network.chat.Component.literal(" • Левый радиатор: §cНЕ НАЙДЕН"));
            }

            if (rightFacingCorrect) {
                tooltip.add(net.minecraft.network.chat.Component.literal(" • Правый радиатор: §aПОДКЛЮЧЕН"));
            } else {
                tooltip.add(net.minecraft.network.chat.Component.literal(" • Правый радиатор: §cНЕ НАЙДЕН"));
            }


            // 2. YOUR SMART EFFICIENCY LOGIC (Fully intact):
            if (waterFilledRadiatorsCount == 1) {
                tooltip.add(net.minecraft.network.chat.Component.literal(" • Эффективность охлаждения: §a200%"));
                if (activeRadiatorsCount == 2) {
                    tooltip.add(net.minecraft.network.chat.Component.literal("   §e⚠ Второй радиатор пуст!"));
                }
            }
            else if (waterFilledRadiatorsCount == 2) {
                tooltip.add(net.minecraft.network.chat.Component.literal(" • Эффективность охлаждения: §b400% §e(МАКС)"));
            }
            else if (activeRadiatorsCount > 0 && waterFilledRadiatorsCount == 0) {
                tooltip.add(net.minecraft.network.chat.Component.literal(" §e⚠ Радиаторы Неэффективны!"));
                tooltip.add(net.minecraft.network.chat.Component.literal("   §7-> Залейте воду в систему"));
            }
            else {
                tooltip.add(net.minecraft.network.chat.Component.literal(" §8Радиаторы не подключены."));
                tooltip.add(net.minecraft.network.chat.Component.literal(" §8Не упускайте возможность приумножить скорость и мощность!"));
            }
        }

        return true;
    }
    @Override
    public float getSafeEngineSpeed() {
        ModularEnginesBlockEntity controller = this.getController();
        if (controller != null && controller != this) {
            return controller.getSafeEngineSpeed(); // Пасы просто берут скорость у контроллера
        }

        // 1. Берем базовую скорость дедушки и делим на 2
        float baseSafeSpeed = super.getSafeEngineSpeed() / 2.0f;

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (this.level != null && state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);

            // Координаты и направления для сканирования рядов
            net.minecraft.core.Direction leftDir = facing.getCounterClockWise();
            net.minecraft.core.Direction rightDir = facing.getClockWise();

            net.minecraft.core.BlockPos leftPos = this.worldPosition.relative(leftDir);
            net.minecraft.core.BlockPos rightPos = this.worldPosition.relative(rightDir);

            // Считаем ряды (теперь Java видит переменные выше!)
            int leftRowLength = getRadiatorRowWaterAndCount(leftPos, leftDir, false);
            int rightRowLength = getRadiatorRowWaterAndCount(rightPos, rightDir, false);

            int leftRowWater = getRadiatorRowWaterAndCount(leftPos, leftDir, true);
            int rightRowWater = getRadiatorRowWaterAndCount(rightPos, rightDir, true);

            // Считаем рабочие радиаторы с водой
            int waterFilledRadiatorsCount = 0;
            if (leftRowLength > 0 && leftRowWater > 0) waterFilledRadiatorsCount++;
            if (rightRowLength > 0 && rightRowWater > 0) waterFilledRadiatorsCount++;

            // Применяем бусты скорости
            if (waterFilledRadiatorsCount == 1) {
                baseSafeSpeed *= 2.0f;
            } else if (waterFilledRadiatorsCount == 2) {
                baseSafeSpeed *= 4.0f;
            }
        }

        if (this.isTurboCharged) {
            baseSafeSpeed *= 2.0f;
        }

        return baseSafeSpeed;
    }
    private boolean isRadiatorFacingCorrectly(net.minecraft.core.BlockPos radiatorPos, net.minecraft.core.Direction towardsEngine) {
        if (this.level == null) return false;

        net.minecraft.world.level.block.state.BlockState state = this.level.getBlockState(radiatorPos);

        // We check for Minecraft's standard horizontal facing property instead of a custom class!
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction radiatorFacing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);

            // If the front faces AWAY from the engine, then the back side is touching it!
            return radiatorFacing.getOpposite() == towardsEngine.getOpposite();
        }
        return false;
    }

    @Override
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidTank() {
        ModularEnginesBlockEntity controller = this.getController();

        // If the controller exists and it's not THIS block, proxy the request
        if (controller != null && controller != this) {
            return controller.getFluidTank();
        }

        // Otherwise, use the standard grandfather's tank logic
        return super.getFluidTank();
    }
    // Метод считает количество подключенных радиаторов в ряду (макс 4) и собирает общую воду
    private int getRadiatorRowWaterAndCount(net.minecraft.core.BlockPos startPos, net.minecraft.core.Direction scanDir, boolean checkWater) {
        if (this.level == null) return 0;

        int connectedInRow = 0;
        int totalWaterInRow = 0;

        // Цикл бежит строго на 4 блока в глубину ряда (max length 4)
        for (int i = 0; i < 4; i++) {
            net.minecraft.core.BlockPos currentPos = startPos.relative(scanDir, i);
            net.minecraft.world.level.block.entity.BlockEntity be = this.level.getBlockEntity(currentPos);

            // Проверяем, что блок является радиатором (замени имя класса, если оно другое!)
            if (be instanceof BaseRadiatorBlockEntity radiator) {
                connectedInRow++;
                if (checkWater) {
                    // Суммируем воду из бака каждого радиатора в ряду
                    totalWaterInRow += radiator.waterTank.getFluidAmount();
                }
            } else {
                // Если цепочка радиаторов прервалась (встретили воздух или другой блок), стопаем цикл
                break;
            }
        }

        // Если метод вызван для подсчета воды — возвращаем воду, иначе — количество блоков
        return checkWater ? totalWaterInRow : connectedInRow;
    }
}