package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class IronV8EngineBlockEntity extends V8EngineBlockEntity {
    public IronV8EngineBlockEntity(BlockPos pos, BlockState state) {
        // Передаем чугунный тип энтити и жесткий маркер материала "iron"
        super(ModBlocks.V8_ENGINE_ENTITY.get(), pos, state, "iron");
    }
}
