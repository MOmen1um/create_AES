package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Наследуемся напрямую от твоего идеального рабочего класса Блока!
public class ModularEnginesBlock extends V8EngineBlock {

    public ModularEnginesBlock(Properties properties) {
        // Прокидываем свойства в родительский конструктор эталона (строго 1 аргумент!)
        super(properties);
    }

    // Спавним нашу модульную сущность
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModularEnginesBlockEntity(pos, state);
    }
}

