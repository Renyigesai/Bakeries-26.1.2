package com.renyigesai.bakeries.common.blocks.luminous_light_sign;

import com.mojang.serialization.MapCodec;
import com.renyigesai.bakeries.BakeriesMod;
import com.renyigesai.bakeries.common.blocks.HorizontalConnectBlock;
import com.renyigesai.bakeries.common.utils.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class LuminousLightSignBlock extends HorizontalConnectBlock implements EntityBlock {
    public LuminousLightSignBlock(Identifier identifier) {
        super(Properties.ofFullCopy(Blocks.GLASS).lightLevel(litBlockEmission(15)).setId(ResourceKey.create(Registries.BLOCK,identifier)));
    }

    public LuminousLightSignBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> box(0, 10, 6, 16, 14, 10);
            case NORTH -> box(0, 10, 6, 16, 14, 10);
            case EAST -> box(6, 10, 0, 10, 14, 16);
            case WEST -> box(6, 10, 0, 10, 14, 16);
        };
    }

    @Override
    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(LuminousLightSignBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getItem() instanceof NameTagItem){
            if (blockEntity instanceof LuminousLightSignBlockEntity sign){
                Component hoverName = itemInHand.getHoverName();
                int length = BakeriesMod.clientUtilsMeasurer.getLength(hoverName.getString(), 45);
                String string = hoverName.getString(length);
                sign.setText(string);
                return InteractionResult.SUCCESS;
            }
        }else {
            if (itemInHand.getItem() instanceof DyeItem){
                if (blockEntity instanceof LuminousLightSignBlockEntity sign){
                    DyeColor dyeColor = itemInHand.get(DataComponents.DYE);
                    if (dyeColor != null) {
                        sign.setColor(dyeColor.getTextColor());
                        ItemUtils.shrink(itemInHand, 1, player);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    public boolean propagatesSkylightDown(BlockState p_154824_, BlockGetter p_154825_, BlockPos p_154826_) {
        return true;
    }

    public int getLightBlock(BlockState p_154828_, BlockGetter p_154829_, BlockPos p_154830_) {
        return 0;
    }

    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(this) ? true : super.skipRendering(pState, pAdjacentBlockState, pSide);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new LuminousLightSignBlockEntity(pPos,pState);
    }

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return value -> lightValue;
    }
}
