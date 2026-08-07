package com.renyigesai.bakeries.common.blocks.letter_tile;

import com.mojang.serialization.MapCodec;
import com.renyigesai.bakeries.common.utils.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class LetterTileBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public LetterTileBlock(Identifier identifier) {
        super(Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(ResourceKey.create(Registries.BLOCK,identifier)));
    }

    public LetterTileBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> box(1, 1, 15, 15, 15, 16);
            case NORTH -> box(1, 1, 0, 15, 15, 1);
            case EAST -> box(15, 1, 1, 16, 15, 15);
            case WEST -> box(0, 1, 1, 1, 15, 15);
        };
    }


    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getItem() instanceof NameTagItem){
            if (blockEntity instanceof LetterTileBlockEntity sign){
                String string = itemInHand.getHoverName().getString();
                int maxLength = Math.min(string.length(), 2);
                String text = string.substring(0, maxLength);
                sign.setText(text);
                return InteractionResult.SUCCESS;
            }
        }else {
            if (itemInHand.getItem() instanceof DyeItem){
                if (blockEntity instanceof LetterTileBlockEntity sign){
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(LetterTileBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new LetterTileBlockEntity(blockPos, blockState);
    }
}
