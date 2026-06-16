package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class ThermoGeneratorBlockEntity extends GeneratingKineticBlockEntity {

    // Конструктор адаптирован под два параметра для NeoForge 1.21.1
    public ThermoGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.THERMO_GEN_ENTITY.get(), pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        if (level == null || level.isClientSide || getBlockState() == null) return 0;

        Direction facing = getBlockState().getValue(ThermoGeneratorBlock.HORIZONTAL_FACING);
        BlockState left = level.getBlockState(worldPosition.relative(facing.getClockWise()));
        BlockState right = level.getBlockState(worldPosition.relative(facing.getCounterClockWise()));

        float heatPower = getHeatValue(right);  // Справа только тепло
        float coldPower = getColdValue(left);   // Слева только холод

        if (heatPower <= 0 && coldPower <= 0) return 0;

        return heatPower + coldPower;
    }

    private float getHeatValue(BlockState state) {
        if (state.getFluidState().is(Fluids.LAVA)) return 64.0f;
        if (state.is(Blocks.MAGMA_BLOCK)) return 32.0f;
        if (state.is(Blocks.BEDROCK)) return 128.0f;
        return 0;
    }

    private float getColdValue(BlockState state) {
        if (state.is(Blocks.BLUE_ICE)) return 64.0f;
        if (state.is(Blocks.PACKED_ICE)) return 32.0f;
        if (state.is(Blocks.SNOW_BLOCK)) return 8.0f;
        if (state.is(Blocks.POWDER_SNOW)) return 12.0f;
        return 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float internalSpeed = getGeneratedSpeed();
        if (internalSpeed <= 0) return 0;
        return internalSpeed * 2.0f;
    }
}
