package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IronV8EngineBlock extends V8EngineBlock {
    public IronV8EngineBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Привязываем базовую (чугунную) сущность
        return ModBlocks.V8_ENGINE_ENTITY.get().create(pos, state);
    }
}
