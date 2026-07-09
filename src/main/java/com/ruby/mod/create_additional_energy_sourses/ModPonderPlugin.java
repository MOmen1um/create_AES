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
        // Привязываем сцены scene1-scene4 к ID твоего двигателя
        helper.forComponents(ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "v8_engine"))
                .addStoryBoard("scene1", ModPonderStoryboards::baseEngineScene)
                .addStoryBoard("scene2", ModPonderStoryboards::radiatorSetupScene)
                .addStoryBoard("scene3", ModPonderStoryboards::infiniteSpeedScene)
                .addStoryBoard("scene4", ModPonderStoryboards::coolingAndMeltingScene);
    }
}

