package com.renyigesai.bakeries.common.client.renderer.blockentity.letter_tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.renyigesai.bakeries.common.blocks.letter_tile.LetterTileBlock;
import com.renyigesai.bakeries.common.blocks.letter_tile.LetterTileBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class LetterTileRender implements BlockEntityRenderer<LetterTileBlockEntity, LetterTileRenderState> {

    private final Font font;
    public LetterTileRender(BlockEntityRendererProvider.Context pContext) {
        this.font = pContext.font();
    }

    @Override
    public void extractRenderState(LetterTileBlockEntity blockEntity, LetterTileRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.text = blockEntity.getText();
        state.color = blockEntity.getColor();
        state.facing = blockEntity.getBlockState().getValue(LetterTileBlock.FACING);
    }

    @Override
    public LetterTileRenderState createRenderState() {
        return new LetterTileRenderState();
    }

    @Override
    public void submit(LetterTileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        renderText(state,poseStack,submitNodeCollector);
    }

    private void renderText(LetterTileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {

        String text = state.text;
        if (text == null) return;
        poseStack.translate(0.5, 1.0, 0.5);
        Direction direction = state.facing;
        float yRot = 0;
        if (direction == Direction.NORTH) {
            yRot = 180;
        } else if (direction == Direction.SOUTH) {
            yRot = -180;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(direction.toYRot() + yRot));

        poseStack.translate(0,0, -0.4375);

        float xScale = 1.0F / font.lineHeight;
        float zScale = 1.0F / font.lineHeight;
        float yScale = 1.0F / font.lineHeight;
        if (text.length() >= 2) {
            xScale = xScale - xScale / 2.5f;
        }
        poseStack.scale(xScale, -yScale, zScale);

        int textWidth = font.width(text);
        int color = state.color;
        submitNodeCollector.submitText(poseStack, (float) -textWidth / 2, 1, Component.literal(text).withStyle(ChatFormatting.BOLD).getVisualOrderText(), false, Font.DisplayMode.NORMAL, state.lightCoords, color, 0, 15728880);
    }
}
