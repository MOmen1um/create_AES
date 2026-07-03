package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

// Наследуемся от SafeBlockEntityRenderer, как это сделано в Diesel Generators!
public class V8EngineRenderer extends SafeBlockEntityRenderer<V8EngineBlockEntity> {

    public V8EngineRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    protected void renderSafe(V8EngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Базовый безопасный рендер. Оставляем пустым!
        // Create сам подхватит и закрутит 3D-вал на блоке, так как у нас в V8EngineBlock прописан метод hasShaftTowards
    }
}





