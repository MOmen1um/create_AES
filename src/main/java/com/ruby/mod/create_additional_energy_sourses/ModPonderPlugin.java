package com.ruby.mod.create_additional_energy_sourses;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ModPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return "create_additional_energy_sourses";
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

        // 🌟 1. РЕГИСТРИРУЕМ СЦЕНЫ НА ВСЕ ТВОИ КОНКРЕТНЫЕ ДВИГАТЕЛИ ТЕКСТОМ
        // Мы пишем именно те ID, под которыми они регистрируются в твоём ModBlocks.java!
        // 🌟 1. СТРОГО ТОЧНЫЕ ID ВСЕХ НЕМОДУЛЬНЫХ ДВИГАТЕЛЕЙ
        helper.forComponents(
                        // Чугунные (Iron) — без приставки iron_!
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "i4_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "v8_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "w16_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "r32_engine"),

                        // Алюминиевые (Aluminum)
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "aluminum_i4_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "aluminum_v8_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "aluminum_w16_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "aluminum_r32_engine"),

                        // Титановые (Titanium)
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "titanium_i4_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "titanium_v8_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "titanium_w16_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "titanium_r32_engine")
                )
                .addStoryBoard("scene1", ModPonderStoryboards::baseEngineScene)
                .addStoryBoard("scene2", ModPonderStoryboards::radiatorSetupScene)
                .addStoryBoard("scene3", ModPonderStoryboards::infiniteSpeedScene)
                .addStoryBoard("scene4", ModPonderStoryboards::coolingAndMeltingScene);

        // 🌟 2. СТРОГО ТОЧНЫЕ ID ВСЕХ МОДУЛЬНЫХ ДВИГАТЕЛЕЙ
        helper.forComponents(
                        // Модульные чугунные
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_iron_i4_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_iron_v8_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_iron_w16_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_iron_r32_engine"),

                        // Модульные алюминиевые
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_aluminum_i4_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_aluminum_v8_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_aluminum_w16_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_aluminum_r32_engine"),

                        // Модульные титановые
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_titanium_i4_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_titanium_v8_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_titanium_w16_engine"),
                        ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "modular_titanium_r32_engine")
                )
                .addStoryBoard("scene1", ModPonderStoryboards::baseEngineScene)
                .addStoryBoard("scene2", ModPonderStoryboards::radiatorSetupScene)
                .addStoryBoard("scene3", ModPonderStoryboards::infiniteSpeedScene)
                .addStoryBoard("scene4", ModPonderStoryboards::coolingAndMeltingScene);

    }
}

