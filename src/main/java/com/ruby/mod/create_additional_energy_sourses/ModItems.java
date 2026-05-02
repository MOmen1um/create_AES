package com.ruby.mod.create_additional_energy_sourses;

import com.ruby.mod.create_additional_energy_sourses.item.AdvancedPickaxeItem;
import com.ruby.mod.create_additional_energy_sourses.item.ModToolTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("create_additional_energy_sourses");

    public static final DeferredItem<Item> ADVANCED_PICKAXE = ITEMS.register("advanced_pickaxe",
            () -> new AdvancedPickaxeItem(ModToolTiers.ADVANCED_TIER, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.ADVANCED_TIER, 1.5F, -2.8F))
                    // Явно указываем уровень зачарования из нашего тира
                    .enchantable(ModToolTiers.ADVANCED_TIER.getEnchantmentValue())
            ));

    public static final DeferredItem<Item> HEAVY_HANDLE = ITEMS.register("heavy_handle",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> PICKAXE_CORE = ITEMS.register("pickaxe_core",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> HEAVY_TIP = ITEMS.register("heavy_tip",
            () -> new Item(new Item.Properties()));
}