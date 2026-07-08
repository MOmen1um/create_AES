package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // 1. Базовый хитбокс шириной и длиной в 1 блок (16х16 пикселей)
        double minX = 0.0D;
        double minZ = 0.0D;
        double maxX = 1.0D;
        double maxZ = 1.0D;

        // Стандартная высота по умолчанию — 1 полная сетка блока (16 пикселей)
        double maxY = 1.0D;

        // 2. Проверяем тип двигателя из BlockState (если у тебя тип записан в проперти блока)
        // Либо, если у тебя разные типы — это разные объекты блоков в ModBlocks, можно проверять по имени:
        String blockName = state.getBlock().toString().toLowerCase();

        if (blockName.contains("w16")) {
            // Поднимаем хитбокс на 1 пиксель вверх (17 пикселей в высоту)
            maxY = 1.0D + (1.0D / 16.0D); // 1.0625D
        } else if (blockName.contains("r32") || blockName.contains("radial")) {
            // Поднимаем хитбокс на 2 пикселя вверх (18 пикселей в высоту)
            maxY = 1.0D + (2.0D / 16.0D); // 1.125D
        }

        return Shapes.box(minX, 0.0D, minZ, maxX, maxY, maxZ);
    }
}


