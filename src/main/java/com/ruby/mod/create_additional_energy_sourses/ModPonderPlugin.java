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
    public void registerScenes(PonderSceneRegistrationHelper helper) {
        // Оставляем только чистый вызов сцен через правильный .location()
        ResourceLocation titaniumEngineId = ModBlocks.TITANIUM_W16.getKey().location();

        helper.forComponents(new ResourceLocation[]{ titaniumEngineId })
                .addStoryBoard("engine_base", ModPonderStoryboards::baseEngineScene)
                .addStoryBoard("radiator_setup", ModPonderStoryboards::radiatorSetupScene)
                .addStoryBoard("infinite_speed", ModPonderStoryboards::infiniteSpeedScene)
                .addStoryBoard("cooling_melting", ModPonderStoryboards::coolingAndMeltingScene);
    }
}