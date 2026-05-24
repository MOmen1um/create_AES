package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.ruby.mod.create_additional_energy_sourses.item.thermoGeneratorblock.blockentity.ThermoGeneratorBlockEntity

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Create_additional_energy_sourses.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Create_additional_energy_sourses.MODID);

    // ==================== ТЕРМОГЕНЕРАТОР ====================
    public static final Supplier<Block> THERMO_GENERATOR = BLOCKS.register("thermo_generator",
            () -> new ThermoGeneratorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).sound(SoundType.METAL)));

    public static final Supplier<BlockEntityType<ThermoGeneratorBlockEntity>> THERMO_GEN_ENTITY = BLOCK_ENTITIES.register("thermo_gen_entity",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new ThermoGeneratorBlockEntity((BlockEntityType<?>) ModBlocks.THERMO_GEN_ENTITY.get(), pos, state),
                    THERMO_GENERATOR.get()
            ).build(null));


    // ================== АЛЮМИНИЙ (ALUMINUM) ==================
    public static final Supplier<Block> ALUMINUM_BLOCK = BLOCKS.register("aluminum_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)));

    // Починено: Передаем BlockState Блока алюминия + его свойства Properties!
    public static final Supplier<Block> ALUMINUM_STAIRS = BLOCKS.register("aluminum_stairs",
            () -> new StairBlock(ALUMINUM_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> ALUMINUM_SLAB = BLOCKS.register("aluminum_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)));


    // ==================== ТИТАН (TITANIUM) ====================
    public static final Supplier<Block> TITANIUM_BLOCK = BLOCKS.register("titanium_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0F, 12.0F).sound(SoundType.METAL)));

    // Починено: Передаем BlockState Блока титана + его свойства Properties!
    public static final Supplier<Block> TITANIUM_STAIRS = BLOCKS.register("titanium_stairs",
            () -> new StairBlock(TITANIUM_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0F, 12.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> TITANIUM_SLAB = BLOCKS.register("titanium_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0F, 12.0F).sound(SoundType.METAL)));


    // ================== ДЕТАЛИ МОДУЛЬНОГО ДВС ==================
    public static final Supplier<Block> ENGINE_VALVE = BLOCKS.register("engine_valve",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> TURBOCHARGER = BLOCKS.register("turbocharger",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F).sound(SoundType.METAL)));

    public static final Supplier<Block> CRANKSHAFT = BLOCKS.register("crankshaft",
            () -> new CrankshaftBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).sound(SoundType.METAL)));

    public static final Supplier<BlockEntityType<CrankshaftBlockEntity>> CRANKSHAFT_ENTITY = BLOCK_ENTITIES.register("crankshaft_entity",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> new CrankshaftBlockEntity((BlockEntityType<?>) ModBlocks.CRANKSHAFT_ENTITY.get(), pos, state),
                    CRANKSHAFT.get()
            ).build(null));

    public static void register(IEventBus modEventBus) {
    }
}
