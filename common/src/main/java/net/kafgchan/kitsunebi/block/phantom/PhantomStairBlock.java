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
 * 幻影階段ブロック（高・中・低の3サイズ規格対応）
 * バニラ階段準拠の内角・外角（5形状）自動接続に対応。
 */
public class PhantomStairBlock extends Block implements IPhantomBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final StairSlopeSize size;
    private final Map<String, VoxelShape> shapeCache = new HashMap<>();

    public PhantomStairBlock(StairSlopeSize size, BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noLootTable()
                .noOcclusion());
        this.size = size;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(WATERLOGGED, false));
        buildShapes();
    }

    @Override
    public void playerDestroy(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, BlockPos pos, BlockState state, @Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity, net.minecraft.world.item.ItemStack tool) {
        IPhantomBlock.handlePlayerDestroy(this, level, player, pos, state, blockEntity, tool, 1);
    }

    public StairSlopeSize getSize() {
        return this.size;
    }

    private void buildShapes() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Half half : Half.values()) {
                for (StairsShape shape : StairsShape.values()) {
                    String key = getKey(facing, half, shape);
                    shapeCache.put(key, createStairShape(facing, half, shape, this.size));
                }
            }
        }
    }

    private static String getKey(Direction facing, Half half, StairsShape shape) {
        return facing.getName() + "_" + half.getSerializedName() + "_" + shape.getSerializedName();
    }

    private static VoxelShape createStairShape(Direction facing, Half half, StairsShape shape, StairSlopeSize size) {
        double baseHeight = size == StairSlopeSize.HIGH ? 8.0 : 4.0;
        double stepHeight = size == StairSlopeSize.HIGH ? 8.0 : 4.0;

        // 土台 (base) とステップ (step) の正確な Y 座標計算（中・低サイズの上下反転を完全対応）
        double yBase = (half == Half.BOTTOM) ? 0.0 : (16.0 - baseHeight);
        double yStep0 = (half == Half.BOTTOM) ? baseHeight : (16.0 - baseHeight - stepHeight);
        double yStep1 = yStep0 + stepHeight;

        // 基準のスラブ部分
        VoxelShape base;
        if (size == StairSlopeSize.LOW) {
            // LOWは奥行き半分（8/16）かつ上下反転を考慮
            base = facing.getAxis() == Direction.Axis.Z
                    ? (facing == Direction.NORTH ? Block.box(0, yBase, 0, 16, yBase + baseHeight, 8) : Block.box(0, yBase, 8, 16, yBase + baseHeight, 16))
                    : (facing == Direction.WEST ? Block.box(0, yBase, 0, 8, yBase + baseHeight, 16) : Block.box(8, yBase, 0, 16, yBase + baseHeight, 16));
        } else {
            base = Block.box(0, yBase, 0, 16, yBase + baseHeight, 16);
        }

        if (size == StairSlopeSize.LOW && shape != StairsShape.STRAIGHT) {
            // Apply the same corner operation to the lower tier, not just its upper step.
            base = createStepQuarter(facing, shape, yBase, yBase + baseHeight, StairSlopeSize.MID);
        }

        // 上段のステップ部分（向きと形状に応じて配置）
        VoxelShape step = createStepQuarter(facing, shape, yStep0, yStep1, size);

        return Shapes.or(base, step);
    }

    private static VoxelShape createStepQuarter(Direction facing, StairsShape shape, double y0, double y1, StairSlopeSize size) {
        // LOWサイズの場合は奥行き4/16（下段8/16の半分）にして段差を生成
        double dMax = (size == StairSlopeSize.LOW) ? 4.0 : 8.0;

        VoxelShape northStep = Block.box(0, y0, 0, 16, y1, dMax);
        VoxelShape southStep = Block.box(0, y0, 16.0 - dMax, 16, y1, 16.0);
        VoxelShape westStep = Block.box(0, y0, 0, dMax, y1, 16);
        VoxelShape eastStep = Block.box(16.0 - dMax, y0, 0, 16.0, y1, 16);

        VoxelShape primary = switch (facing) {
            case NORTH -> northStep;
            case SOUTH -> southStep;
            case WEST -> westStep;
            case EAST -> eastStep;
            default -> northStep;
        };

        if (shape == StairsShape.STRAIGHT) {
            return primary;
        }

        // 角の補正
        Direction sideDir = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT
                ? facing.getCounterClockWise()
                : facing.getClockWise();

        VoxelShape side = switch (sideDir) {
            case NORTH -> northStep;
            case SOUTH -> southStep;
            case WEST -> westStep;
            case EAST -> eastStep;
            default -> northStep;
        };

        VoxelShape sideClipped = side;

        if (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) {
            return Shapes.or(primary, sideClipped);
        } else {
            // OUTER: 角の交差部分のみ
            return Shapes.join(primary, sideClipped, net.minecraft.world.phys.shapes.BooleanOp.AND);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        String key = getKey(state.getValue(FACING), state.getValue(HALF), state.getValue(SHAPE));
        return shapeCache.getOrDefault(key, Shapes.block());
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
