package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.foundation.render.SpecialModels;
import net.minecraft.resources.ResourceLocation;

public class ModSpecialModels {
    // Регистрируем коленвал V8 через новую систему SpecialModels.Key
    public static final SpecialModels.Key V8_CRANKSHAFT = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/v8_crankshaft")
    );

    // Поршни по материалам (TM, ALU, CI)
    public static final SpecialModels.Key TM_PISTON = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/tm_piston")
    );
    public static final SpecialModels.Key ALU_PISTON = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/alu_piston")
    );
    public static final SpecialModels.Key CI_PISTON = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/ci_piston")
    );

    // ГБЦ по материалам
    public static final SpecialModels.Key TM_GBC = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/tm_gbc")
    );
    public static final SpecialModels.Key ALU_GBC = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/alu_gbc")
    );
    public static final SpecialModels.Key CI_GBC = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/ci_gbc")
    );

    // Мозги / Контроллер (BrainUpgrade)
    public static final SpecialModels.Key ENGINE_CONTROLLER = SpecialModels.register(
            ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", "block/partial/engine_controller")
    );

    // Этот метод принудительно вызывается в главном клиентском классе для инициализации
    public static void init() {}
}