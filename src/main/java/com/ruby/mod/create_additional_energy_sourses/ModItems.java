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


    // ==================== ПАКЕТ ПРЕДМЕТОВ ДЛЯ БЛОКОВ ДВС ====================

    // Алюминий
    public static final Supplier<Item> ALUMINUM_BLOCK_ITEM = ITEMS.register("aluminum_block",
            () -> new BlockItem(ModBlocks.ALUMINUM_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> ALUMINUM_STAIRS_ITEM = ITEMS.register("aluminum_stairs",
            () -> new BlockItem(ModBlocks.ALUMINUM_STAIRS.get(), new Item.Properties()));

    public static final Supplier<Item> ALUMINUM_SLAB_ITEM = ITEMS.register("aluminum_slab",
            () -> new BlockItem(ModBlocks.ALUMINUM_SLAB.get(), new Item.Properties()));

    // Титан
    public static final Supplier<Item> TITANIUM_BLOCK_ITEM = ITEMS.register("titanium_block",
            () -> new BlockItem(ModBlocks.TITANIUM_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> TITANIUM_STAIRS_ITEM = ITEMS.register("titanium_stairs",
            () -> new BlockItem(ModBlocks.TITANIUM_STAIRS.get(), new Item.Properties()));

    public static final Supplier<Item> TITANIUM_SLAB_ITEM = ITEMS.register("titanium_slab",
            () -> new BlockItem(ModBlocks.TITANIUM_SLAB.get(), new Item.Properties()));

    // Органы ДВС
    public static final Supplier<Item> ENGINE_VALVE_ITEM = ITEMS.register("engine_valve",
            () -> new BlockItem(ModBlocks.ENGINE_VALVE.get(), new Item.Properties()));

    public static final Supplier<Item> TURBOCHARGER_ITEM = ITEMS.register("turbocharger",
            () -> new BlockItem(ModBlocks.TURBOCHARGER.get(), new Item.Properties()));

    public static final Supplier<Item> CRANKSHAFT_ITEM = ITEMS.register("crankshaft",
            () -> new BlockItem(ModBlocks.CRANKSHAFT.get(), new Item.Properties()));
}
