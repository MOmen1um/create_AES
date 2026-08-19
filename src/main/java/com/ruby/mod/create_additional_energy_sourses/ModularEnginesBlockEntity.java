package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlockEntity extends BlockEntity {
    private boolean isController = false;

    public ModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MODULAR_ENGINE_ENTITY.get(), pos, state);
    }

    // Конструктор с тремя аргументами, который вызывается в newBlockEntity()
    public ModularEnginesBlockEntity(BlockPos pos, BlockState state, boolean isController) {
        super(ModBlocks.MODULAR_ENGINE_ENTITY.get(), pos, state);
        this.isController = isController;
    }

    // Заглушка метода setController
    public void setController(boolean isController) {
        this.isController = isController;
    }

    // Заглушка метода getController
    public ModularEnginesBlockEntity getController() {
        return this.isController ? this : null;
    }

    // Заглушка метода updateEngineStructure
    public void updateEngineStructure() {
        // Временно ничего не делаем, курс на взрывы!
    }
}
