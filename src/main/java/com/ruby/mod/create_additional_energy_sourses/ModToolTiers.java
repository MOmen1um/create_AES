package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {
    private static ModBlocks ModItems;
    public static final Tier ADVANCED_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Уровень добычи (незерит)
            2500, // Прочность
            10.0f, // Скорость копания (быстрее алмаза)
            4.0f, // Урон
            20, // Зачаровываемость
            () -> Ingredient.of(ModItems.ADVANCED_PRECISION_MECHANISM.get()) // Чем чинить
    );
}
