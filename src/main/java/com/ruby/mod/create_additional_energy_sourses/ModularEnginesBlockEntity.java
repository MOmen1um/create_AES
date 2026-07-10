package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    // Store the controller status inside the brain of our block
    private final boolean isController;

    // Fixed constructor that accepts 3 arguments from the block class
    public ModularEnginesBlockEntity(BlockPos pos, BlockState state, boolean isController) {
        super(pos, state);
        this.isController = isController;
    }

    // Getter to check if this block can open GUI and hold fuel
    public boolean isController() {
        return this.isController;
    }
    // FIX TRIGGER: Force Create API to recognize this block as a true modular entity!
    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<?> getType() {
        // Return your exact registered modular entity holder!
        return ModBlocks.MODULAR_ENGINE_ENTITY.get();
    }
    @Override
    public boolean addToGogglesTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        // 1. Позволяем родителю полностью собрать всю упитанную телеметрию
        boolean appendDefault = super.addToGogglesTooltip(tooltip, isPlayerSneaking);

        // 2. Исправляем заголовок спецификации под индексом 1
        if (tooltip.size() > 1) {
            String blockPath = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getKey(this.getBlockState().getBlock()).getPath().toUpperCase();

            String specName = "Модульный ДВС";
            if (blockPath.contains("I2")) specName = "Модульный I2 (Рядный)";
            if (blockPath.contains("V4")) specName = "Модульный V4 (V-образный)";
            if (blockPath.contains("W8")) specName = "Модульный W8 (W-образный)";
            if (blockPath.contains("R16")) specName = "Модульный R16 (Радиальный)";

            if (this.isController()) {
                specName += " [КОНТРОЛЛЕР]";
            }

            // Заменяем старую плашку "V8 Engine" на имя нашей новой конфигурации
            tooltip.set(1, net.minecraft.network.chat.Component.literal("⚙ Спецификация: " + specName)
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
        }

        return appendDefault;
    }
}