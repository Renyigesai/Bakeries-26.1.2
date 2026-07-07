package com.renyigesai.bakeries.common.client.renderer.blockentity.bread_rack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.renyigesai.bakeries.api.ResourceLocation;
import com.renyigesai.bakeries.common.blocks.HorizontalConnectBlock;
import com.renyigesai.bakeries.common.blocks.bread_rack.BreadRackBlock;
import com.renyigesai.bakeries.common.blocks.bread_rack.BreadRackBlockEntity;
import com.renyigesai.bakeries.common.blocks.bread_rack.GlassBreadRackBlock;
import com.renyigesai.bakeries.common.client.model.GlassBreadRackDoorModel;
import com.renyigesai.bakeries.common.client.renderer.blockentity.BlockEntityItemRenderer;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BreadRackRender extends BlockEntityItemRenderer<BreadRackBlockEntity,BreadRackRenderState> {

    public static final float ADD_SIZE = 0.25f;
    public static final Vec2[][] VEC2S = {
            new Vec2[]{},
            new Vec2[]{new Vec2(0.5f, 0.5f)},
            new Vec2[]{new Vec2(0.5f + ADD_SIZE, 0.5f), new Vec2(0.5f - ADD_SIZE, 0.5f)},
            new Vec2[]{new Vec2(0.5f + ADD_SIZE, 0.5f + ADD_SIZE), new Vec2(0.5f - ADD_SIZE, 0.5f + ADD_SIZE), new Vec2(0.5f, 0.5f - ADD_SIZE)},
            new Vec2[]{new Vec2(0.5f + ADD_SIZE, 0.5f + ADD_SIZE), new Vec2(0.5f - ADD_SIZE, 0.5f + ADD_SIZE), new Vec2(0.5f + ADD_SIZE, 0.5f - ADD_SIZE), new Vec2(0.5f - ADD_SIZE, 0.5f - ADD_SIZE)}
    };
    public static final Identifier TEXTURE = ResourceLocation.fromNamespaceAndPath("bakeries","textures/entity/glass_bread_rack_door/glass_bread_rack_door.png");
    public static final Identifier TEXTURE_LEFT = ResourceLocation.fromNamespaceAndPath("bakeries","textures/entity/glass_bread_rack_door/glass_bread_rack_door_left.png");
    public static final Identifier TEXTURE_RIGHT = ResourceLocation.fromNamespaceAndPath("bakeries","textures/entity/glass_bread_rack_door/glass_bread_rack_door_right.png");
    public static final Identifier TEXTURE_ALL = ResourceLocation.fromNamespaceAndPath("bakeries","textures/entity/glass_bread_rack_door/glass_bread_rack_door_all.png");
    public final GlassBreadRackDoorModel model;

    public BreadRackRender(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new GlassBreadRackDoorModel(context.bakeLayer(GlassBreadRackDoorModel.LAYER_LOCATION));
    }

    @Override
    public NonNullList<ItemStack> items(BreadRackBlockEntity blockEntity) {
        return blockEntity.getInventory().getItems();
    }

    @Override
    public void extractRenderState(BreadRackBlockEntity blockEntity, @NotNull BreadRackRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity,state,breakProgress);
        NonNullList<ItemStack> items = items(blockEntity);
        int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());
        for (int slot = 0; slot < items.size(); ++slot) {
            ItemStack itemStack = items.get(slot);
            if (itemStack.isEmpty()) {
                itemStack = ItemStack.EMPTY;
            }
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
            this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.ON_SHELF, level(blockEntity), blockEntity, seed + slot);
            state.items[slot] = itemStackRenderState;
            if (itemStack.getItem() instanceof BlockItem blockItem){
                this.blockModelResolver.update(blockModelRenderState, blockItem.getBlock().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
                state.blockStates[slot] = blockModelRenderState;
            }
        }
        state.blockPos = blockEntity.getBlockPos();
        extract(blockEntity, state, partialTicks, cameraPosition, breakProgress);
    }

    @Override
    public void extract(BreadRackBlockEntity blockEntity, @NotNull BreadRackRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        Direction direction = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        state.facing = direction;
        boolean isOpen = blockEntity.getBlockState().hasProperty(GlassBreadRackBlock.OPEN);
        if (isOpen){
            state.isOpen = true;
            state.type = blockEntity.getBlockState().getValue(BreadRackBlock.TYPE);
            float doorX = getLeftByBlock(level(blockEntity),blockEntity.getBlockPos(),direction.getOpposite()).isAir() ? -12F : 12F;
            state.open = -blockEntity.getProgress(partialTicks) * doorX;
        }

    }

    protected BlockState getLeftByBlock(Level level, BlockPos pos, Direction direction){
        return level.getBlockState(pos.relative(direction.getCounterClockWise()));
    }

    @Override
    public Level level(BreadRackBlockEntity blockEntity) {
        return blockEntity.level();
    }

    @Override
    public void render(ItemStackRenderState[] itemStackRenderStates, BlockModelRenderState[] blockModelRenderStates, BreadRackRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        renderDoor(state,poseStack,submitNodeCollector);
        if (!(state.getInventoryCount() >= 1)){
            return;
        }
        for (int slot = 0; slot < blockModelRenderStates.length; ++slot) {
            BlockModelRenderState blockModelRenderState = blockModelRenderStates[slot];
            if (blockModelRenderState != null){
                Direction facing = state.facing;
                float rotation = facing.toYRot();
                Vec2 pos2d = transformPositionByDirection(VEC2S[state.getInventoryCount()][slot],facing);
                // ������Ʒ�������еľ������꣨���߶ȣ�
                Vec3 worldPos = transitionVec3(pos2d);
                poseStack.pushPose();
                // 1. ƽ�Ƶ���Ʒ������λ��
                poseStack.translate(worldPos.x, 0.5 + 0.046875 + (slot > 1 ? 0.453125 : 0), worldPos.z);
                // 2. ��Ʒ�������ת��15��΢�� + ������ת��
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation + 15));
                // 3. ����
                float size = 1.0f;
                poseStack.scale(size, size, size);
                // 4. ����ģ��ê�㣺ʹģ�����Ķ��뵱ǰԭ��
                poseStack.translate(-0.5, -0.5, -0.5);
                // 5. �ύ
                blockModelRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }

    private void renderDoor(BreadRackRenderState state, PoseStack poseStack,SubmitNodeCollector submitNodeCollector){
        if (!state.isOpen){
            return;
        }
        Direction direction = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(180F));
        poseStack.scale(0.9995F, 0.9995F, 0.9995F);

        Identifier location;
        HorizontalConnectBlock.Type type = state.type;
        switch (type){
            case ALL -> location = TEXTURE_ALL;
            case LEFT -> location = TEXTURE_LEFT;
            case RIGHT -> location = TEXTURE_RIGHT;
            default -> location = TEXTURE;
        }
        submitNodeCollector.submitModel(model,state.open,poseStack, location,state.lightCoords, OverlayTexture.NO_OVERLAY,0,state.breakProgress);
        poseStack.popPose();
    }

    @Override
    public BreadRackRenderState createRenderState() {
        return new BreadRackRenderState(4);
    }

    private Vec2 transformPositionByDirection(Vec2 position, Direction direction) {
        float x = position.x;
        float y = position.y;
        return switch (direction) {
            case NORTH -> new Vec2(1 - x, 1 - y);
            case SOUTH -> new Vec2(x, y);
            case EAST -> new Vec2(y, 1 - x);
            case WEST -> new Vec2(1 - y, x);
            default -> position;
        };
    }
}
