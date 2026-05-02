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

    // Метод, который всё это "включает" при запуске мода
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        ITEMS.register(eventBus);
    }
}
