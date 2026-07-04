package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BaseRadiatorBlock extends Block implements EntityBlock {

    private final java.util.function.Supplier<BlockEntityType<? extends BaseRadiatorBlockEntity>> entityType;

    public BaseRadiatorBlock(Properties properties, java.util.function.Supplier<BlockEntityType<? extends BaseRadiatorBlockEntity>> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.entityType.get().create(pos, state);
    }
}