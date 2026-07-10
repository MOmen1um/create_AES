package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlock extends V8EngineBlock {

    // Переменная, которая хранит статус конкретного установленного блока
    private final boolean isController;

    // Конструктор №1 (Для обычных модулей, где флаг не передается)
    public ModularEnginesBlock(Properties properties) {
        super(properties);
        this.isController = false;
    }

    // Конструктор №2 (Для блоков-контроллеров из метода registerController)
    public ModularEnginesBlock(Properties properties, boolean isController) {
        super(properties);
        this.isController = isController;
    }

    // Метод создания сущности блока (BlockEntity)
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Мы создаем сущность и сразу передаем ей pos, state и наш флаг контроллера!
        return new ModularEnginesBlockEntity(pos, state, this.isController);
    }
    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state, net.minecraft.core.Direction face) {
        if (state.hasProperty(HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = state.getValue(HORIZONTAL_FACING);
            // Сквозной вал: connection is allowed from both front and back!
            return face == facing || face == facing.getOpposite();
        }
        return super.hasShaftTowards(world, pos, state, face);
    }
    public boolean isController() {
        return this.isController;
    }
}

