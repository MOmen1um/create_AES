package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    public ModularEnginesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String material, String engineType) {
        super(type, pos, state, material, engineType);
    }
}
