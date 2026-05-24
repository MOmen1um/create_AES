package com.ruby.mod.create_additional_energy_sourses; // Проверь, чтобы этот путь совпадал с твоим!

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrankshaftBlockEntity extends GeneratingKineticBlockEntity {

    // Переменные для хранения характеристик нашего мотора
    private int cylinderCount = 0;
    private boolean isTitaniumEngine = false;
    private boolean isAluminumEngine = false;

    // Изменили конструктор: теперь он принимает только позицию и блокстейт!
    public CrankshaftBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(type, pos, state);
    }

    // Метод, который принудительно пересчитывает структуру двигателя
    public void scanEngineStructure() {
        if (level == null || level.isClientSide || getBlockState() == null) return;

        // Сбрасываем старые значения
        cylinderCount = 0;
        isTitaniumEngine = false;
        isAluminumEngine = false;

        Direction facing = getBlockState().getValue(CrankshaftBlock.HORIZONTAL_FACING);

        // 3D-МАТРИЦА СКАНИРОВАНИЯ
        for (int yOffset = 1; yOffset <= 4; yOffset++) {
            for (int widthOffset = -2; widthOffset <= 2; widthOffset++) {
                BlockPos targetPos = worldPosition.above(yOffset).relative(facing.getClockWise(), widthOffset);
                BlockState checkState = level.getBlockState(targetPos);

                if (checkState.is(ModBlocks.TITANIUM_BLOCK.get())) {
                    cylinderCount++;
                    isTitaniumEngine = true;
                } else if (checkState.is(ModBlocks.ALUMINUM_BLOCK.get())) {
                    cylinderCount++;
                    isAluminumEngine = true;
                }
            }
        }

        // ВЫВОД РЕЗУЛЬТАТОВ ПРЯМО В ЧАТ ИГРЫ!
        if (cylinderCount > 0) {
            String motorType = isTitaniumEngine ? "§6Тяжелый Титановый" : "§bЛегкий Алюминиевый";

            level.players().forEach(player -> player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§e[ДВС] §aСтруктура обнаружена! Тип: " + motorType + "§a, Мощность блоков: §e" + cylinderCount)
            ));
        } else {
            level.players().forEach(player -> player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§e[ДВС] §cДвигатель не собран! Поставьте поршни сверху.")
            ));
        }

        updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        // Если структура не собрана или нет блоков — вал стоит на месте (0 RPM)
        if (cylinderCount <= 0) return 0;

        // ЛОГИКА ОБОРОТОВ (RPM):
        // Алюминиевый мотор выдает бешеные 256 RPM (внутренние 25 000)
        if (isAluminumEngine) return 256.0f;

        // Титановый мотор медленнее, но мощнее — выдает 64 RPM
        if (isTitaniumEngine) return 64.0f;

        return 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (cylinderCount <= 0) return 0;

        // ЛОГИКА СТРЕСС-ЕМКОСТИ:
        // Каждый найденный блок титана дает огромный крутящий момент (по 512 единиц стресса!)
        if (isTitaniumEngine) {
            return cylinderCount * 512.0f;
        }

        // Алюминий дает меньше мощности, но больше скорости (по 128 единиц за блок)
        if (isAluminumEngine) {
            return cylinderCount * 128.0f;
        }

        return 0;
    }
}