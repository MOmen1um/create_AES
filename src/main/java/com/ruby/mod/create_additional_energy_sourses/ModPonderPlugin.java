package com.ruby.mod.create_additional_energy_sourses;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

// 1. Добавили <ResourceLocation>, чтобы не ругался @Override на строке 15
public class ModPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return "create_additional_energy_sourses";
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper helper) {
        // Мы передаем ID блока в метод, и Ponder API автоматически привяжет его и к блоку в мире, и к предмету в инвентаре!
        helper.forComponents(new ResourceLocation[]{ ModBlocks.TITANIUM_W16.getId() })
                .addStoryBoard("scene1", ModPonderStoryboards::baseEngineScene)
                .addStoryBoard("scene2", ModPonderStoryboards::radiatorSetupScene)
                .addStoryBoard("scene3", ModPonderStoryboards::infiniteSpeedScene)
                .addStoryBoard("scene4", ModPonderStoryboards::coolingAndMeltingScene);
    }
}
