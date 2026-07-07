package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlock extends HorizontalKineticBlock implements net.minecraft.world.level.block.EntityBlock {

    private final String material;
    private final String engineType;

    public ModularEnginesBlock(Properties properties, String material, String engineType) {
        super(properties);
        this.material = material;
        this.engineType = engineType;
    }

    // Рождаем модульный BlockEntity
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Оставляем строго 2 аргумента! Класс сам всё определит автоматически
        return new ModularEnginesBlockEntity(pos, state);
    }

    // Вспомогательный метод (убрали @Override, чтобы Create не ругался)
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends ModularEnginesBlockEntity> getBlockEntityType() {
        return ModBlocks.MODULAR_ENGINE_ENTITY.get();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

}

