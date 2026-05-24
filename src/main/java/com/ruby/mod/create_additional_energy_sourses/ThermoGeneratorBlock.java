package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import com.ruby.mod.create_additional_energy_sourses.item.thermoGeneratorblock.blockentity.ThermoGeneratorBlockEntity;
public class ThermoGeneratorBlock extends HorizontalKineticBlock implements IBE<ThermoGeneratorBlockEntity> {

    public ThermoGeneratorBlock(BlockBehaviour.Properties properties) {
        // Мы берем свойства и добавляем к ним звуки и крошки ЖЕЛЕЗА
        super(properties.sound(net.minecraft.world.level.block.SoundType.METAL));
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
        if (level.isClientSide) return; // На клиенте выходим, сервер сделает всё сам

        withBlockEntityDo(level, pos, be -> {
            // 1. Принудительно заставляем мозг генератора пересчитать скорость от лавы/льда
            be.updateGeneratedRotation();

            // 2. Помечаем блок как "измененный", чтобы сервер сохранил его в памяти
            be.setChanged();

            // 3. Отправляем пакет обновления сети, чтобы Create перерисовал вращение вала
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
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends ThermoGeneratorBlockEntity> getBlockEntityType() {
        // Просто возвращаем объект без каких-либо кастов в скобках!
        return ModBlocks.THERMO_GEN_ENTITY.get();
    }

    @Override
    public void animateTick(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        // 1. Узнаем, куда повернут генератор
        net.minecraft.core.Direction facing = state.getValue(HORIZONTAL_FACING);

        // 2. Проверяем блоки слева и справа прямо из мира
        net.minecraft.world.level.block.state.BlockState leftState = level.getBlockState(pos.relative(facing.getClockWise()));
        net.minecraft.world.level.block.state.BlockState rightState = level.getBlockState(pos.relative(facing.getCounterClockWise()));

        // 3. Проверяем наличие тепла
        boolean hasHeat = leftState.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA) ||
                rightState.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA) ||
                leftState.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK) ||
                rightState.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK) ||
                leftState.is(net.minecraft.world.level.block.Blocks.BEDROCK) ||
                rightState.is(net.minecraft.world.level.block.Blocks.BEDROCK);

        // 4. Проверяем наличие холода
        boolean hasCold = leftState.is(net.minecraft.world.level.block.Blocks.BLUE_ICE) ||
                rightState.is(net.minecraft.world.level.block.Blocks.BLUE_ICE) ||
                leftState.is(net.minecraft.world.level.block.Blocks.PACKED_ICE) ||
                rightState.is(net.minecraft.world.level.block.Blocks.PACKED_ICE) ||
                leftState.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK) ||
                rightState.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK);

        // 5. Если оба фактора на месте — пускаем пар и добавляем звук!
        if (hasHeat && hasCold) {
            if (random.nextFloat() < 0.8F) {
                double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D;
                double y = pos.getY() + 1.0D;
                double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D;

                // Спавним пар и дым
                level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD, x, y, z, 0.0D, 0.03D, 0.0D);
                if (random.nextFloat() < 0.4F) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
                }
            }

            // НОВАЯ ФИЧА: Шипение пара с шансом 5% каждый такт
            if (random.nextFloat() < 0.05F) {
                level.playLocalSound(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        net.minecraft.sounds.SoundEvents.LAVA_EXTINGUISH,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        0.2F,
                        1.2F + random.nextFloat() * 0.4F,
                        false
                );
            }
        }
    }
    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Вызываем конструктор через 3 аргумента, передавая тип сущности!
        return new ThermoGeneratorBlockEntity(getBlockEntityType(), pos, state);
    }

}