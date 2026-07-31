package com.ruby.mod.create_additional_energy_sourses;

import com.ruby.mod.create_additional_energy_sourses.item.AdvancedPickaxeItem;
import com.ruby.mod.create_additional_energy_sourses.item.ModToolTiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModItems {
    // Твой правильный регистратор предметов
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Create_additional_energy_sourses.MODID);

    // Чугунные предметы (Iron)
    public static final DeferredHolder<Item, Item> IRON_I4_CARTER_ITEM = ITEMS.register("iron_i4_carter", () -> new BlockItem(ModBlocks.IRON_I4_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> IRON_V8_CARTER_ITEM = ITEMS.register("iron_v8_carter", () -> new BlockItem(ModBlocks.IRON_V8_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> IRON_W16_CARTER_ITEM = ITEMS.register("iron_w16_carter", () -> new BlockItem(ModBlocks.IRON_W16_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> IRON_R32_CARTER_ITEM = ITEMS.register("iron_r32_carter", () -> new BlockItem(ModBlocks.IRON_R32_CARTER.get(), new Item.Properties()));

    // Алюминиевые предметы (Aluminum)
    public static final DeferredHolder<Item, Item> ALUMINUM_I4_CARTER_ITEM = ITEMS.register("aluminum_i4_carter", () -> new BlockItem(ModBlocks.ALUMINUM_I4_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALUMINUM_V8_CARTER_ITEM = ITEMS.register("aluminum_v8_carter", () -> new BlockItem(ModBlocks.ALUMINUM_V8_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALUMINUM_W16_CARTER_ITEM = ITEMS.register("aluminum_w16_carter", () -> new BlockItem(ModBlocks.ALUMINUM_W16_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALUMINUM_R32_CARTER_ITEM = ITEMS.register("aluminum_r32_carter", () -> new BlockItem(ModBlocks.ALUMINUM_R32_CARTER.get(), new Item.Properties()));

    // Титановые предметы (Titanium)
    public static final DeferredHolder<Item, Item> TITANIUM_I4_CARTER_ITEM = ITEMS.register("titanium_i4_carter", () -> new BlockItem(ModBlocks.TITANIUM_I4_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_V8_CARTER_ITEM = ITEMS.register("titanium_v8_carter", () -> new BlockItem(ModBlocks.TITANIUM_V8_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_W16_CARTER_ITEM = ITEMS.register("titanium_w16_carter", () -> new BlockItem(ModBlocks.TITANIUM_W16_CARTER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_R32_CARTER_ITEM = ITEMS.register("titanium_r32_carter", () -> new BlockItem(ModBlocks.TITANIUM_R32_CARTER.get(), new Item.Properties()));


    // Твои старые предметы (инструменты)
    public static final Supplier<Item> ADVANCED_PICKAXE = ITEMS.register("advanced_pickaxe",
            () -> new AdvancedPickaxeItem(ModToolTiers.ADVANCED_TIER, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.ADVANCED_TIER, 1.0F, -2.8F))));
    public static final DeferredHolder<Item, Item> ADVANCED_PRECISION_MECHANISM = ITEMS.register("advanced_precision_mechanism", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> HEAVY_HANDLE = ITEMS.register("heavy_handle", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> PICKAXE_CORE = ITEMS.register("pickaxe_core", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> HEAVY_TIP = ITEMS.register("heavy_tip", () -> new Item(new Item.Properties()));
    // ==========================================
// КОМПОНЕНТЫ ДЛЯ СБОРКИ ДВИГАТЕЛЕЙ (РЕБАЛАНС)
// ==========================================

    // Поршни по металлам
    public static final DeferredHolder<Item, Item> ALUMINUM_PISTON = ITEMS.register("aluminum_piston", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_PISTON = ITEMS.register("titanium_piston", () -> new Item(new Item.Properties()));

    // Стандартные ГБЦ (Головки блока цилиндров)
    public static final DeferredHolder<Item, Item> ALUMINUM_GBC = ITEMS.register("aluminum_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_GBC = ITEMS.register("titanium_gbc", () -> new Item(new Item.Properties()));

    // Сдвоенные ГБЦ для твоего будущего радиального монстра R16
    public static final DeferredHolder<Item, Item> IRON_R16_GBC = ITEMS.register("iron_r16_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALUMINUM_R16_GBC = ITEMS.register("aluminum_r16_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_R16_GBC = ITEMS.register("titanium_r16_gbc", () -> new Item(new Item.Properties()));

    // ГБЦ с контроллерами (Мозги для финального шага V8, R16, R32)
    public static final DeferredHolder<Item, Item> IRON_BRAIN_GBC = ITEMS.register("iron_brain_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALUMINUM_BRAIN_GBC = ITEMS.register("aluminum_brain_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_BRAIN_GBC = ITEMS.register("titanium_brain_gbc", () -> new Item(new Item.Properties()));

    // Сдвоенные ГБЦ с контроллерами (Спецом под финал R16!)
    public static final DeferredHolder<Item, Item> IRON_R16_BRAIN_GBC = ITEMS.register("iron_r16_brain_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALUMINUM_R16_BRAIN_GBC = ITEMS.register("aluminum_r16_brain_gbc", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TITANIUM_R16_BRAIN_GBC = ITEMS.register("titanium_r16_brain_gbc", () -> new Item(new Item.Properties()));
    // Официальный предмет для термогенератора блока
    public static final java.util.function.Supplier<net.minecraft.world.item.Item> THERMO_GENERATOR_ITEM = ITEMS.register("thermo_generator",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.THERMO_GENERATOR.get(), new net.minecraft.world.item.Item.Properties()));
}
