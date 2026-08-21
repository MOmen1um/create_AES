#include <jni.h>
#include <cmath>
#include <vector>
#include <algorithm>
#include <cstdint>

struct ExplosionBlock {
    jint x, y, z;
    int32_t distSq;
};

// Глобальный вектор, который будет временно хранить результат в памяти C++
// Благодаря std::vector, память очистится автоматически, когда мы его принудительно очистим
std::vector<ExplosionBlock> global_valid_blocks;

extern "C" {

// 1. Метод инициализации: считает, сортирует и возвращает ОБЩЕЕ число блоков
JNIEXPORT jint JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_initializeExplosion(
    JNIEnv *env, jclass clazz, jint radius, jfloat roughness, jfloat phase1, jfloat phase2, jint seed
) {
    global_valid_blocks.clear(); // Очищаем старые данные на всякий случай
    global_valid_blocks.reserve((radius * 2 + 1) * (radius * 2 + 1) * (radius * 2 + 1) / 4);

    for (int32_t x = -radius; x <= radius; x++) {
        for (int32_t y = -radius; y <= radius; y++) {
            for (int32_t z = -radius; z <= radius; z++) {
                int32_t distSq = x * x + y * y + z * z;
                float dist = std::sqrt(static_cast<float>(distSq));
                if (dist < 0.1f) dist = 0.1f;

                float nx = x / dist;
                float ny = y / dist;
                float nz = z / dist;

                float noise = std::sin(nx * 1.5f + phase1) * std::cos(ny * 1.2f + phase2) * std::sin(nz * 1.7f);
                float modifiedRadius = static_cast<float>(radius) + (noise * roughness);

                if (distSq <= modifiedRadius * modifiedRadius) {
                    global_valid_blocks.push_back({x, y, z, distSq});
                }
            }
        }
    }

    // Сортировка от центра
    std::sort(global_valid_blocks.begin(), global_valid_blocks.end(), [](const ExplosionBlock& a, const ExplosionBlock& b) {
        return a.distSq < b.distSq;
    });

    return static_cast<jint>(global_valid_blocks.size());
}

JNIEXPORT jint JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_fillExplosionBatch(
    JNIEnv *env, jclass clazz, jint startOffset, jint batchSize, jintArray outArray
) {
    jint totalSize = static_cast<jint>(global_valid_blocks.size());
    if (startOffset < 0 || startOffset >= totalSize || batchSize <= 0 || outArray == nullptr) {
        return 0;
    }

    jint actualBlocks = batchSize;
    if (startOffset + batchSize > totalSize) {
        actualBlocks = totalSize - startOffset;
    }

    // Заполняем временный вектор данными
    std::vector<jint> flat_coords;
    flat_coords.reserve(actualBlocks * 3);

    for (jint i = 0; i < actualBlocks; i++) {
        size_t vectorIndex = static_cast<size_t>(startOffset + i);
        if (vectorIndex < global_valid_blocks.size()) {
            const auto& block = global_valid_blocks[vectorIndex];
            flat_coords.push_back(block.x);
            flat_coords.push_back(block.y);
            flat_coords.push_back(block.z);
        }
    }

    // ВНИМАНИЕ: Мы НЕ создаем NewIntArray. Мы пишем поверх старого массива Java!
    env->SetIntArrayRegion(outArray, 0, static_cast<jsize>(flat_coords.size()), flat_coords.data());

    // Возвращаем количество РЕАЛЬНО записанных координат (чисел, а не блоков, то есть блоков * 3)
    return static_cast<jint>(flat_coords.size());
}


// 3. Метод очистки: освобождает память C++ после завершения взрыва
JNIEXPORT void JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_clearExplosionMemory(
    JNIEnv *env, jclass clazz
) {
    global_valid_blocks.clear();
    global_valid_blocks.shrink_to_fit(); // Намертво вычищает ОЗУ на стороне C++
}

}