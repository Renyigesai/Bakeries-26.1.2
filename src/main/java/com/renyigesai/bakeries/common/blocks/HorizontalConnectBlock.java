package com.renyigesai.bakeries.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class HorizontalConnectBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty SINGLE = BooleanProperty.create("single");
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type",Type.class);

    protected HorizontalConnectBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE,Type.SINGLE).setValue(SINGLE,false));
    }

    @Override
    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(HorizontalConnectBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        boolean flag = context.isSecondaryUseActive();

        boolean leftConnected;
        boolean rightConnected;
        boolean single = false;
        Type newType = Type.SINGLE;

        if (!flag) {
            leftConnected = canConnectTo(level, pos, facing, this.defaultBlockState(), false);
            rightConnected = canConnectTo(level, pos, facing, this.defaultBlockState(), true);
            if (!(!leftConnected && !rightConnected)){
                if (leftConnected && rightConnected){
                    newType = Type.ALL;
                }else {
                    newType = leftConnected ? Type.LEFT : Type.RIGHT;
                }
            }

        }else {
            single = true;
        }
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(TYPE,newType)
                .setValue(SINGLE,single);
    }

    protected boolean canConnectTo(BlockGetter level, BlockPos pos, Direction facing, BlockState oleState, boolean isRight) {
        Direction leftDir = getLeft(facing);
        BlockPos neighborPos = isRight ? pos.relative(leftDir.getOpposite()) : pos.relative(leftDir);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (matchConnect(neighborState) && !neighborState.getValue(SINGLE)) {
            return neighborState.getValue(FACING) == facing;
        }
        return false;
    }

    public boolean matchConnect(BlockState state){
        return state.is(this);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(SINGLE)) {
            return state;
        }

        Direction facing = state.getValue(FACING);
        Direction leftDir = getLeft(facing);
        Direction rightDir = leftDir.getOpposite();
        if (direction == leftDir || direction == rightDir) {
            boolean leftConnected = canConnectTo(level, pos, facing, state, false);
            boolean rightConnected = canConnectTo(level, pos, facing, state, true);

            Type newType;
            if (leftConnected && rightConnected) {
                newType = Type.ALL;
            } else if (leftConnected) {
                newType = Type.LEFT;
            } else if (rightConnected) {
                newType = Type.RIGHT;
            } else {
                newType = Type.SINGLE;
            }
            state = state.setValue(TYPE, newType);
        }
        return state;
    }

    protected Direction getLeft(Direction direction){
        return direction.getCounterClockWise();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING,TYPE,SINGLE);
    }

    public enum Type implements StringRepresentable {
        ALL("all"),
        LEFT("left"),
        RIGHT("right"),
        SINGLE("single");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
