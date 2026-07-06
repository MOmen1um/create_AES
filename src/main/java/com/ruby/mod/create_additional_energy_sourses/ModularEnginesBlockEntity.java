package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    public ModularEnginesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String material, String engineType) {
        super(type, pos, state, material, engineType);
    }

    // --- 1. ЛОГИКА «УМНОГО» ОБЪЕДИНЁННОГО БАКА ---
    // Этот метод ищет самую первую секцию в ряду, чтобы все блоки сосали топливо из одного бака
    public ModularEnginesBlockEntity getMasterSection() {
        BlockState state = this.getBlockState();
        if (!state.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) return this;

        Direction facing = state.getValue(V8EngineBlock.HORIZONTAL_FACING);
        // Противоположное направление (идем назад по цепочке коленвала)
        Direction backward = facing.getOpposite();

        BlockPos currentPos = this.worldPosition.relative(backward);
        ModularEnginesBlockEntity lastFound = this;

        // Цикл идет назад (максимум на 4 секции по ТЗ), пока находит модульные моторы той же компоновки
        for (int i = 0; i < 4; i++) {
            BlockEntity be = this.level.getBlockEntity(currentPos);
            if (be instanceof ModularEnginesBlockEntity modularNeighbor) {
                lastFound = modularNeighbor;
                currentPos = currentPos.relative(backward);
            } else {
                break; // Цепочка оборвалась, значит lastFound — это главная первая секция
            }
        }
        return lastFound;
    }

    // --- 2. МЕТОД ОБНОВЛЕНИЯ (ТИК) С УСИЛЕННЫМ В 2 РАЗА ОХЛАЖДЕНИЕМ ---
    // Переопределяем тик, чтобы добавить модульные механики топлива и охлаждения
    public static void tick(Level level, BlockPos pos, BlockState state, ModularEnginesBlockEntity be) {
        if (level.isClientSide) return;

        // Вычисляем целевую скорость
        float realTargetSpeed = be.targetSliderSpeed * 64.0f;

        // [Ньютоновский разгон] (Код унаследован из суперкласса)
        if (be.currentSpeed != realTargetSpeed) {
            float massFactor = (be.countOfPistons * 0.4f) * (be.getEngineMaterial().equals("iron") ? 1.6f : 1.0f);
            float accelerationRate = (be.countOfPistons * be.materialPower) / (massFactor * 100f);
            if (be.currentSpeed < realTargetSpeed) {
                be.currentSpeed = Math.min(realTargetSpeed, be.currentSpeed + accelerationRate);
            } else {
                be.currentSpeed = Math.max(realTargetSpeed, be.currentSpeed - accelerationRate * 1.5f);
            }
        }

        // --- МОДУЛЬНАЯ ТЕРМОДИНАМИКА ---
        float ambientTemp = 20.0f;
        float heatGeneration = (Math.abs(be.currentSpeed) / be.maxSafeSpeed) * 3.5f;

        // Базовое рассеивание тепла металлом
        float naturalCooling = (be.engineTemperature - ambientTemp) * 0.015f;

        // Проверяем радиаторы (в модульном ДВС можно облепить блок сильнее, поэтому охлаждение х2)
        boolean hasRadiatorCooling = be.checkRadiatorsActive();
        float radiatorCooling = hasRadiatorCooling ? (be.engineTemperature - ambientTemp) * 0.06f : 0.0f; // Коэффициент 0.06f вместо 0.03f

        // Применяем тепловой баланс (внедряем двойное охлаждение радиатора)
        be.engineTemperature += (heatGeneration - naturalCooling - radiatorCooling);

        if (be.engineTemperature > be.maxMeltingTemp) {
            be.engineTemperature = be.maxMeltingTemp;
        }

        // --- РАСХОД ТОПЛИВА ИЗ ОБЩЕГО БАКА ---
        if (be.currentSpeed > 0) {
            // Находим главную секцию, у которой стоит бак
            ModularEnginesBlockEntity master = be.getMasterSection();
            if (!master.fuelTank.isEmpty() && master.fuelTank.getFluidAmount() >= 1) {
                // Списываем топливо из бака первой секции
                master.fuelTank.drain(1, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                master.setChanged();
            } else {
                // Если бензин в общем баке кончился — глушим эту секцию
                be.currentSpeed = Math.max(0, be.currentSpeed - 5.0f);
            }
        }

        be.updateGeneratedRotation();
        be.setChanged();
    }

    // --- 3. УМНАЯ БОКОВАЯ ПРОВЕРКА РАДИАТОРА (Свободный сквозной вал спереди/сзади) ---
    private boolean checkRadiatorsActive() {
        BlockState blockState = this.getBlockState();
        if (!blockState.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) return false;

        Direction facing = blockState.getValue(V8EngineBlock.HORIZONTAL_FACING);
        int waterUsage = 1; // Сколько воды тратит ОДИН радиатор за тик
        int activeRadiatorsCount = 0;

        // Позиция СЛЕВА от оси двигателя (Левый борт охлаждения)
        BlockPos leftPos = this.worldPosition.relative(facing.getCounterClockWise());
        if (this.level.getBlockEntity(leftPos) instanceof BaseRadiatorBlockEntity rad) {
            if (!rad.waterTank.isEmpty() && rad.waterTank.getFluidAmount() >= waterUsage) {
                if (!this.level.isClientSide) {
                    rad.waterTank.drain(waterUsage, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    rad.setChanged();
                }
                activeRadiatorsCount++;
            }
        }

        // Позиция СПРАВА от оси двигателя (Правый борт охлаждения)
        BlockPos rightPos = this.worldPosition.relative(facing.getClockWise());
        if (this.level.getBlockEntity(rightPos) instanceof BaseRadiatorBlockEntity rad) {
            if (!rad.waterTank.isEmpty() && rad.waterTank.getFluidAmount() >= waterUsage) {
                if (!this.level.isClientSide) {
                    rad.waterTank.drain(waterUsage, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    rad.setChanged();
                }
                activeRadiatorsCount++;
            }
        }

        // Возвращаем true, если работает хотя бы один боковой радиатор с водой
        return activeRadiatorsCount > 0;
    }

    // Геттер материала для формулы массы
    public String getEngineMaterial() {
        return super.addToGoggleTooltip(java.util.List.of(), false) ? "iron" : "iron";
        // Временная заглушка, Java подтянет поле из суперкласса напрямую, если оно protected
    }

    // --- 3. ОБЩЕЕ СВОДНОЕ ИНЖЕНЕРНОЕ МЕНЮ НА ВСЮ ЦЕПОЧКУ ---
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // ИСПРАВЛЕНО: Используем this вместо неопределенного be!
        ModularEnginesBlockEntity master = this.getMasterSection();

        tooltip.add(Component.literal("§6Многосекционный ДВС (МОДУЛЬНЫЙ):"));
        tooltip.add(Component.literal(" §eСтатус секции: §7АКТИВНА"));
        tooltip.add(Component.literal(" §eОбороты секции: §a" + String.format("%.0f", this.currentSpeed) + " RPM"));

        String tempColor = this.engineTemperature > (this.maxMeltingTemp - 200f) ? "§c" : "§a";
        tooltip.add(Component.literal(" §eТемпература секции: " + tempColor + String.format("%.1f", this.engineTemperature) + "°C"));

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§dМагистраль цепочки:"));

        // Показываем общий остаток топлива из главной секции
        if (!master.fuelTank.isEmpty()) {
            tooltip.add(Component.literal(" §eОбщий бак (Голова): §b" + master.fuelTank.getFluidAmount() + " mB"));
        } else {
            tooltip.add(Component.literal(" §c⚠ МАГИСТРАЛЬ ПУСТА (НЕТ ТОПЛИВА В ГОЛОВЕ)"));
        }

        return true;
    }
}
