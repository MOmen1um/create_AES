package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;

public class ControllingModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    public String dynamicModularName = "Modular Engine";

    // Базовые параметры самого блока контроллера (если он стоит один)
    private int basePistonCount = 8;
    private String baseMaterial = "iron";

    public ControllingModularEnginesBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONTROLLING_MODULAR_ENGINE_ENTITY.get(), pos, state);
        parseOwnConfiguration();
    }

    // Считываем, чем является сам контроллер изначально (например, чугунный v8 или титановый r32)
    private void parseOwnConfiguration() {
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this.getBlockState().getBlock()).getPath().toLowerCase();

        if (blockId.contains("i4")) { this.basePistonCount = 4; this.engineType = "i4"; }
        else if (blockId.contains("w16")) { this.basePistonCount = 16; this.engineType = "w16"; }
        else if (blockId.contains("r32")) { this.basePistonCount = 32; this.engineType = "r32"; }
        else { this.basePistonCount = 8; this.engineType = "v8"; }

        if (blockId.contains("titanium")) this.baseMaterial = "titanium";
        else if (blockId.contains("aluminum")) this.baseMaterial = "aluminum";
        else this.baseMaterial = "iron";

        // Задаем начальные значения
        this.pistonCount = this.basePistonCount;
        this.engineMaterial = this.baseMaterial;
    }

    public void calculateConfiguration() {
        if (this.level == null || this.level.isClientSide) return;

        BlockState state = this.getBlockState();
        if (!state.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING)) return;

        Direction facing = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        Direction backDirection = facing.getOpposite();

        // Начинаем расчет с собственных поршней контроллера!
        int totalPistons = this.basePistonCount;
        int additionalModules = 0;

        BlockPos currentCheckPos = this.worldPosition.relative(backDirection);

        // Бежим назад и добавляем поршни от расширений, если они есть
        while (this.level.getBlockEntity(currentCheckPos) instanceof ModularEnginesBlockEntity modularSection) {
            if (modularSection.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING) == facing) {
                totalPistons += modularSection.pistonCount;
                additionalModules++;
                currentCheckPos = currentCheckPos.relative(backDirection);
            } else {
                break;
            }
        }

        // Записываем итоговое количество поршней в ядро симуляции ДВС
        this.pistonCount = totalPistons;
        this.engineMaterial = this.baseMaterial; // Материал берется от самого контроллера
        this.engineType = "modular";

        // Красивый тултип
        String matName = this.baseMaterial.toUpperCase();
        if (additionalModules > 0) {
            this.dynamicModularName = String.format("Modular %s ДВС (+%d Мод. | Всего поршней: %d)",
                    matName, additionalModules, totalPistons);
        } else {
            this.dynamicModularName = String.format("Modular %s ДВС (Автономный | Поршней: %d)",
                    matName, totalPistons);
        }

        // Обновляем Create
        this.updateGeneratedRotation();
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
    }
}
