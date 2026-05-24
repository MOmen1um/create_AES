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

@Mod(Create_additional_energy_sourses.MODID)
public class Create_additional_energy_sourses {
    public static final String MODID = "create_additional_energy_sourses";

    // Регистрируем вкладку
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_additional_energy_sourses"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new net.minecraft.world.item.ItemStack(ModBlocks.THERMO_GENERATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.THERMO_GENERATOR.get());
                        output.accept(ModItems.ADVANCED_PICKAXE.get());
                        output.accept(ModItems.HEAVY_HANDLE.get());
                        output.accept(ModItems.PICKAXE_CORE.get());
                        output.accept(ModItems.HEAVY_TIP.get());

                        // Алюминий
                        output.accept(ModItems.ALUMINUM_BLOCK_ITEM.get());
                        output.accept(ModItems.ALUMINUM_STAIRS_ITEM.get());
                        output.accept(ModItems.ALUMINUM_SLAB_ITEM.get());

                        // Титан
                        output.accept(ModItems.TITANIUM_BLOCK_ITEM.get());
                        output.accept(ModItems.TITANIUM_STAIRS_ITEM.get());
                        output.accept(ModItems.TITANIUM_SLAB_ITEM.get());

                        // ДВС органы
                        output.accept(ModItems.ENGINE_VALVE_ITEM.get());
                        output.accept(ModItems.TURBOCHARGER_ITEM.get());
                        output.accept(ModItems.CRANKSHAFT_ITEM.get());
                    }).build()
            );

    public Create_additional_energy_sourses(IEventBus modEventBus, ModContainer modContainer) {
        // Регистрируем всё через ModBlocks (блоки, предметы, энтити)
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        // Регистрируем вкладки
        CREATIVE_MODE_TABS.register(modEventBus);


        // ВНИМАНИЕ: Строка NeoForge.EVENT_BUS.register(this) удалена, чтобы не было ошибки!
    }
}