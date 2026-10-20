package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

/**
 * 階段・スロープ共通の5形状（STRAIGHT, INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT）自動接続計算ヘルパー
 */
public class StairSlopeConnectionHelper {

    public static StairsShape calculateShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        Half half = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HALF)
                ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HALF)
                : Half.BOTTOM;

        // 1. 正面（向いている側 pos.relative(facing)）の隣接ブロックとの外角（OUTER）接続判定
        BlockPos frontPos = pos.relative(facing);
        BlockState frontState = level.getBlockState(frontPos);
        if (isCompatibleStairOrSlope(state, frontState) && getHalf(frontState) == half) {
            Direction frontFacing = getFacing(frontState);
            if (frontFacing.getAxis() != facing.getAxis() && isDifferenceCompliant(state, level, pos, frontFacing.getOpposite())) {
                if (frontFacing == facing.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }
                return StairsShape.OUTER_RIGHT;
            }
        }

        // 2. 背面（反対側 pos.relative(facing.getOpposite())）の隣接ブロックとの内角（INNER）接続判定
        BlockPos backPos = pos.relative(facing.getOpposite());
        BlockState backState = level.getBlockState(backPos);
        if (isCompatibleStairOrSlope(state, backState) && getHalf(backState) == half) {
            Direction backFacing = getFacing(backState);
            if (backFacing.getAxis() != facing.getAxis() && isDifferenceCompliant(state, level, pos, backFacing)) {
                if (backFacing == facing.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }
                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean isDifferenceCompliant(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        BlockState neighbor = level.getBlockState(pos.relative(dir));
        return !isCompatibleStairOrSlope(state, neighbor) || getFacing(neighbor) != getFacing(state) || getHalf(neighbor) != getHalf(state);
    }

    public static boolean isCompatibleStairOrSlope(BlockState current, BlockState target) {
        StairSlopeSize a = size(current), b = size(target);
        return a != null && a == b;
    }

    private static StairSlopeSize size(BlockState state) {
        if (state.getBlock() instanceof PhantomStairBlock stair) return stair.getSize();
        if (state.getBlock() instanceof PhantomSlopeBlock slope) return slope.getSize();
        return null;
    }

    private static Direction getFacing(BlockState state) {
        return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    private static Half getHalf(BlockState state) {
        return state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HALF)
                ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HALF)
                : Half.BOTTOM;
    }
}
