package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import com.ruby.mod.create_additional_energy_sourses.item.thermoGeneratorblock.blockentity.ThermoGeneratorBlockEntity;

public class ModBlocks {
    // 1. Сначала создаем регистраторы (базы)
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Create_additional_energy_sourses.MODID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Create_additional_energy_sourses.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Create_additional_energy_sourses.MODID);

    // 2. Теперь регистрируем сами ПРЕДМЕТЫ (Механизм точности)
    public static final DeferredHolder<Item, Item> ADVANCED_PRECISION_MECHANISM =
            ITEMS.register("advanced_precision_mechanism",
                    () -> new Item(new Item.Properties()));

    // 3. Регистрируем БЛОКИ (Термогенератор)
    public static final DeferredHolder<Block, ThermoGeneratorBlock> THERMO_GENERATOR =
            BLOCKS.register("thermo_generator",
                    () -> new ThermoGeneratorBlock(BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    // ==================== НАБОР ДЕТАЛЕЙ ДЛЯ ТВОЕГО ДВС ====================

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> ALUMINUM_BLOCK = BLOCKS.register("aluminum_block",
            () -> new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(3.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> ALUMINUM_STAIRS = BLOCKS.register("aluminum_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(ALUMINUM_BLOCK.get().defaultBlockState(), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(3.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> ALUMINUM_SLAB = BLOCKS.register("aluminum_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(3.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            () -> new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(6.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> TITANIUM_STAIRS = BLOCKS.register("titanium_stairs",
            () -> new net.minecraft.world.level.block.StairBlock(TITANIUM_BLOCK.get().defaultBlockState(), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(6.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> TITANIUM_SLAB = BLOCKS.register("titanium_slab",
            () -> new net.minecraft.world.level.block.SlabBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(6.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> ENGINE_VALVE = BLOCKS.register("engine_valve",
            () -> new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(3.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> TURBOCHARGER = BLOCKS.register("turbocharger",
            () -> new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(4.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));

    public static final java.util.function.Supplier<net.minecraft.world.level.block.Block> CRANKSHAFT = BLOCKS.register("crankshaft",
            () -> new CrankshaftBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(5.0F).sound(net.minecraft.world.level.block.SoundType.METAL)));
    // Предметы для алюминиевого набора
    public static final java.util.function.Supplier<net.minecraft.world.item.Item> ALUMINUM_BLOCK_ITEM = ITEMS.register("aluminum_block",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.ALUMINUM_BLOCK.get(), new net.minecraft.world.item.Item.Properties()));

    public static final java.util.function.Supplier<net.minecraft.world.item.Item> ALUMINUM_STAIRS_ITEM = ITEMS.register("aluminum_stairs",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.ALUMINUM_STAIRS.get(), new net.minecraft.world.item.Item.Properties()));

    public static final java.util.function.Supplier<net.minecraft.world.item.Item> ALUMINUM_SLAB_ITEM = ITEMS.register("aluminum_slab",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.ALUMINUM_SLAB.get(), new net.minecraft.world.item.Item.Properties()));

    // Предметы для титанового набора
    public static final java.util.function.Supplier<net.minecraft.world.item.Item> TITANIUM_BLOCK_ITEM = ITEMS.register("titanium_block",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.TITANIUM_BLOCK.get(), new net.minecraft.world.item.Item.Properties()));

    public static final java.util.function.Supplier<net.minecraft.world.item.Item> TITANIUM_STAIRS_ITEM = ITEMS.register("titanium_stairs",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.TITANIUM_STAIRS.get(), new net.minecraft.world.item.Item.Properties()));

    public static final java.util.function.Supplier<net.minecraft.world.item.Item> TITANIUM_SLAB_ITEM = ITEMS.register("titanium_slab",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.TITANIUM_SLAB.get(), new net.minecraft.world.item.Item.Properties()));

    // Предметы для механизмов ДВС
    public static final java.util.function.Supplier<net.minecraft.world.item.Item> ENGINE_VALVE_ITEM = ITEMS.register("engine_valve",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.ENGINE_VALVE.get(), new net.minecraft.world.item.Item.Properties()));

    public static final java.util.function.Supplier<net.minecraft.world.item.Item> TURBOCHARGER_ITEM = ITEMS.register("turbocharger",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.TURBOCHARGER.get(), new net.minecraft.world.item.Item.Properties()));

    public static final java.util.function.Supplier<net.minecraft.world.item.Item> CRANKSHAFT_ITEM = ITEMS.register("crankshaft",
            () -> new net.minecraft.world.item.BlockItem(ModBlocks.CRANKSHAFT.get(), new net.minecraft.world.item.Item.Properties()));


    // 4. Регистрируем Блок-предмет (чтобы блок можно было держать в руках)
    public static final DeferredHolder<Item, BlockItem> THERMO_GENERATOR_ITEM =
            ITEMS.registerSimpleBlockItem("thermo_generator", THERMO_GENERATOR);

    // 5. Регистрируем "Мозги" блока (Block Entity)
    public static final Supplier<BlockEntityType<ThermoGeneratorBlockEntity>> THERMO_GEN_ENTITY =
            BLOCK_ENTITIES.register("thermo_generator",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new ThermoGeneratorBlockEntity(ModBlocks.THERMO_GEN_ENTITY.get(), pos, state),
                            THERMO_GENERATOR.get()
                    ).build(null));

    public static final java.util.function.Supplier CRANKSHAFT_ENTITY =
            BLOCK_ENTITIES.register("crankshaft_entity", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(
                    (pos, state) -> new CrankshaftBlockEntity((net.minecraft.world.level.block.entity.BlockEntityType<?>) ModBlocks.CRANKSHAFT_ENTITY.get(), pos, state),
                    CRANKSHAFT.get()
            ).build(null));

    // Метод, который всё это "включает" при запуске мода
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        ITEMS.register(eventBus);
    }
}
