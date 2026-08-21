package com.ruby.mod.create_additional_energy_sourses;

public class NativeExplosionJNI {
    static {
        // Вместо loadLibrary используем load и пишем прямой путь к файлу на Linux!
        System.load("/home/admin/Project/create_AES/native/libcreate_aes_native.so");
    }

    public static native int initializeExplosion(int radius, float roughness, float phase1, float phase2, int seed);
    public static native int fillExplosionBatch(int startOffset, int batchSize, int[] outArray);
    public static native void clearExplosionMemory();
}
