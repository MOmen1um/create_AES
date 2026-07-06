package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.AllBlockEntityTypes;

public class NonModularEnginesBlock extends HorizontalKineticBlock implements net.minecraft.world.level.block.EntityBlock {

    private final String material;
    private final String engineType;

    public NonModularEnginesBlock(Properties properties, String material, String engineType) {
        super(properties);
        this.material = material;
        this.engineType = engineType;
    }

    // Этот метод призывает наш универсальный немодульный BlockEntity, передавая туда параметры
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Мы используем ленивую регистрацию. Передаем тип сущности через твой ModBlocks реестр
        return new NonModularEnginesBlockEntity(getLeftType(), pos, state, this.material, this.engineType);
    }

    // Вспомогательный метод, который мы свяжем с ModBlocks чуть позже
    protected BlockEntityType<? extends NonModularEnginesBlockEntity> getLeftType() {
        // Заглушка, здесь IDEA может попросить точную ссылку на тип BlockEntity из реестра
        return null;
    }

    // Говорим Create, с какой стороны у блока выходит крутящий вал (вдоль направления взгляда)
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    // Проверка устойчивости вала Create
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }
}

