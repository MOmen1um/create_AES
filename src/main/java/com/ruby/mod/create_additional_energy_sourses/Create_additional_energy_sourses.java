package com.ruby.mod.create_additional_energy_sourses;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

@Mod(Create_additional_energy_sourses.MODID)
public class Create_additional_energy_sourses {
    public static final String MODID = "create_additional_energy_sourses";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);


    public Create_additional_energy_sourses(IEventBus modEventBus, ModContainer modContainer) {
        // Подключаем наши регистраторы к шине мода
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);


        // Регистрируем рендерер вала вручную — без ломающихся аннотаций
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerBlockEntityRenderer(
                    ModBlocks.THERMO_GEN_ENTITY.get(),
                    KineticBlockEntityRenderer::new
            );
        });
    }
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.CreativeModeTab, net.minecraft.world.item.CreativeModeTab> CREATIVE_TAB =
            CREATIVE_MODE_TABS.register("create_aes_tab", () -> net.minecraft.world.item.CreativeModeTab.builder()
                    // Ставим иконку (наш термогенератор)
                    .icon(() -> new net.minecraft.world.item.ItemStack(ModBlocks.THERMO_GENERATOR.get()))
                    .title(net.minecraft.network.chat.Component.literal("Create: Additional Energy Sources"))
                    // Наполняем её блоками:
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.THERMO_GENERATOR.get());
                        output.accept(ModBlocks.V8_ENGINE_BLOCK.get());
                    })
                    .build());
}
