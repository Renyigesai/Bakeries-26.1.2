package com.renyigesai.bakeries.common.client.renderer.blockentity.luminous_light_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.renyigesai.bakeries.common.blocks.luminous_light_sign.LuminousLightSignBlock;
import com.renyigesai.bakeries.common.blocks.luminous_light_sign.LuminousLightSignBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class LuminousLightSignRender implements BlockEntityRenderer<LuminousLightSignBlockEntity,LuminousLightSignRenderState> {

    private final Font font;
    public LuminousLightSignRender(BlockEntityRendererProvider.Context pContext) {
        this.font = pContext.font();
    }

    @Override
    public void extractRenderState(LuminousLightSignBlockEntity blockEntity, LuminousLightSignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.text = blockEntity.getText();
        state.color = blockEntity.getColor();
        state.facing = blockEntity.getBlockState().getValue(LuminousLightSignBlock.FACING);
    }

    @Override
    public LuminousLightSignRenderState createRenderState() {
        return new LuminousLightSignRenderState();
    }

    @Override
    public void submit(LuminousLightSignRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        renderText(state,poseStack,submitNodeCollector);
    }

    private void renderText(LuminousLightSignRenderState state,PoseStack poseStack, SubmitNodeCollector submitNodeCollector){
        poseStack.translate(0.5, 0.625 + 0.25, 0.5);
        poseStack.scale(0.023F, -0.025F, 0.023F);
        Direction direction = state.facing;
        float yRot = 0;
        if (direction == Direction.NORTH){
            yRot = 180;
        }
        if (direction == Direction.SOUTH){
            yRot = -180;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(direction.toYRot() + yRot));
        String text = state.text;
        if (text == null){
            return;
        }
        int textWidth = font.width(text);
        int color = state.color;
        submitNodeCollector.submitText(poseStack,(float) -textWidth / 2 + 1,1,Component.literal(text).getVisualOrderText(),false,Font.DisplayMode.NORMAL, state.lightCoords,color,0,15728880);
    }
}
