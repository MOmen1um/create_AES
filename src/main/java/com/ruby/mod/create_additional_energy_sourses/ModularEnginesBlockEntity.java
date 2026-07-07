package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

// Наследуемся от нашего универсального класса из папки Setup
public class ModularEnginesBlockEntity extends com.ruby.mod.create_additional_energy_sourses.NonModularEnginesBlockEntity {

    public ModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        // Передаем ровно 2 аргумента в супер-конструктор (как он теперь и просит)!
        super(pos, state);
    }
}
