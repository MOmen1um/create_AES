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
        // Силой привязываем сцену ОДНОВРЕМЕННО и к БЛОКУ, и к ПРЕДМЕТУ из инвентаря!
        helper.forComponents(
                        ModBlocks.TITANIUM_W16.get(), // Физический блок в мире
                        ModBlocks.TITANIUM_W16.get().asItem() // Иконка предмета в инвентаре/JEI
                )
                .addStoryBoard("engine_base", ModPonderStoryboards::baseEngineScene)
                .addStoryBoard("radiator_setup", ModPonderStoryboards::radiatorSetupScene)
                .addStoryBoard("infinite_speed", ModPonderStoryboards::infiniteSpeedScene)
                .addStoryBoard("cooling_melting", ModPonderStoryboards::coolingAndMeltingScene);
    }
}