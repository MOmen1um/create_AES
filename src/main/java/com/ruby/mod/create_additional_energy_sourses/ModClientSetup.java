package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Create_additional_energy_sourses.MODID, value = Dist.CLIENT)
public class ModClientSetup {

    @SubscribeEvent
    public static void registerBlockEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.NON_MODULAR_ENGINE_ENTITY.get(), EngineShaftRenderer::new);
    }

    public static class EngineShaftRenderer implements BlockEntityRenderer<NonModularEnginesBlockEntity> {
        private final BlockEntityRendererProvider.Context context;

        public EngineShaftRenderer(BlockEntityRendererProvider.Context context) {
            this.context = context;
        }

        @Override
        public void render(NonModularEnginesBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
            if (be.getLevel() == null) return;

            BlockState state = be.getBlockState();
            if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return;

            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction.Axis axis = facing.getAxis();

            ms.pushPose();

            // 1. Сдвигаем матрицу в центр блока
            ms.translate(0.49, 0.5, 0.5);

            // 2. Разворачиваем саму модель вала по направлению двигателя
            // Используем встроенный метод Direction для точного поворота без путаницы в осях
            ms.mulPose(facing.getRotation());
            if (axis == Direction.Axis.X) {
                ms.mulPose(Axis.YP.rotationDegrees(90));
            }

            // 3. Получаем угол вращения из Create
            float angleInRadians = com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), axis);
            float angleInDegrees = (float) Math.toDegrees(angleInRadians);

            // 4. Поворачиваем строго вокруг продольной оси вала (в моделях Create это ось Z)
            ms.mulPose(Axis.YP.rotationDegrees(angleInDegrees));

            // Возвращаем центр на место
            ms.translate(-0.5, -0.5, -0.5);

            ResourceLocation shaftPath = ResourceLocation.fromNamespaceAndPath("create", "block/shaft");

            BakedModel shaftModel = Minecraft.getInstance().getModelManager().getModel(
                    new ModelResourceLocation(shaftPath, "standalone")
            );

            context.getBlockRenderDispatcher().getModelRenderer().renderModel(
                    ms.last(),
                    buffer.getBuffer(RenderType.solid()),
                    state,
                    shaftModel,
                    1.0f, 1.0f, 1.0f,
                    light,
                    overlay
            );

            ms.popPose();
        }
    }
}