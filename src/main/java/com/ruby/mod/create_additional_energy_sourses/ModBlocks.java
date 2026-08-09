package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.COPPER).noOcclusion(), () -> ModBlocks.RADIATOR_COPPER_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_COPPER_ITEM = ModItems.ITEMS.register("radiator_copper",
            () -> new BlockItem(RADIATOR_COPPER.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> RADIATOR_GOLD = BLOCKS.register("radiator_gold",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.NETHER_GOLD_ORE).noOcclusion(), () -> ModBlocks.RADIATOR_GOLD_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_GOLD_ITEM = ModItems.ITEMS.register("radiator_gold",
            () -> new BlockItem(RADIATOR_GOLD.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> RADIATOR_STEEL = BLOCKS.register("radiator_steel",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.METAL).noOcclusion(), () -> ModBlocks.RADIATOR_STEEL_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_STEEL_ITEM = ModItems.ITEMS.register("radiator_steel",
            () -> new BlockItem(RADIATOR_STEEL.get(), new Item.Properties()));

    public static final DeferredHolder<Block, Block> RADIATOR_TITANIUM = BLOCKS.register("radiator_titanium",
            () -> new BaseRadiatorBlock(BlockBehaviour.Properties.of().strength(6.0f).sound(SoundType.METAL).noOcclusion(), () -> ModBlocks.RADIATOR_TITANIUM_ENTITY.get()));
    public static final DeferredHolder<Item, BlockItem> RADIATOR_TITANIUM_ITEM = ModItems.ITEMS.register("radiator_titanium",
            () -> new BlockItem(RADIATOR_TITANIUM.get(), new Item.Properties()));


    // --- 2. ИСПРАВЛЕННАЯ РЕГИСТРАЦИЯ СУЩНОСТЕЙ (BLOCK ENTITIES) ---

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_COPPER_ENTITY =
            BLOCK_ENTITIES.register("radiator_copper_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_COPPER_ENTITY.get(), pos, state), ModBlocks.RADIATOR_COPPER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_GOLD_ENTITY =
            BLOCK_ENTITIES.register("radiator_brass_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_GOLD_ENTITY.get(), pos, state), ModBlocks.RADIATOR_GOLD.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_STEEL_ENTITY =
            BLOCK_ENTITIES.register("radiator_steel_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_STEEL_ENTITY.get(), pos, state), ModBlocks.RADIATOR_STEEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BaseRadiatorBlockEntity>> RADIATOR_TITANIUM_ENTITY =
            BLOCK_ENTITIES.register("radiator_ultimate_entity", () -> BlockEntityType.Builder.of((pos, state) -> new BaseRadiatorBlockEntity(ModBlocks.RADIATOR_TITANIUM_ENTITY.get(), pos, state), ModBlocks.RADIATOR_TITANIUM.get()).build(null));



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
    public static final DeferredHolder<Block, Block> MODULAR_IRON_I2 = registerEngine("modular_iron_i2_engine", "iron", "i2", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_I2_CONTROLLER = registerController("modular_iron_i2_controller", "iron", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_IRON_V4 = registerEngine("modular_iron_v4_engine", "iron", "v4", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_V4_CONTROLLER = registerController("modular_iron_v4_controller", "iron", "v4");
    public static final DeferredHolder<Block, Block> MODULAR_IRON_W8 = registerEngine("modular_iron_w8_engine", "iron", "w8", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_W8_CONTROLLER = registerController("modular_iron_w8_controller", "iron", "w8");
    public static final DeferredHolder<Block, Block> MODULAR_IRON_R16 = registerEngine("modular_iron_r16_engine", "iron", "r16", true);
    public static final DeferredHolder<Block, Block> MODULAR_IRON_R16_CONTROLLER = registerController("modular_iron_r16_controller", "iron", "r16");

    // Модульные алюминиевые
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_I2 = registerEngine("modular_aluminum_i2_engine", "aluminum", "i2", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_I2_CONTROLLER = registerController("modular_aluminum_i2_controller", "aluminum", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_V4 = registerEngine("modular_aluminum_v4_engine", "aluminum", "v4", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_V4_CONTROLLER = registerController("modular_aluminum_v4_controller", "aluminum", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_W8 = registerEngine("modular_aluminum_w8_engine", "aluminum", "w8", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_W8_CONTROLLER = registerController("modular_aluminum_w8_controller", "aluminum", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_R16 = registerEngine("modular_aluminum_r16_engine", "aluminum", "r16", true);
    public static final DeferredHolder<Block, Block> MODULAR_ALUMINUM_R16_CONTROLLER = registerController("modular_aluminum_r16_controller", "aluminum", "i2");

    // Модульные титановые
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_I2 = registerEngine("modular_titanium_i2_engine", "titanium", "i2", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_I2_CONTROLLER = registerController("modular_titanium_i2_controller", "titanium", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_V4 = registerEngine("modular_titanium_v4_engine", "titanium", "v4", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_V4_CONTROLLER = registerController("modular_titanium_v4_controller", "titanium", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_W8 = registerEngine("modular_titanium_w8_engine", "titanium", "w8", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_W8_CONTROLLER = registerController("modular_titanium_w8_controller", "titanium", "i2");
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_R16 = registerEngine("modular_titanium_r16_engine", "titanium", "r16", true);
    public static final DeferredHolder<Block, Block> MODULAR_TITANIUM_R16_CONTROLLER = registerController("modular_titanium_r16_controller", "titanium", "i2");

    // Чугунные картеры (Iron)
    public static final DeferredHolder<Block, Block> IRON_I4_CARTER  = registerCarter("iron_i4_carter");
    public static final DeferredHolder<Block, Block> IRON_V8_CARTER  = registerCarter("iron_v8_carter");
    public static final DeferredHolder<Block, Block> IRON_W16_CARTER = registerCarter("iron_w16_carter");
    public static final DeferredHolder<Block, Block> IRON_R32_CARTER = registerCarter("iron_r32_carter");

    // Алюминиевые картеры (Aluminum)
    public static final DeferredHolder<Block, Block> ALUMINUM_I4_CARTER  = registerCarter("aluminum_i4_carter");
    public static final DeferredHolder<Block, Block> ALUMINUM_V8_CARTER  = registerCarter("aluminum_v8_carter");
    public static final DeferredHolder<Block, Block> ALUMINUM_W16_CARTER = registerCarter("aluminum_w16_carter");
    public static final DeferredHolder<Block, Block> ALUMINUM_R32_CARTER = registerCarter("aluminum_r32_carter");

    // Титановые картеры (Titanium)
    public static final DeferredHolder<Block, Block> TITANIUM_I4_CARTER  = registerCarter("titanium_i4_carter");
    public static final DeferredHolder<Block, Block> TITANIUM_V8_CARTER  = registerCarter("titanium_v8_carter");
    public static final DeferredHolder<Block, Block> TITANIUM_W16_CARTER = registerCarter("titanium_w16_carter");
    public static final DeferredHolder<Block, Block> TITANIUM_R32_CARTER = registerCarter("titanium_r32_carter");

    // Регистрация Алюминия (Каменная кирка и выше)
    public static final DeferredHolder<Block, Block> ALUMINUM_ORE = BLOCKS.register("aluminum_ore", () ->
            new Block(BlockBehaviour.Properties.of().strength(3.0f)) {
                @Override
                public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
                    java.util.List<ItemStack> drops = new java.util.ArrayList<>();
                    ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);

                    // 1. Проверяем, что в руках вообще КИРКА (через тег)
                    if (tool == null || !tool.is(net.minecraft.tags.ItemTags.PICKAXES)) {
                        return drops; // Если рука, топор или лопата — дропа нет
                    }

                    // 2. Убираем дерево и золото по названию предмета
                    String itemName = tool.getItem().toString();
                    if (itemName.contains("wooden_pickaxe") || itemName.contains("golden_pickaxe")) {
                        return drops; // Каменная, железная, алмазная и незеритовая сработают!
                    }

                    int fortuneLevel = tool.getEnchantmentLevel(params.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE));
                    int count = 1 + (fortuneLevel > 0 ? new java.util.Random().nextInt(fortuneLevel + 1) : 0);
                    drops.add(new ItemStack(ModItems.RAW_ALUMINUM, count));
                    return drops;
                }
            }
    );

    // Регистрация Титана (Только Незеритовая и модовые кирки)
    public static final DeferredHolder<Block, Block> TITANIUM_ORE = BLOCKS.register("titanium_ore", () ->
            new Block(BlockBehaviour.Properties.of().strength(5.0f)) {
                @Override
                public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
                    java.util.List<ItemStack> drops = new java.util.ArrayList<>();
                    ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);

                    // 2. Разрешаем только незеритовую кирку.
                    // Если у тебя появится кастомная кирка из твоего мода, допиши её сюда через ИЛИ (|| itemName.contains("имя_твоей_кирки"))
                    String itemName = tool.getItem().toString();
                    if (!itemName.contains("netherite_pickaxe") || itemName.contains("advanced_pickaxe")) {
                        return drops; // Все остальные ванильные кирки (алмазная, железная) выдадут 0
                    }

                    // Логика Шелкового касания и Удачи
                    if (tool.getEnchantmentLevel(params.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH)) > 0) {
                        drops.add(new ItemStack(this));
                        return drops;
                    }
                    int fortuneLevel = tool.getEnchantmentLevel(params.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE));
                    int count = 1 + (fortuneLevel > 0 ? new java.util.Random().nextInt(fortuneLevel + 1) : 0);
                    drops.add(new ItemStack(ModItems.RAW_TITANIUM, count));
                    return drops;
                }
            }
    );


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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModularEnginesBlockEntity>> MODULAR_ENGINE_ENTITY =
            BLOCK_ENTITIES.register("modular_engine_entity", () -> {
                return BlockEntityType.Builder.of(
                        // Самая чистая и безопасная фабрика в мире:
                        (pos, state) -> new ModularEnginesBlockEntity(pos, state, false),

                        // === 1. REGULAR ENGINES ===
                        MODULAR_IRON_I2.get(), MODULAR_IRON_V4.get(), MODULAR_IRON_W8.get(), MODULAR_IRON_R16.get(),
                        MODULAR_ALUMINUM_I2.get(), MODULAR_ALUMINUM_V4.get(), MODULAR_ALUMINUM_W8.get(), MODULAR_ALUMINUM_R16.get(),
                        MODULAR_TITANIUM_I2.get(), MODULAR_TITANIUM_V4.get(), MODULAR_TITANIUM_W8.get(), MODULAR_TITANIUM_R16.get(),

                        // === 2. CONTROLLERS ===
                        MODULAR_IRON_I2_CONTROLLER.get(), MODULAR_IRON_V4_CONTROLLER.get(), MODULAR_IRON_W8_CONTROLLER.get(), MODULAR_IRON_R16_CONTROLLER.get(),
                        MODULAR_ALUMINUM_I2_CONTROLLER.get(), MODULAR_ALUMINUM_V4_CONTROLLER.get(), MODULAR_ALUMINUM_W8_CONTROLLER.get(), MODULAR_ALUMINUM_R16_CONTROLLER.get(),
                        MODULAR_TITANIUM_I2_CONTROLLER.get(), MODULAR_TITANIUM_V4_CONTROLLER.get(), MODULAR_TITANIUM_W8_CONTROLLER.get(), MODULAR_TITANIUM_R16_CONTROLLER.get()
                ).build(null);
            });
    // <-- ЗАКРЫВАЕМ регистратор

    // УНИВЕРСАЛЬНЫЙ ТИП СУЩНОСТИ ДЛЯ ВСЕХ НАШИХ КАРТЕРОВ (Через Стрим-Фабрику!)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EngineCarterBlockEntity>> ENGINE_CARTER_ENTITY =
            BLOCK_ENTITIES.register("engine_carter_entity", () -> BlockEntityType.Builder.of(
                    EngineCarterBlockEntity::new,
                    // Собираем все 12 картеров в один массив силами Java ООП!
                    java.util.stream.Stream.of(
                            IRON_I4_CARTER, IRON_V8_CARTER, IRON_W16_CARTER, IRON_R32_CARTER,
                            ALUMINUM_I4_CARTER, ALUMINUM_V8_CARTER, ALUMINUM_W16_CARTER, ALUMINUM_R32_CARTER,
                            TITANIUM_I4_CARTER, TITANIUM_V8_CARTER, TITANIUM_W16_CARTER, TITANIUM_R32_CARTER
                    ).map(DeferredHolder::get).toArray(Block[]::new)
            ).build(null));

    // ==========================================
// ФАБРИКА АВТОМАТИЧЕСКОЙ РЕГИСТРАЦИИ КАРТЕРОВ
// ==========================================
    private static DeferredHolder<Block, Block> registerCarter(String id) {
        return BLOCKS.register(id, () ->
                new EngineCarterBlock(BlockBehaviour.Properties.of()
                        .mapColor(net.minecraft.world.level.material.MapColor.METAL)
                        .strength(3.0f)
                        .requiresCorrectToolForDrops()
                        .noOcclusion())
        );
    }



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
    private static DeferredHolder<Block, Block> registerController(String id, String material, String type) {
        // 1. Считаем прочность от материала точно так же, как в твоем методе
        float weightStrength = material.equals("titanium") ? 8.0f : (material.equals("aluminum") ? 3.0f : 4.0f);

        DeferredHolder<Block, Block> registeredBlock = BLOCKS.register(id, () -> {
            BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                    .strength(weightStrength)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion();

            // 2. ВАЖНО: передаем в конструктор ModularEnginesBlock свойства И флаг true (что это контроллер!)
            return new ModularEnginesBlock(props, true);
        });

        // 3. Автоматически регистрируем предмет для инвентаря
        ModItems.ITEMS.register(id, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));

        return registeredBlock;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}



