package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class NativeExplosionJNI {
    static {
        // Исправили путь в соответствии с твоей реальной структурой папок на CachyOS!
        System.load("/home/admin/Projects/Project_javaMod/create_AES/native/libcreate_aes_native.so");
    }

    // 1. Обновленный нативный метод. Теперь он принимает объект уровня, ссылку на метод проверки, эпицентр и мощность взрыва.
    public static native int initializeExplosion(
            int radius, float roughness, float phase1, float phase2, int seed,
            Level world, Object getResistanceMethod, int cx, int cy, int cz, float maxPower
    );

    public static native int fillExplosionBatch(int startOffset, int batchSize, int[] outArray);

    public static native void clearExplosionMemory();

    // 2. Ответный Java-метод (Callback), который наш C++ код будет дергать изнутри циклов через JNI!
    // Он берет реальный блок из мира Minecraft NeoForge 1.21.1 и возвращает его взрывоустойчивость.
    public static float getBlockResistance(Level level, int x, int y, int z) {
        if (level == null) return 0.0f;

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        // Получаем официальную взрывоустойчивость блока из движка Minecraft
        return state.getBlock().getExplosionResistance(state, level, pos, null);
    }
}
