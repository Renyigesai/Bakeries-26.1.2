package com.renyigesai.bakeries.common.blocks.bread_rack;

import com.mojang.serialization.MapCodec;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GlassBreadRackBlock extends BreadRackBlock {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public GlassBreadRackBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN,false).setValue(TYPE,Type.SINGLE).setValue(SINGLE,false));
    }

    @Override
    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(GlassBreadRackBlock::new);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand hand, BlockHitResult hitResult) {
        if (pLevel.isClientSide()){
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (!(blockEntity instanceof BreadRackBlockEntity rackBlock)){
            return InteractionResult.FAIL;
        }
        boolean open = pState.getValue(OPEN);
        ItemStack itemInHand = pPlayer.getItemInHand(hand);
        if (open && pPlayer.isShiftKeyDown()){
            return super.take(rackBlock,pState,pLevel,pPos,pPlayer,hitResult);
        }
        if (rackBlock.getItemsCount() == 4){
            pLevel.blockEvent(pPos,pState.getBlock(), 0,open ? 1 : 0);
            pLevel.setBlock(pPos,pState.setValue(OPEN,!open),3);
            pLevel.playSound(null,pPos, open? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.8F, 0.8F);
            return InteractionResult.SUCCESS;
        }
        if (itemInHand.isEmpty()){
            pLevel.blockEvent(pPos,pState.getBlock(), 0,open ? 1 : 0);
            pLevel.setBlock(pPos,pState.setValue(OPEN,!open),3);
            pLevel.playSound(null,pPos, open? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.8F, 0.8F);
        }else {
            if (open){
                return super.useItemOn(stack, pState, pLevel, pPos, pPlayer, hand, hitResult);
            }else {
                pLevel.blockEvent(pPos,pState.getBlock(), 0,0);
                pLevel.setBlock(pPos,pState.setValue(OPEN,true),3);
                pLevel.playSound(null,pPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.PLAYERS, 0.8F, 0.8F);
            }
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return createTickerHelper(pBlockEntityType, BakeriesBlocks.Entities.BREAD_RACK_ENTITY.get(), BreadRackBlockEntity::clientTick);
        }
        return null;
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pType, BlockEntityType<E> pExpectedType, BlockEntityTicker<? super E> pTicker) {
        return pExpectedType == pType ? (BlockEntityTicker<A>) pTicker : null;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof BreadRackBlockEntity && blockEntity.triggerEvent(id, param);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,TYPE,SINGLE,OPEN);
    }
}
