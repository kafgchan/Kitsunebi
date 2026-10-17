package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 幻影スロープブロック（高・中・低の3サイズ規格対応）
 * 通常の階段移動を使う3～4段の衝突形状。選択枠は16分割、表示は連続斜面。
 */
public class PhantomSlopeBlock extends Block implements IPhantomBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final StairSlopeSize size;
    private final Map<String, VoxelShape> shapeCache = new HashMap<>();
    private final Map<String, VoxelShape> collisionCache = new HashMap<>();

    public PhantomSlopeBlock(StairSlopeSize size, BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion());
        this.size = size;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(WATERLOGGED, false));
        buildSlopeShapes();
    }

    public StairSlopeSize getSize() {
        return this.size;
    }

    private void buildSlopeShapes() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                for (StairsShape shape : StairsShape.values()) {
                    String key = getKey(facing, half, shape);
                    shapeCache.put(key, createSlopeShape(facing, half, shape, this.size));
                    int collisionSteps = this.size == StairSlopeSize.LOW ? 3 : 4;
                    collisionCache.put(key, createSlopeShape(facing, half, shape, this.size, collisionSteps).optimize());
                }
            }
        }
    }

    private static String getKey(Direction facing, Half half, StairsShape shape) {
        return facing.getName() + "_" + half.getSerializedName() + "_" + shape.getSerializedName();
    }

    private static VoxelShape createSlopeShape(Direction facing, Half half, StairsShape shape, StairSlopeSize size) {
        return createSlopeShape(facing, half, shape, size, 16);
    }

    private static VoxelShape createSlopeShape(Direction facing, Half half, StairsShape shape, StairSlopeSize size, int slices) {
        double maxHeight = size.getMaxHeight();
        double maxDepth = size.getMaxDepth();
        double sliceH = maxHeight / slices;
        double stepD = maxDepth / slices;
        VoxelShape result = Shapes.empty();

        for (int i = 0; i < slices; i++) {
            // 上下反転（HALF）に対応した正確なY範囲
            double y0 = (half == Half.BOTTOM) ? (i * sliceH) : (16.0 - (i + 1) * sliceH);
            double y1 = (half == Half.BOTTOM) ? ((i + 1) * sliceH) : (16.0 - i * sliceH);

            double d0 = i * stepD;

            VoxelShape sliceShape = getDirectionalSlice(facing, shape, y0, y1, d0, maxDepth, size);
            result = Shapes.or(result, sliceShape);
        }

        return result;
    }

    private static VoxelShape getDirectionalSlice(Direction facing, StairsShape shape, double y0, double y1, double d0, double maxDepth, StairSlopeSize size) {
        double dLimit = maxDepth - d0;

        VoxelShape slice = switch (facing) {
            case NORTH -> Block.box(0, y0, 0, 16, y1, dLimit);
            case SOUTH -> Block.box(0, y0, 16.0 - dLimit, 16, y1, 16.0);
            case WEST -> Block.box(0, y0, 0, dLimit, y1, 16);
            case EAST -> Block.box(16.0 - dLimit, y0, 0, 16.0, y1, 16);
            default -> Block.box(0, y0, 0, 16, y1, 16);
        };

        if (shape == StairsShape.STRAIGHT) {
            return slice;
        }

        Direction sideDir = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT
                ? facing.getCounterClockWise()
                : facing.getClockWise();

        VoxelShape sideSlice = switch (sideDir) {
            case NORTH -> Block.box(0, y0, 0, 16, y1, dLimit);
            case SOUTH -> Block.box(0, y0, 16.0 - dLimit, 16, y1, 16.0);
            case WEST -> Block.box(0, y0, 0, dLimit, y1, 16);
            case EAST -> Block.box(16.0 - dLimit, y0, 0, 16.0, y1, 16);
            default -> Block.box(0, y0, 0, 16, y1, 16);
        };

        VoxelShape sideClipped = sideSlice;

        if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
            return Shapes.or(slice, sideClipped);
        } else {
            return Shapes.join(slice, sideClipped, net.minecraft.world.phys.shapes.BooleanOp.AND);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        String key = getKey(state.getValue(FACING), state.getValue(HALF), state.getValue(SHAPE));
        return collisionCache.getOrDefault(key, Shapes.block());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (canPlayerInteract(context)) {
            String key = getKey(state.getValue(FACING), state.getValue(HALF), state.getValue(SHAPE));
            return shapeCache.getOrDefault(key, Shapes.block());
        }
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return net.kafgchan.kitsunebi.client.KitsunebiClientHelper.isVisionActive()
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);

        Half half = clickedFace != Direction.DOWN && (clickedFace == Direction.UP || !(context.getClickLocation().y - (double)pos.getY() > 0.5))
                ? Half.BOTTOM
                : Half.TOP;

        BlockState state = this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(HALF, half)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return state.setValue(SHAPE, StairSlopeConnectionHelper.calculateShape(state, context.getLevel(), pos));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction.getAxis().isHorizontal()) {
            return state.setValue(SHAPE, StairSlopeConnectionHelper.calculateShape(state, level, pos));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SHAPE, WATERLOGGED);
    }
}
