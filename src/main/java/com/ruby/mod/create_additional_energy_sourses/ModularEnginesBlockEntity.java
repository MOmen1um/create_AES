package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;

public class ModularEnginesBlockEntity extends BlockEntity {

    // Классические полноразмерные параметры секций
    public int pistonCount = 8;
    public String engineMaterial = "iron";
    public String engineType = "v8";

    public ModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MODULAR_ENGINE_ENTITY.get(), pos, state);
        parseModularConfiguration();
    }

    private void parseModularConfiguration() {
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this.getBlockState().getBlock()).getPath().toLowerCase();

        // Полноразмерная фабрика парсинга (i4, v8, w16, r32)
        if (blockId.contains("inline4") || blockId.contains("i4")) {
            this.engineType = "i4";
            this.pistonCount = 4;
        } else if (blockId.contains("w16")) {
            this.engineType = "w16";
            this.pistonCount = 16;
        } else if (blockId.contains("radial32") || blockId.contains("r32")) {
            this.engineType = "r32";
            this.pistonCount = 32;
        } else {
            // Конфигурация по умолчанию — классический V8
            this.engineType = "v8";
            this.pistonCount = 8;
        }

        // Парсинг материала секции
        if (blockId.contains("titanium")) this.engineMaterial = "titanium";
        else if (blockId.contains("aluminum")) this.engineMaterial = "aluminum";
        else this.engineMaterial = "iron";
    }

    /**
     * Поиск Главного Управляющего Контроллера (CMEBE) по соосной линии вала (вперед).
     * Модули ищутся только вплотную по цепочке.
     */
    public ControllingModularEnginesBlockEntity findMasterController() {
        if (this.level == null) return null;
        BlockState state = this.getBlockState();
        if (!state.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING)) return null;

        Direction facing = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);

        // Идем строго ВПЕРЕД по направлению вала блока секции
        for (int i = 1; i <= 16; i++) {
            BlockPos checkPos = this.worldPosition.relative(facing, i);
            BlockEntity be = this.level.getBlockEntity(checkPos);

            // Если нашли контроллер и он соосен с нами — возвращаем его
            if (be instanceof ControllingModularEnginesBlockEntity controller) {
                if (controller.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING) == facing) {
                    return controller;
                }
            }

            // Если цепь прервалась чем-то, кроме другой соосной секции поршней, прекращаем поиск
            if (!(be instanceof ModularEnginesBlockEntity neighbor &&
                    neighbor.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING) == facing)) {
                break;
            }
        }
        return null;
    }
}