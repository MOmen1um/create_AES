package com.ruby.mod.create_additional_energy_sourses.item;

import com.ruby.mod.create_additional_energy_sourses.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.minecraft.tags.BlockTags;

public class ModToolTiers {
    public static final Tier ADVANCED_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Тир 4 (как незерит)
            5000, // Прочность
            40.0f, // Скорость
            4.0f, // Урон
            25, // Зачаровываемость
            () -> Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT) // Пока починка незеритом
    );
}