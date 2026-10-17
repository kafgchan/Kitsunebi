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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * 幻影縦クォーターブロック（1/4）
 * 高さ16/16、厚さ4/16。東西南北の向きと厚さ方向の4段階（layer: 0..3）に対応する薄型壁ブロック。
 */
public class PhantomVerticalQuarterBlock extends Block implements IPhantomBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty EXTRA = IntegerProperty.create("extra", 0, 15);
    public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, 3);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, VoxelShape[]> SHAPES = new EnumMap<>(Direction.class);

    static {
        // NORTH (Z: 0->16)
        SHAPES.put(Direction.NORTH, new VoxelShape[] {
                Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
                Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 8.0),
                Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 12.0),
                Block.box(0.0, 0.0, 12.0, 16.0, 16.0, 16.0)
        });
        // SOUTH (Z: 16->0)
        SHAPES.put(Direction.SOUTH, new VoxelShape[] {
                Block.box(0.0, 0.0, 12.0, 16.0, 16.0, 16.0),
                Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 12.0),
                Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 8.0),
                Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0)
        });
        // WEST (X: 0->16)
        SHAPES.put(Direction.WEST, new VoxelShape[] {
                Block.box(0.0, 0.0, 0.0, 4.0, 16.0, 16.0),
                Block.box(4.0, 0.0, 0.0, 8.0, 16.0, 16.0),
                Block.box(8.0, 0.0, 0.0, 12.0, 16.0, 16.0),
                Block.box(12.0, 0.0, 0.0, 16.0, 16.0, 16.0)
        });
        // EAST (X: 16->0)
        SHAPES.put(Direction.EAST, new VoxelShape[] {
                Block.box(12.0, 0.0, 0.0, 16.0, 16.0, 16.0),
                Block.box(8.0, 0.0, 0.0, 12.0, 16.0, 16.0),
                Block.box(4.0, 0.0, 0.0, 8.0, 16.0, 16.0),
                Block.box(0.0, 0.0, 0.0, 4.0, 16.0, 16.0)
        });
    }

    public PhantomVerticalQuarterBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(EXTRA, 0)
                .setValue(LAYER, 0)
                .setValue(WATERLOGGED, false));
    }

    private VoxelShape getVoxelShape(BlockState state) {
        return stackedShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canPlayerInteract(context) ? getVoxelShape(state) : Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return net.kafgchan.kitsunebi.client.KitsunebiClientHelper.isVisionActive()
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }


    private VoxelShape stackedShape(BlockState state) {
        int mask = state.getValue(EXTRA) | (1 << state.getValue(LAYER));
        VoxelShape result = Shapes.empty();
        for (int i=0;i<4;i++) if ((mask & (1 << i)) != 0) result = Shapes.or(result, SHAPES.get(state.getValue(FACING))[i]);
        return result;
    }

    private int addition(BlockState state, BlockPlaceContext context) {
        Direction.Axis axis = state.getValue(FACING).getAxis();
        double hit = (axis == Direction.Axis.X ? context.getClickLocation().x - context.getClickedPos().getX() : context.getClickLocation().z - context.getClickedPos().getZ());
        if (context.getClickedFace().getAxis() == axis)
            hit += context.getClickedFace().getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.00001 : -0.00001;
        if ((state.getValue(FACING) == Direction.SOUTH || state.getValue(FACING) == Direction.EAST)) hit = 1-hit;
        if (hit < 0 || hit >= 1) return -1;
        int slot = Math.min(3, (int)(hit*4));
        return ((state.getValue(EXTRA) | (1 << state.getValue(LAYER))) & (1 << slot)) == 0 ? slot : -1;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(this.asItem()) && addition(state, context) >= 0;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            int slot = addition(existing, context);
            if (slot < 0) return null;
            int mask = existing.getValue(EXTRA) | (1 << existing.getValue(LAYER)) | (1 << slot);
            return existing.setValue(EXTRA, mask).setValue(WATERLOGGED, mask != 15 && existing.getValue(WATERLOGGED));
        }

        Direction clickedFace = context.getClickedFace();
        Direction facing;
        int layer;

        if (clickedFace.getAxis().isHorizontal()) {
            facing = clickedFace.getOpposite();
            layer = 0;
        } else {
            // 上下をクリックした場合は視線方向
            facing = context.getHorizontalDirection().getOpposite();
            // クリック位置の深さに応じて layer を決定
            double hitZ = context.getClickLocation().z - context.getClickedPos().getZ();
            double hitX = context.getClickLocation().x - context.getClickedPos().getX();
            if (facing.getAxis() == Direction.Axis.Z) {
                double val = facing == Direction.NORTH ? hitZ : (1.0 - hitZ);
                layer = Math.min(3, Math.max(0, (int) (val * 4.0)));
            } else {
                double val = facing == Direction.WEST ? hitX : (1.0 - hitX);
                layer = Math.min(3, Math.max(0, (int) (val * 4.0)));
            }
        }

        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(LAYER, layer)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXTRA, FACING, LAYER, WATERLOGGED);
    }
}
