package com.ruby.mod.create_additional_energy_sourses;

import com.ruby.mod.create_additional_energy_sourses.item.AdvancedPickaxeItem;
import com.ruby.mod.create_additional_energy_sourses.item.ModToolTiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModItems {
    // Твой правильный регистратор предметов
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Create_additional_energy_sourses.MODID);


    // Твои старые предметы (инструменты)
    public static final Supplier<Item> ADVANCED_PICKAXE = ITEMS.register("advanced_pickaxe",
            () -> new AdvancedPickaxeItem(ModToolTiers.ADVANCED_TIER, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.ADVANCED_TIER, 1.0F, -2.8F))));
    public static final Supplier<Item> HEAVY_HANDLE = ITEMS.register("heavy_handle", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> PICKAXE_CORE = ITEMS.register("pickaxe_core", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> HEAVY_TIP = ITEMS.register("heavy_tip", () -> new Item(new Item.Properties()));
    // Официальный предмет для термогенератора блока
    public static final java.util.function.Supplier<net.minecraft.world.item.Item> THERMO_GENERATOR_ITEM = ITEMS.register("thermo_generator",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.THERMO_GENERATOR.get(), new net.minecraft.world.item.Item.Properties()));
}
