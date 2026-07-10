package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlock extends V8EngineBlock {

    private final boolean isController;

    public ModularEnginesBlock(Properties properties) {
        super(properties);
        this.isController = false;
    }

    public ModularEnginesBlock(Properties properties, boolean isController) {
        super(properties);
        this.isController = isController;
    }

    // ХАК ДЛЯ КРАША: Явно говорим Create, какой ТИП сущности привязан к этому блоку!
    // Этот метод ищет внутренний движок Create при проверке чанков
    public BlockEntityType<? extends ModularEnginesBlockEntity> getBlockEntityType() {
        return ModBlocks.MODULAR_ENGINE_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModularEnginesBlockEntity(pos, state, this.isController);
    }

    public boolean isController() {
        return this.isController;
    }
}


