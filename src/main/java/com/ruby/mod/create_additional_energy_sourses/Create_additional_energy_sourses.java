package com.ruby.mod.create_additional_energy_sourses;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
                    .icon(() -> new ItemStack(ModBlocks.TITANIUM_W16.get()))
                    .title(net.minecraft.network.chat.Component.literal("Create: Additional Energy Sources"))
                    .displayItems((parameters, output) -> {
                        // 1. ИНСТРУМЕНТЫ И РАСХОДНИКИ (Основа разработки)
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.HEAVY_HANDLE.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.HEAVY_TIP.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ADVANCED_PICKAXE.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.PICKAXE_CORE.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ADVANCED_PRECISION_MECHANISM.get()));

                        // 2. БЛОКИ ЦИЛИНДРОВ / КАРТЕРЫ (Выстраиваем строго от I4 до R32 по металлам)
                        // Чугун (Iron)
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_I4_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_V8_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_W16_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_R32_CARTER_ITEM.get()));
                        // Алюминий (Aluminum)
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_I4_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_V8_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_W16_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_R32_CARTER_ITEM.get()));
                        // Титан (Titanium)
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_I4_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_V8_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_W16_CARTER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_R32_CARTER_ITEM.get()));

                        // 3. ГОТОВЫЕ ДВИГАТЕЛИ ДВC (В таком же строгом порядке!)
                        // Чугунные (По твоей логике дефолтных имен без приставки iron)
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "i4_engine"))));
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "v8_engine"))));
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "w16_engine"))));
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "r32_engine"))));
                        // Алюминиевые
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "aluminum_i4_engine"))));
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "aluminum_v8_engine"))));
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "aluminum_w16_engine"))));
                        output.accept(new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Create_additional_energy_sourses.MODID, "aluminum_r32_engine"))));
                        // Титановые
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_I4.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_V8.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_W16.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.TITANIUM_R32.get()));

                        // 4. СИСТЕМА ОХЛАЖДЕНИЯ (Радиаторы и генераторы)
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.THERMO_GENERATOR_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_COPPER_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_STEEL_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_GOLD_ITEM.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModBlocks.RADIATOR_TITANIUM_ITEM.get()));
                        // РАЗДЕЛ РАСХОДНИКОВ ДЛЯ СБОРКИ (Добавляем наши новые детали!)
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_PISTON.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_PISTON.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_GBC.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_R16_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_R16_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_R16_GBC.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_BRAIN_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_BRAIN_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_BRAIN_GBC.get()));

                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.IRON_R16_BRAIN_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.ALUMINUM_R16_BRAIN_GBC.get()));
                        output.accept(new net.minecraft.world.item.ItemStack(ModItems.TITANIUM_R16_BRAIN_GBC.get()));
                    })
                    .build());

    public Create_additional_energy_sourses(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);



        // Слушатель рендереров валов для ОБОИХ блоков
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerBlockEntityRenderer(ModBlocks.THERMO_GEN_ENTITY.get(), KineticBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlocks.V8_ENGINE_ENTITY.get(), KineticBlockEntityRenderer::new);
        });

        // === ИСПРАВЛЕННЫЙ СЛУШАТЕЛЬ ТОПЛИВНЫХ КАПАБИЛИТИ ДЛЯ 1.21.1 ===
        modEventBus.addListener(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class, event -> {

            // 1. Fluid capability for NON-MODULAR engines (Strictly from the bottom!)
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    ModBlocks.NON_MODULAR_ENGINE_ENTITY.get(),
                    (blockEntity, side) -> {
                        // Only allow fluid interaction from the bottom side!
                        if (side == net.minecraft.core.Direction.DOWN) {
                            return ((NonModularEnginesBlockEntity) blockEntity).getFluidTank();
                        }
                        return null; // Block access from all other sides
                    }
            );

            // 2. Fluid capability for NEW MODULAR engines (Strictly from the bottom!)
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    ModBlocks.MODULAR_ENGINE_ENTITY.get(),
                    (blockEntity, side) -> {
                        // Only allow fuel input from the bottom side of the block!
                        if (side == net.minecraft.core.Direction.DOWN) {
                            return ((ModularEnginesBlockEntity) blockEntity).getFluidTank();
                        }
                        return null;
                    }
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
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.RADIATOR_GOLD_ENTITY.get(),
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
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.RADIATOR_TITANIUM_ENTITY.get(),
                    (be, side) -> be instanceof BaseRadiatorBlockEntity radiator ? radiator.waterTank : null
            );
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    ModBlocks.V8_ENGINE_ENTITY.get(),
                    (blockEntity, side) -> {
                        if (side == net.minecraft.core.Direction.DOWN) {
                            return ((ModularEnginesBlockEntity) blockEntity).getFluidTank();
                        }
                        return null;
                    }
            );
        });


    }
}