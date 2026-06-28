package com.ruby.mod.create_additional_energy_sourses;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

    // 1. Блок Термогенератора
    public static final DeferredHolder<Block, ThermoGeneratorBlock> THERMO_GENERATOR =
            BLOCKS.register("thermo_generator", () -> new ThermoGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // 2. Предмет Термогенератора
    public static final DeferredHolder<Item, BlockItem> THERMO_GENERATOR_ITEM =
          ModItems.ITEMS.register("thermo_generator_item", () -> new BlockItem(THERMO_GENERATOR.get(), new Item.Properties()));

    // 3. Блок Двигателя V8
    public static final DeferredHolder<Block, V8EngineBlock> V8_ENGINE_BLOCK =
            BLOCKS.register("v8_engine", () -> new V8EngineBlock(BlockBehaviour.Properties.of()
                    .strength(4.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // 4. Предмет Двигателя V8
    public static final DeferredHolder<Item, BlockItem> V8_ENGINE_ITEM =
            ModItems.ITEMS.register("v8_engine", () -> new BlockItem(V8_ENGINE_BLOCK.get(), new Item.Properties()));

    // 5. Энтити Термогенератора (Явно передаем тип через .get() в лямбду)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermoGeneratorBlockEntity>> THERMO_GEN_ENTITY =
           BLOCK_ENTITIES.register("thermo_generator", () -> BlockEntityType.Builder.of(
                   (pos, state) -> new ThermoGeneratorBlockEntity(ModBlocks.THERMO_GEN_ENTITY.get(), pos, state),
                   THERMO_GENERATOR.get()).build(null));

    // 6. Энтити V8 (Точно так же передаем три параметра)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<V8EngineBlockEntity>> V8_ENGINE_ENTITY =
            BLOCK_ENTITIES.register("v8_engine_entity", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new V8EngineBlockEntity(ModBlocks.V8_ENGINE_ENTITY.get(), pos, state),
                    V8_ENGINE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}


