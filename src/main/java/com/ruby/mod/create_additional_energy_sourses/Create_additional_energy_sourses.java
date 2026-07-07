package com.ruby.mod.create_additional_energy_sourses;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

@Mod(Create_additional_energy_sourses.MODID)
public class Create_additional_energy_sourses {
    public static final String MODID = "create_additional_energy_sourses";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);


    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB =
            CREATIVE_MODE_TABS.register("create_aes_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.THERMO_GENERATOR.get()))
                    .title(net.minecraft.network.chat.Component.literal("Create: Additional Energy Sources"))
                    .displayItems((parameters, output) -> {
                        // 1. Термогенератор и кастомные предметы
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.THERMO_GENERATOR_ITEM.get(), 1));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ADVANCED_PICKAXE.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.HEAVY_HANDLE.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.HEAVY_TIP.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.PICKAXE_CORE.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ADVANCED_PRECISION_MECHANISM.get()));

                        // 2. Радиаторы охлаждения
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_COPPER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_STEEL_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_BRASS_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_ULTIMATE_ITEM.get()));

                        // 3. НЕМОДУЛЬНЫЕ ДВИГАТЕЛИ (Выстраиваем по материалам и мощности)
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.IRON_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.IRON_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.IRON_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.IRON_R32.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.ALUMINUM_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.ALUMINUM_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.ALUMINUM_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.ALUMINUM_R32.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_R32.get()));

                        // 4. МОДУЛЬНЫЕ ДВИГАТЕЛИ (Сквозные блоки)
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_IRON_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_IRON_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_IRON_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_IRON_R32.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_ALUMINUM_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_ALUMINUM_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_ALUMINUM_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_ALUMINUM_R32.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_TITANIUM_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_TITANIUM_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_TITANIUM_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.MODULAR_TITANIUM_R32.get()));
                    })
                    .build());

    public Create_additional_energy_sourses(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);


        // Слушатель рендереров валов для ОБОИХ блоков
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerBlockEntityRenderer(ModBlocks.THERMO_GEN_ENTITY.get(), KineticBlockEntityRenderer::new);
        });
        // === ИСПРАВЛЕННЫЙ СЛУШАТЕЛЬ ТОПЛИВНЫХ КАПАБИЛИТИ ДЛЯ 1.21.1 ===
        modEventBus.addListener(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class, event -> {

            // 1. Открываем бак для жидкостей у всех НЕМОДУЛЬНЫХ двигателей мода
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    ModBlocks.NON_MODULAR_ENGINE_ENTITY.get(),
                    (blockEntity, side) -> blockEntity.fuelTank // Отдаём бак немодульного мотора
            );

            // 2. Открываем бак для жидкостей у всех МОДУЛЬНЫХ двигателей мода
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    ModBlocks.MODULAR_ENGINE_ENTITY.get(),
                    (blockEntity, side) -> blockEntity.fuelTank // Отдаём бак модульного мотора
            );


            // 1. Медный
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.RADIATOR_COPPER_ENTITY.get(),
                    (be, side) -> be instanceof BaseRadiatorBlockEntity radiator ? radiator.waterTank : null
            );

            // 2. Латунный
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.RADIATOR_BRASS_ENTITY.get(),
                    (be, side) -> be instanceof BaseRadiatorBlockEntity radiator ? radiator.waterTank : null
            );

            // 3. Стальной
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.RADIATOR_STEEL_ENTITY.get(),
                    (be, side) -> be instanceof BaseRadiatorBlockEntity radiator ? radiator.waterTank : null
            );

            // 4. Топовый (Ультимативный)
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.RADIATOR_ULTIMATE_ENTITY.get(),
                    (be, side) -> be instanceof BaseRadiatorBlockEntity radiator ? radiator.waterTank : null
            );
        });


    }
}