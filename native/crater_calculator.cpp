#include <jni.h>
#include <cmath>
#include <vector>
#include <algorithm>
#include <cstdint>

// Структура для финального списка блоков взрыва
struct ExplosionBlock {
    jint x, y, z;
    int32_t distSq;
};

// Структура для блоков-щитов, которая была пропущена!
struct ShieldBlock {
    int32_t x, y, z;
    float dist;
};

// Глобальный вектор для пакетной передачи в Java
std::vector<ExplosionBlock> global_valid_blocks;

extern "C" {

// 1. Метод инициализации: считает, фильтрует конусы тени, сортирует и возвращает число блоков
JNIEXPORT jint JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_initializeExplosion(
    JNIEnv *env, jclass clazz, jint radius, jfloat roughness, jfloat phase1, jfloat phase2, jint seed,
    jobject world, jmethodID getResistanceMethod, jint cx, jint cy, jint cz, jfloat maxPower
) {
    global_valid_blocks.clear();

    // Временный вектор для чернового наброска сферы
    std::vector<ExplosionBlock> draft_blocks;
    draft_blocks.reserve((radius * 2 + 1) * (radius * 2 + 1) * (radius * 2 + 1) / 4);

    // ШАГ 1: Собираем рваную сферу с твоим тригонометрическим шумом
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
                    draft_blocks.push_back({x, y, z, distSq});
                }
            }
        }
    }

    // Сортируем блоки от центра к краям, чтобы волна шла последовательно
    std::sort(draft_blocks.begin(), draft_blocks.end(), [](const ExplosionBlock& a, const ExplosionBlock& b) {
        return a.distSq < b.distSq;
    });

    // Список встреченных блоков-щитов, остановивших волну
    std::vector<ShieldBlock> shields;

    // ШАГ 2: Фильтрация конусов тени и вычитание сил
    for (const auto& block : draft_blocks) {
        float dist = std::sqrt(static_cast<float>(block.distSq));

        // 2.1 Проверяем, не спрятался ли блок в тени равнобедренного треугольника/конуса
        bool isProtected = false;
        for (const auto& shield : shields) {
            if (dist > shield.dist) {
                // Скалярное произведение векторов
                float dotProduct = (block.x * shield.x + block.y * shield.y + block.z * shield.z) / (dist * shield.dist);

                // Фиксированный порог для идеальных ровных лучей от центра
                float halfBlockWidth = 0.5f;
                float fixedThreshold = std::cos(std::atan2(halfBlockWidth, shield.dist));

                if (dotProduct >= fixedThreshold) {
                    isProtected = true;
                    break;
                }
            }
        }

        // Если блок попал под защиту щита — вычеркиваем (просто не добавляем в финальный список)
        if (isProtected) {
            continue;
        }

        // 2.2 Расчет падения мощности и проверка взрывоустойчивости
        float currentPower = maxPower - (dist * 0.5f); // Затухание волны в воздухе

        // Запрашиваем у Java взрывоустойчивость текущего блока по абсолютным координатам
        jfloat resistance = env->CallFloatMethod(world, getResistanceMethod, cx + block.x, cy + block.y, cz + block.z);

        if (resistance > currentPower) {
            // Блок выстоял! Запоминаем его как новый щит
            shields.push_back({block.x, block.y, block.z, dist});
        } else {
            // Блок уничтожен — заносим в финальный массив удаления
            global_valid_blocks.push_back(block);
        }
    }

    return static_cast<jint>(global_valid_blocks.size());
}

// 2. Метод батчинга (остался без изменений, пишет в готовый Java-массив кусками)
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

    env->SetIntArrayRegion(outArray, 0, static_cast<jsize>(flat_coords.size()), flat_coords.data());
    return static_cast<jint>(flat_coords.size());
}

// 3. Метод очистки памяти после завершения взрыва
JNIEXPORT void JNICALL
Java_com_ruby_mod_create_1additional_1energy_1sourses_NativeExplosionJNI_clearExplosionMemory(
    JNIEnv *env, jclass clazz
) {
    global_valid_blocks.clear();
    global_valid_blocks.shrink_to_fit();
}

}
