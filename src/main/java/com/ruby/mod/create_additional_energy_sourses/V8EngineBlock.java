package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class V8EngineBlock extends HorizontalKineticBlock implements IBE<V8EngineBlockEntity> {

    public V8EngineBlock(Properties properties) {
        super(properties);
    }

    // Каноничный способ активировать ползунок настроек Create в 1.21.1
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Разворачиваем двигатель лицом к игроку при установке
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        // Сквозной вал: коленвал торчит и спереди, и сзади двигателя!
        return face == facing.getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<V8EngineBlockEntity> getBlockEntityClass() {
        return V8EngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends V8EngineBlockEntity> getBlockEntityType() {
        // Эту корзину мы сейчас добавим в ModBlocks
        return ModBlocks.V8_ENGINE_ENTITY.get();
    }
}