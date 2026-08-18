package com.ruby.mod.create_additional_energy_sourses;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ModPonderStoryboards {

    // 🏎️ Сцена 1: Базовая кинетика, вал и интерфейс двигателя
    public static void baseEngineScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("engine_base", "Основы ДВС: Мощность и Интерфейс");
        scene.configureBasePlate(0, 0, 8);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos enginePos = util.grid().at(4, 1, 4);
        // Вместо одного блока мы выбираем всю область схемы от (0,0,0) до (8,5,8)
        scene.world().showSection(util.select().cuboid(new BlockPos(0, 0, 0), new BlockPos(8, 5, 8)), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(60)
                .text("Каждый двигатель оснащен встроенным выходным валом спереди для передачи SU.")
                .pointAt(util.vector().topOf(enginePos))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Используйте колесико мыши на ползунке корпуса, чтобы настроить целевую скорость.")
                .pointAt(util.vector().centerOf(enginePos))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    // ❄️ Сцена 2: Смысл радиаторов и правильная стыковка
    public static void radiatorSetupScene(SceneBuilder scene, SceneBuildingUtil util) {
        // Изменяем первый аргумент строго на "radiator_setup", так как файл называется radiator_setup.nbt!
        scene.title("radiator_setup", "Анатомия охлаждения: Правильная установка");
        scene.configureBasePlate(0, 0, 8);
        scene.showBasePlate();

// Показываем всю схему радиатора и двигателя сразу
        scene.world().showSection(util.select().cuboid(new BlockPos(0, 0, 0), new BlockPos(8, 5, 8)), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(60)
                .text("Радиатор ОБЯЗАН быть направлен строго лицевой стороной к моторному отсеку.");
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Если развернуть его боком или спиной — соединение разорвется, и охлаждение прекратится.")
                .placeNearTarget();
        scene.idle(70);
        scene.markAsFinished();
    }

    // 🚀 Сцена 3: Бешеный концепт скорости (Без лимитов RPM)
    public static void infiniteSpeedScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("infinite_speed", "Концепция безлимитной скорости");
        scene.configureBasePlate(0, 0, 8);
        scene.showBasePlate();

        BlockPos enginePos = util.grid().at(4, 1, 4);
        // Показываем всю постройку концепции скорости
        scene.world().showSection(util.select().cuboid(new BlockPos(0, 0, 0), new BlockPos(8, 5, 8)), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(80)
                .text("У этих двигателей НЕТ программного лимита скорости. Вы можете выкручивать RPM на максимум!")
                .pointAt(util.vector().topOf(enginePos));
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Однако помните: чем выше обороты, тем быстрее растет температура ядра. Ограничитель — только плавление металлов.")
                .placeNearTarget();
        scene.idle(80);
        scene.markAsFinished();
    }

    // 💀 Сцена 4: Подключение труб и Ядерный перегрев
    public static void coolingAndMeltingScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("engine_catastrophe", "Гидравлика и последствия перегрева");
        scene.configureBasePlate(0, 0, 8);
        scene.showBasePlate();

        // Показываем всю постройку целиком, чтобы трубы и ядро выкатились вместе
        scene.world().showSection(util.select().cuboid(new BlockPos(0, 0, 0), new BlockPos(8, 5, 8)), Direction.DOWN);
        scene.idle(20);

        BlockPos radiatorPos = util.grid().at(4, 1, 4);

        scene.overlay().showText(60)
                .text("Для непрерывной работы радиатора подведите к нему трубы с водой из Create.")
                .pointAt(util.vector().centerOf(radiatorPos));
        scene.idle(70);

        scene.overlay().showText(90)
                .text("Если помпа остановится, прогрессивное испарение осушит бак за считанные секунды...")
                .pointAt(util.vector().centerOf(radiatorPos));
        scene.idle(100);

        scene.overlay().showText(100)
                .text("Достижение критической температуры вызовет детонацию ядра. Мощность взрыва сотрет весь ваш завод! Следите за телеметрией. 💀")
                .placeNearTarget();
        scene.idle(110);
        scene.markAsFinished();
    }
}