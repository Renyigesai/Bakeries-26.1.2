package com.renyigesai.bakeries.common.blocks.oven;


import com.mojang.serialization.MapCodec;
import com.renyigesai.bakeries.common.init.BakeriesBlocks;
import com.renyigesai.bakeries.common.init.BakeriesSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class OvenBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape S_BOX = box(0, 0, 1, 16, 14, 16);
    public static final VoxelShape N_BOX = box(0, 0, 0, 16, 14, 15);
    public static final VoxelShape E_BOX = box(1, 0, 0, 16, 14, 16);
    public static final VoxelShape W_BOX = box(0, 0, 0, 15, 14, 16);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public OvenBlock(Identifier identifier) {
        super(BlockBehaviour.Properties.of().strength(3.5F).setId(ResourceKey.create(Registries.BLOCK,identifier)));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    public OvenBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(OvenBlock::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> S_BOX;
            case NORTH -> N_BOX;
            case EAST -> E_BOX;
            case WEST -> W_BOX;
        };
    }

    @Override
    public int getLightEmission(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        if(pState.getValue(LIT)){
            return 10;
        }else {
            return 0;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new OvenBlockEntity(pPos, pState);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? createTickerHelper(pBlockEntityType, BakeriesBlocks.Entities.OVEN_ENTITY.get(),
                OvenBlockEntity::clientTick) : createTickerHelper(pBlockEntityType, BakeriesBlocks.Entities.OVEN_ENTITY.get(),
                OvenBlockEntity::serverTick);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public boolean triggerEvent(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            return this.openContainer(level, pos, player);
        }
    }
    protected InteractionResult openContainer(
            Level level,
            BlockPos pos,
            Player player
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof OvenBlockEntity ovenBlockEntity)) {
            throw new IllegalStateException("Oven block entity is missing at " + pos);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        serverPlayer.openMenu(ovenBlockEntity, pos);

        level.blockEvent(
                pos,
                level.getBlockState(pos).getBlock(),
                0,
                0
        );

        level.playSound(
                null,
                pos,
                BakeriesSounds.OVEN_OPEN.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        level.gameEvent(player, GameEvent.BLOCK_OPEN, pos);

        return InteractionResult.CONSUME;
    }

    @Override
    public MenuProvider getMenuProvider(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void animateTick(BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull RandomSource pRandom) {
        if (pState.getValue(LIT)) {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY();
            double d2 = (double)pPos.getZ() + 0.5D;
            if (pRandom.nextDouble() < 0.3D) {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = pState.getValue(FACING).getClockWise();
            Direction.Axis direction$axis = direction.getAxis();
            for (int i = 0; i < 4 ; i++) {
                double d3 = 0.52D;
                double d4 = pRandom.nextDouble() * 0.6D - 0.3D;
                double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.6D : d4;
                double d6 = pRandom.nextDouble() * 6.0D / 16.0D;
                double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.6D : d4;
                pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            }
        }
    }

}
