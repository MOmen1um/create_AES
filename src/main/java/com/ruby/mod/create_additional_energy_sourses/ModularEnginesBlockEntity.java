package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    // Store the controller status inside the brain of our block
    private final boolean isController;

    // Fixed constructor that accepts 3 arguments from the block class
    public ModularEnginesBlockEntity(BlockPos pos, BlockState state, boolean isController) {
        super(pos, state);
        this.isController = isController;
    }

    // Getter to check if this block can open GUI and hold fuel
    public boolean isController() {
        return this.isController;
    }
    // FIX TRIGGER: Force Create API to recognize this block as a true modular entity!
}