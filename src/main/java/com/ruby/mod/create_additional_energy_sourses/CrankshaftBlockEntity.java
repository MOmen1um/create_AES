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
    public CrankshaftBlockEntity(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        // Передаем в super тип сущности напрямую через ModBlocks!
        super(ModBlocks.CRANKSHAFT_ENTITY.get(), pos, state);
    }

    // Метод, который принудительно пересчитывает структуру двигателя
    public void scanEngineStructure() {
        if (level == null || level.isClientSide || getBlockState() == null) return;

        // Сбрасываем старые значения перед новым сканированием
        cylinderCount = 0;
        isTitaniumEngine = false;
        isAluminumEngine = false;

        // Узнаем, куда повернут коленвал (вдоль какой оси он лежит)
        Direction facing = getBlockState().getValue(CrankshaftBlock.HORIZONTAL_FACING);

        System.out.println(">>> НАЧАЛО СКАНРИОВАНИЯ ДВС <<<");

        // 3D-МАТРИЦА СКАНИРОВАНИЯ:
        // Перебираем блоки над коленвалом.
        // Ось Y (высота): смотрим на 4 блока вверх над валом
        for (int yOffset = 1; yOffset <= 4; yOffset++) {
            // Ось X/Z (ширина): заглядываем на 2 блока влево и вправо
            for (int widthOffset = -2; widthOffset <= 2; widthOffset++) {

                // Считаем точную координату проверяемой точки в 3D-пространстве
                BlockPos targetPos;
                if (facing.getAxis() == Direction.Axis.X) {
                    // Если вал лежит вдоль X, то ширина двигателя расширяется по оси Z
                    targetPos = worldPosition.above(yOffset).relative(facing.getClockWise(), widthOffset);
                } else {
                    // If вал лежит вдоль Z, ширина расширяется по оси X
                    targetPos = worldPosition.above(yOffset).relative(facing.getClockWise(), widthOffset);
                }

                // Читаем блок по этому адресу
                BlockState checkState = level.getBlockState(targetPos);

                // ПРОВЕРКА МАТЕРИАЛА: Ищем наши универсальные блоки
                if (checkState.is(ModBlocks.TITANIUM_BLOCK.get())) {
                    cylinderCount++;
                    isTitaniumEngine = true;
                } else if (checkState.is(ModBlocks.ALUMINUM_BLOCK.get())) {
                    cylinderCount++;
                    isAluminumEngine = true;
                }
            }
        }

        System.out.println(">>> СКАН ЗАВЕРШЕН! Найдено блоков ДВС: " + cylinderCount);
        if (isTitaniumEngine) System.out.println(">>> Тип мотора: Тяжелый Титановый");
        if (isAluminumEngine) System.out.println(">>> Тип мотора: Легкий Алюминиевый");

        // Обновляем вращение в сети Create на основе собранной структуры
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