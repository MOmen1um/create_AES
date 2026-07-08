package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

// Расширяем наш универсальный класс, чтобы забрать логику считывания имени блока и всю физику V8
public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    public ModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        // Прокидываем ровно 2 аргумента наверх, чтобы состыковаться с новым конструктором
        super(pos, state);
    }

    // Если в будущем для модульных ДВС тебе понадобится кастомная формула SU
    // или другое поведение, мы сможем переопределить методы прямо здесь!
}
