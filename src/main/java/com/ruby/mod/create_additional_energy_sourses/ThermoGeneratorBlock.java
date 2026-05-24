package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.material.Fluids;

public class ThermoGeneratorBlock extends HorizontalKineticBlock implements IBE<ThermoGeneratorBlockEntity> {

    public ThermoGeneratorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;

        withBlockEntityDo(level, pos, be -> {
            be.updateGeneratedRotation();
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        });
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<ThermoGeneratorBlockEntity> getBlockEntityClass() {
        return ThermoGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThermoGeneratorBlockEntity> getBlockEntityType() {
        return (BlockEntityType<? extends ThermoGeneratorBlockEntity>) ModBlocks.THERMO_GEN_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThermoGeneratorBlockEntity(getBlockEntityType(), pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(HORIZONTAL_FACING);

        BlockState leftState = level.getBlockState(pos.relative(facing.getClockWise()));
        BlockState rightState = level.getBlockState(pos.relative(facing.getCounterClockWise()));

        boolean hasHeat = leftState.getFluidState().is(Fluids.LAVA) ||
                rightState.getFluidState().is(Fluids.LAVA) ||
                leftState.is(Blocks.MAGMA_BLOCK) ||
                rightState.is(Blocks.MAGMA_BLOCK) ||
                leftState.is(Blocks.BEDROCK) ||
                rightState.is(Blocks.BEDROCK);

        boolean hasCold = leftState.is(Blocks.BLUE_ICE) ||
                rightState.is(Blocks.BLUE_ICE) ||
                leftState.is(Blocks.PACKED_ICE) ||
                rightState.is(Blocks.PACKED_ICE) ||
                leftState.is(Blocks.SNOW_BLOCK) ||
                rightState.is(Blocks.SNOW_BLOCK);

        if (hasHeat && hasCold) {
            if (random.nextFloat() < 0.8F) {
                double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D;
                double y = pos.getY() + 1.0D;
                double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D;

                level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.03D, 0.0D);
                if (random.nextFloat() < 0.4F) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
                }
            }

            if (random.nextFloat() < 0.05F) {
                level.playLocalSound(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.2F,
                        1.2F + random.nextFloat() * 0.4F,
                        false
                );
            }
        }
    }
}
