package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class ThermoGeneratorBlockEntity extends GeneratingKineticBlockEntity {
    public ThermoGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        if (level == null || level.isClientSide || getBlockState() == null) return 0;

        Direction facing = getBlockState().getValue(ThermoGeneratorBlock.HORIZONTAL_FACING);
        BlockState left = level.getBlockState(worldPosition.relative(facing.getClockWise()));
        BlockState right = level.getBlockState(worldPosition.relative(facing.getCounterClockWise()));

        // Считаем общую теплоту
        float heatPower = getHeatValue(right);
        // Считаем общий холод
        float coldPower = getColdValue(left);

        // Если нет одного из компонентов — энергии не будет
        if (heatPower <= 0 || coldPower <= 0) return 0;

        // Итоговая скорость = тепло + холод (чем мощнее источники, тем быстрее крутит)
        // Например: Лава (32) + Синий лед (64) = 96 RPM
        return heatPower + coldPower;
    }

    // Вспомогательный метод для определения "мощности тепла"
    private float getHeatValue(BlockState state) {
        if (state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA)) return 64.0f; // Лава — горячо
        if (state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)) return 32.0f;            // Магма — слабее
        return 0;
    }

    // Вспомогательный метод для определения "мощности холода"
    private float getColdValue(BlockState state) {
        if (state.is(net.minecraft.world.level.block.Blocks.BLUE_ICE)) return 64.0f;              // Синий лед — супер холод
        if (state.is(net.minecraft.world.level.block.Blocks.PACKED_ICE)) return 32.0f;            // Плотный лед — норма
        if (state.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)) return 8.0f;             // Снег — чуть-чуть
        if (state.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW)) return 12.0f;           // Рыхлый снег — получше
        return 0;
    }
    @Override
    public float calculateAddedStressCapacity() {
        float speed = getGeneratedSpeed();
        if (speed <= 0) return 0;

        // Твои новые мощные множители:
        float baseCapacity = 8.0f; // Для начального уровня (магма/снег)

        if (speed >= 96) {
            baseCapacity = 16.0f;  // Для среднего уровня (лава/плотный лед)
        }
        if (speed >= 128) {
            baseCapacity = 32.0f;  // Для топового уровня (лава/синий лед)
        }

        return baseCapacity;
    }
    @Override
    public BlockState getRenderedBlockState() {
        // Говорим Create использовать модельку стандартного вала (Shaft)
        // и разворачивать его вдоль оси нашего генератора
        return com.simibubi.create.AllBlocks.SHAFT.getDefaultState()
                .setValue(com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS,
                        getBlockState().getValue(ThermoGeneratorBlock.HORIZONTAL_FACING).getAxis());
    }


}