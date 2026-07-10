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
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Сцена 1: Базовая кинетика, вал и интерфейс двигателя
        // Привязываем к вашему классическому V8 двигателю
        helper.forComponents(new ResourceLocation[]{ ModBlocks.IRON_W16.getId() })
                .addStoryBoard("scene1", ModPonderStoryboards::baseEngineScene);

        // Сцена 2: Смеси радиаторов и правильная стыковка
        // Привязываем, например, к медному радиатору (или любому другому рабочему)
        helper.forComponents(new ResourceLocation[]{ ModBlocks.IRON_W16.getId() })
                .addStoryBoard("scene2", ModPonderStoryboards::radiatorSetupScene);

        // Сцена 3: Немодульные двигатели (Твой эпичный W16!)
        // Если хочешь показать его, привязываем сцену к IRON_W16
        helper.forComponents(new ResourceLocation[]{ ModBlocks.IRON_W16.getId() })
                .addStoryBoard("scene3", ModPonderStoryboards::infiniteSpeedScene);

        // Сцена 4: Термогенератор
        helper.forComponents(new ResourceLocation[]{ ModBlocks.IRON_W16.getId() })
                .addStoryBoard("scene4", ModPonderStoryboards::coolingAndMeltingScene);
    }
}
