#include <jni.h>
#include <cmath>
#include <vector>
#include <cstdint>

extern "C" {
JNIEXPORT jintArray JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_calculateExplosionCrater(
    JNIEnv *env, jclass clazz, jint radius, jfloat roughness, jfloat phase1, jfloat phase2, int seed
) {
    // Вектор для хранения относительных координат [x1, y1, z1, ...]
    std::vector<jint> affected_blocks;
    affected_blocks.reserve((radius * 2 + 1) * (radius * 2 + 1) * (radius * 2 + 1) / 4);

    // Оптимизированный расчет кратера (тригонометрия)
    for (int32_t x = -radius; x <= radius; x++) {
        for (int32_t y = -radius; y <= radius; y++) {
            for (int32_t z = -radius; z <= radius; z++) {
                int32_t distSq = x * x + y * y + z * z;
                float dist = std::sqrt(static_cast<float>(distSq));
                if (dist < 0.1f) dist = 0.1f;

                float nx = x / dist, ny = y / dist, nz = z / dist;
                float noise = std::sin(nx * 1.5f + phase1) * std::cos(ny * 1.2f + phase2) * std::sin(nz * 1.7f);
                float modifiedRadius = radius + (noise * roughness);

                if (distSq <= modifiedRadius * modifiedRadius) {
                    affected_blocks.push_back(x); affected_blocks.push_back(y); affected_blocks.push_back(z);
                }
            }
        }
    }

    // Возврат массива в JVM
    jintArray result = env->NewIntArray(affected_blocks.size());
    if (result != nullptr) env->SetIntArrayRegion(result, 0, affected_blocks.size(), affected_blocks.data());
    return result;
}
}
