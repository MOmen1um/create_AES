package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Наследуемся НАПРЯМУЮ от твоего идеального рабочего класса Блока!
public class NonModularEnginesBlock extends V8EngineBlock {

    public NonModularEnginesBlock(Properties properties) {
        // Прокидываем свойства в родительский конструктор эталона
        super(properties);
    }

    // Переопределяем только создание сущности, чтобы блок спавнил наш универсальный класс
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NonModularEnginesBlockEntity(pos, state);
    }
}


