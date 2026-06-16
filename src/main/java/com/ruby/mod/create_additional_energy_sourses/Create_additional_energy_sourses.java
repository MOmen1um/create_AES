package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@net.neoforged.fml.common.Mod(Create_additional_energy_sourses.MODID)
@net.neoforged.fml.common.asm.XModFile.EventBusSubscriber(bus = net.neoforged.fml.common.asm.XModFile.EventBusSubscriber.Bus.MOD, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class Create_additional_energy_sourses {
    public static final String MODID = "create_additional_energy_sourses";

    // Регистрируем вкладку
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_additional_energy_sourses"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModBlocks.THERMO_GENERATOR_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.THERMO_GENERATOR_ITEM.get());
                        output.accept(ModBlocks.ADVANCED_PRECISION_MECHANISM.get());
                        output.accept(ModItems.ADVANCED_PICKAXE.get());
                        output.accept(ModItems.HEAVY_HANDLE.get());
                        output.accept(ModItems.PICKAXE_CORE.get());
                        output.accept(ModItems.HEAVY_TIP.get());
                    }).build());

    public Create_additional_energy_sourses(IEventBus modEventBus, ModContainer modContainer) {
        // Регистрируем всё через ModBlocks (блоки, предметы, энтити)
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        // Регистрируем вкладки
        CREATIVE_MODE_TABS.register(modEventBus);

        // ВОТ ЭТУ СТРОЧКУ ДОБАВЬ СЮДА:
        modEventBus.addListener(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerBlockEntityRenderer(
                    com.ruby.mod.create_additional_energy_sourses.ModBlocks.THERMO_GEN_ENTITY.get(),
                    ctx -> new com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer(ctx)
            );
        });
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                com.ruby.mod.create_additional_energy_sourses.ModBlocks.THERMO_GEN_ENTITY.get(),
                ctx -> new com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer(ctx)
        );
    }

}