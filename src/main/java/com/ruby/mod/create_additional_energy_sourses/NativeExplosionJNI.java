package com.ruby.mod.create_additional_energy_sourses;

public class NativeExplosionJNI {
    static {
        // Загружаем скомпилированную .so (Linux) или .dll (Windows) библиотеку
        try {
            System.loadLibrary("create_aes_native");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("⚠ Не удалось загрузить нативную библиотеку взрывов: " + e.getMessage());
        }
    }

    // Тот самый нативный метод
    public static native int[] calculateExplosionCrater(int radius, float roughness, float phase1, float phase2, int seed);
}
