#include <jni.h>
#include <cmath>
#include <vector>
#include <algorithm>
#include <cstdint>

// Структура для хранения точки и её расстояния от эпицентра
struct ExplosionBlock {
    jint x, y, z;
    int32_t distSq;
};

extern "C" {
JNIEXPORT jintArray JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_calculateExplosionCrater(
    JNIEnv *env, jclass clazz, jint radius, jfloat roughness, jfloat phase1, jfloat phase2, jint seed
) {
    std::vector<ExplosionBlock> valid_blocks;
    valid_blocks.reserve((radius * 2 + 1) * (radius * 2 + 1) * (radius * 2 + 1) / 4);

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
                    valid_blocks.push_back({x, y, z, distSq});
                }
            }
        }
    }

    // ─── МАГИЯ СОРТИРОВКИ ───
    // Сортируем блоки: те, что ближе к центру (distSq меньше), будут в начале массива!
    std::sort(valid_blocks.begin(), valid_blocks.end(), [](const ExplosionBlock& a, const ExplosionBlock& b) {
        return a.distSq < b.distSq;
    });

    // Переносим отсортированные данные в плоский массив для Java
    jintArray result = env->NewIntArray(valid_blocks.size() * 3);
    if (result != nullptr) {
        std::vector<jint> flat_coords;
        flat_coords.reserve(valid_blocks.size() * 3);

        for (const auto& block : valid_blocks) {
            flat_coords.push_back(block.x);
            flat_coords.push_back(block.y);
            flat_coords.push_back(block.z);
        }

        env->SetIntArrayRegion(result, 0, flat_coords.size(), flat_coords.data());
    }

    return result;
}
}
