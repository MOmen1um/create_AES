package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AluminumV8EngineBlock extends V8EngineBlock {
    public AluminumV8EngineBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Привязываем алюминиевые мозги!
        return ModBlocks.ALUMINUM_V8_ENGINE_ENTITY.get().create(pos, state);
    }
}
