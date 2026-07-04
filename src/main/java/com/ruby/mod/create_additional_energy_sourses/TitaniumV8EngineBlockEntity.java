package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TitaniumV8EngineBlockEntity extends V8EngineBlockEntity {
    public TitaniumV8EngineBlockEntity(BlockPos pos, BlockState state) {
        // Передаем уникальный тип энтити титана и его маркер материала
        super(ModBlocks.TITANIUM_V8_ENGINE_ENTITY.get(), pos, state, "titanium");
    }
}

