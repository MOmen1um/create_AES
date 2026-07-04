package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AluminumV8EngineBlockEntity extends V8EngineBlockEntity {
    public AluminumV8EngineBlockEntity(BlockPos pos, BlockState state) {
        // Передаем уникальный тип энтити алюминия и его маркер материала
        super(ModBlocks.ALUMINUM_V8_ENGINE_ENTITY.get(), pos, state, "aluminum");
    }
}
