package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.ruby.mod.create_additional_energy_sourses.ModBlocks.ITEMS;

public class ModItems {
    public static final DeferredItem<Item> ADVANCED_PICKAXE = ITEMS.register("advanced_pickaxe",
        () -> new com.ruby.mod.create_additional_energy_sourses.item.AdvancedPickaxeItem(ModToolTiers.ADVANCED_TIER, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.ADVANCED_TIER, 1.5F, -2.8F))));
}
