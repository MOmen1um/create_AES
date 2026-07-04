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
                        // Кладем на витрину полноценные, зарегистрированные Блок-Предметы
                        output.accept(new ItemStack(ModBlocks.THERMO_GENERATOR_ITEM.get(), 1));
                        output.accept(new ItemStack(ModBlocks.V8_ENGINE_ITEM.get(), 1));
                        output.accept(new ItemStack(ModItems.ADVANCED_PICKAXE.get()));
                        output.accept(new ItemStack(ModItems.HEAVY_HANDLE.get()));
                        output.accept(new ItemStack(ModItems.HEAVY_TIP.get()));
                        output.accept(new ItemStack(ModItems.PICKAXE_CORE.get()));
                        output.accept(new ItemStack(ModItems.ADVANCED_PRECISION_MECHANISM.get()));
                        output.accept(new ItemStack(ModBlocks.ALUMINUM_V8_ENGINE_ITEM.get()));
                        output.accept(new ItemStack(ModBlocks.TITANIUM_V8_ENGINE_ITEM.get()));

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
        modEventBus.addListener(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class, event -> {
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.V8_ENGINE_ENTITY.get(),
                    (be, side) -> {
                        if (be instanceof V8EngineBlockEntity v8 && side == net.minecraft.core.Direction.DOWN) {
                            return v8.getFluidTank();
                        }
                        return null;
                    }
            );

            // --- 2. РЕГИСТРАЦИЯ БАКА АЛЮМИНИЕВОГО ДВС (Только снизу) ---
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.ALUMINUM_V8_ENGINE_ENTITY.get(),
                    (be, side) -> {
                        if (be instanceof AluminumV8EngineBlockEntity v8 && side == net.minecraft.core.Direction.DOWN) {
                            return v8.getFluidTank();
                        }
                        return null;
                    }
            );

            // --- 3. РЕГИСТРАЦИЯ БАКА ТИТАНОВОГО ДВС (Только снизу) ---
            event.registerBlockEntity(
                    net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    (net.minecraft.world.level.block.entity.BlockEntityType) ModBlocks.TITANIUM_V8_ENGINE_ENTITY.get(),
                    (be, side) -> {
                        if (be instanceof TitaniumV8EngineBlockEntity v8 && side == net.minecraft.core.Direction.DOWN) {
                            return v8.getFluidTank();
                        }
                        return null;
                    }
            );
            // --- ПРЯМАЯ РЕГИСТРАЦИЯ ТРУБ ДЛЯ КАЖДОГО ТИРА РАДИАТОРОВ ---

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