package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Create_additional_energy_sourses.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Create_additional_energy_sourses.MODID);

    // Оставлены блоки Термогенераторов и Радиаторов
    // 1. Блок Термогенератора
    public static final DeferredHolder<Block, ThermoGeneratorBlock> THERMO_GENERATOR =
            BLOCKS.register("thermo_generator", () -> new ThermoGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));


    // Наш эталонный стабильный мотор для тестов и сравнения
    public static final net.neoforged.neoforge.registries.DeferredHolder<Block, Block> V8_ENGINE =
            BLOCKS.register("v8_engine_classic", () -> new V8EngineBlock(BlockBehaviour.Properties.of()
                    .strength(4.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // Отдельный тип сущности для классического эталона
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<V8EngineBlockEntity>> V8_ENGINE_ENTITY =
            BLOCK_ENTITIES.register("v8_engine_classic_entity", () -> BlockEntityType.Builder.of(
                    V8EngineBlockEntity::new,
                    V8_ENGINE.get()
            ).build(null));

    // 2. Предмет Термогенератора
    public static final DeferredHolder<Item, BlockItem> THERMO_GENERATOR_ITEM =
            ModItems.ITEMS.register("thermo_generator_item", () -> new BlockItem(THERMO_GENERATOR.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermoGeneratorBlockEntity>> THERMO_GEN_ENTITY =
            BLOCK_ENTITIES.register("thermo_generator", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new ThermoGeneratorBlockEntity(ModBlocks.THERMO_GEN_ENTITY.get(), pos, state),
                    THERMO_GENERATOR.get()).build(null));

    public static final DeferredHolder<Block, Block> RADIATOR_COPPER = BLOCKS.register("radiator_copper",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.COPPER), () -> ModBlocks.RADIATOR_COPPER_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_COPPER_ITEM = ModItems.ITEMS.register("radiator_copper",
            () -> new BlockItem(RADIATOR_COPPER.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> RADIATOR_BRASS = BLOCKS.register("radiator_brass",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.NETHER_GOLD_ORE), () -> ModBlocks.RADIATOR_BRASS_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_BRASS_ITEM = ModItems.ITEMS.register("radiator_brass",
            () -> new BlockItem(RADIATOR_BRASS.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> RADIATOR_STEEL = BLOCKS.register("radiator_steel",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.METAL), () -> ModBlocks.RADIATOR_STEEL_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_STEEL_ITEM = ModItems.ITEMS.register("radiator_steel",
            () -> new BlockItem(RADIATOR_STEEL.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> RADIATOR_ULTIMATE = BLOCKS.register("radiator_ultimate",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(6.0f).sound(SoundType.METAL), () -> ModBlocks.RADIATOR_ULTIMATE_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_ULTIMATE_ITEM = ModItems.ITEMS.register("radiator_ultimate",
            () -> new BlockItem(RADIATOR_ULTIMATE.get(), new Item.Properties()));


    // --- 2. ИСПРАВЛЕННАЯ РЕГИСТРАЦИЯ СУЩНОСТЕЙ (BLOCK ENTITIES) ---

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_COPPER_ENTITY =
            BLOCK_ENTITIES.register("radiator_copper_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_COPPER_ENTITY.get(), pos, state), ModBlocks.RADIATOR_COPPER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_BRASS_ENTITY =
            BLOCK_ENTITIES.register("radiator_brass_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_BRASS_ENTITY.get(), pos, state), ModBlocks.RADIATOR_BRASS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_STEEL_ENTITY =
            BLOCK_ENTITIES.register("radiator_steel_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_STEEL_ENTITY.get(), pos, state), ModBlocks.RADIATOR_STEEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_ULTIMATE_ENTITY =
            BLOCK_ENTITIES.register("radiator_ultimate_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_ULTIMATE_ENTITY.get(), pos, state), ModBlocks.RADIATOR_ULTIMATE.get()).build(null));



    // ==========================================
    // 2. РЕГИСТРАЦИЯ СЕТКИ МОТОРОВ (Используем метод-хелпер)
    // ==========================================

    // НЕМОДУЛЬНЫЕ (IRON, ALUMINUM, TITANIUM)
    // Чугунные (Iron)
    public static final DeferredHolder<Block, Block> IRON_I4 = registerEngine("i4_engine", "iron", "i4", false);
    public static final DeferredHolder<Block, Block> IRON_V8 = registerEngine("v8_engine", "iron", "v8", false);
    public static final DeferredHolder<Block, Block> IRON_W16 = registerEngine("w16_engine", "iron", "w16", false);
    public static final DeferredHolder<Block, Block> IRON_R32 = registerEngine("r32_engine", "iron", "r32", false);

    // Алюминиевые (Aluminum)
    public static final DeferredHolder<Block, Block> ALUMINUM_I4 = registerEngine("aluminum_i4_engine", "aluminum", "i4", false);
    public static final DeferredHolder<Block, Block> ALUMINUM_V8 = registerEngine("aluminum_v8_engine", "aluminum", "v8", false);
    public static final DeferredHolder<Block, Block> ALUMINUM_W16 = registerEngine("aluminum_w16_engine", "aluminum", "w16", false);
    public static final DeferredHolder<Block, Block> ALUMINUM_R32 = registerEngine("aluminum_r32_engine", "aluminum", "r32", false);

    // Титановые (Titanium)
    public static final DeferredHolder<Block, Block> TITANIUM_I4 = registerEngine("titanium_i4_engine", "titanium", "i4", false);
    public static final DeferredHolder<Block, Block> TITANIUM_V8 = registerEngine("titanium_v8_engine", "titanium", "v8", false);
    public static final DeferredHolder<Block, Block> TITANIUM_W16 = registerEngine("titanium_w16_engine", "titanium", "w16", false);
    public static final DeferredHolder<Block, Block> TITANIUM_R32 = registerEngine("titanium_r32_engine", "titanium", "r32", false);

    // МОДУЛЬНЫЕ (IRON, ALUMINUM, TITANIUM)
    // Модульные чугунные
    public static final DeferredHolder<Block, Block> MODULAR_IRON_I4 = registerEngine("modular_iron_i4_engine", "iron", "i4", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_V8 = registerEngine("modular_iron_v8_engine", "iron", "v8", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_W16 = registerEngine("modular_iron_w16_engine", "iron", "w16", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_R32 = registerEngine("modular_iron_r32_engine", "iron", "r32", true);

    // Модульные алюминиевые
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_I4 = registerEngine("modular_aluminum_i4_engine", "aluminum", "i4", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_V8 = registerEngine("modular_aluminum_v8_engine", "aluminum", "v8", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_W16 = registerEngine("modular_aluminum_w16_engine", "aluminum", "w16", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_R32 = registerEngine("modular_aluminum_r32_engine", "aluminum", "r32", true);

    // Модульные титановые
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_I4 = registerEngine("modular_titanium_i4_engine", "titanium", "i4", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_V8 = registerEngine("modular_titanium_v8_engine", "titanium", "v8", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_W16 = registerEngine("modular_titanium_w16_engine", "titanium", "w16", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_R32 = registerEngine("modular_titanium_r32_engine", "titanium", "r32", true);


    // ==========================================
    // 4. РЕГИСТРАЦИЯ СУЩНОСТЕЙ (BLOCK ENTITIES)
    // ==========================================

    // УНИВЕРСАЛЬНЫЙ ТИП ДЛЯ ВСЕХ НЕМОДУЛЬНЫХ ДВИГАТЕЛЕЙ (Твоя оригинальная сетка!)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.ruby.mod.create_additional_energy_sourses.NonModularEnginesBlockEntity>> NON_MODULAR_ENGINE_ENTITY =
            BLOCK_ENTITIES.register("non_modular_engine_entity", () -> BlockEntityType.Builder.of(
                    // Передаем строго pos и state! А тип сущности класс заберет сам через ModBlocks
                    (pos, state) -> new com.ruby.mod.create_additional_energy_sourses.NonModularEnginesBlockEntity(pos, state),

                    // Твоя сетка моторов капсом
                    IRON_I4.get(), IRON_V8.get(), IRON_W16.get(), IRON_R32.get(),
                    ALUMINUM_I4.get(), ALUMINUM_V8.get(), ALUMINUM_W16.get(), ALUMINUM_R32.get(),
                    TITANIUM_I4.get(), TITANIUM_V8.get(), TITANIUM_W16.get(), TITANIUM_R32.get()
            ).build(null));

    // УНИВЕРСАЛЬНЫЙ ТИП ДЛЯ ВСЕХ МОДУЛЬНЫХ ДВИГАТЕЛЕЙ (Чистый и автоматический!)
    public static final net.minecraft.core.Holder MODULAR_ENGINE_ENTITY =
            BLOCK_ENTITIES.register("modular_engine_entity", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
                    ModularEnginesBlockEntity::new, // Передаем ссылку на наш автоматический 2-аргументный конструктор

                    // Твоя сетка модульных моторов капсом
                    MODULAR_IRON_I4.get(), MODULAR_IRON_V8.get(), MODULAR_IRON_W16.get(), MODULAR_IRON_R32.get(),
                    MODULAR_ALUMINUM_I4.get(), MODULAR_ALUMINUM_V8.get(), MODULAR_ALUMINUM_W16.get(), MODULAR_ALUMINUM_R32.get(),
                    MODULAR_TITANIUM_I4.get(), MODULAR_TITANIUM_V8.get(), MODULAR_TITANIUM_W16.get(), MODULAR_TITANIUM_R32.get()
            ).build(null));


    // ==========================================
    // 5. ВСПОМОГАТЕЛЬНЫЙ МЕТОД СОКРАЩЕНИЯ РЕГИСТРАЦИИ
    // ==========================================
    private static DeferredHolder<Block, Block> registerEngine(String id, String material, String type, boolean isModular) {
        float weightStrength = material.equals("titanium") ? 8.0f : (material.equals("aluminum") ? 3.0f : 4.0f);
        SoundType sound = SoundType.METAL;

        DeferredHolder<Block, Block> registeredBlock = BLOCKS.register(id, () -> {
            BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                    .strength(weightStrength)
                    .sound(sound)
                    .requiresCorrectToolForDrops()
                    .noOcclusion();

            return isModular ? new ModularEnginesBlock(props) : new NonModularEnginesBlock(props);
        });

        ModItems.ITEMS.register(id, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
        return registeredBlock;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}



