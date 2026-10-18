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
 * 幻影縦ハーフブロック
 * 高さ1ブロック、厚さ0.5ブロックの垂直板状ブロック。東西南北4方向に対応。
 */
public class PhantomVerticalSlabBlock extends Block implements IPhantomBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty DOUBLE = BooleanProperty.create("double");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.NORTH, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 8.0));
        SHAPES.put(Direction.SOUTH, Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 16.0));
        SHAPES.put(Direction.WEST, Block.box(0.0, 0.0, 0.0, 8.0, 16.0, 16.0));
        SHAPES.put(Direction.EAST, Block.box(8.0, 0.0, 0.0, 16.0, 16.0, 16.0));
    }

    public PhantomVerticalSlabBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DOUBLE, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public void playerDestroy(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, BlockPos pos, BlockState state, @Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity, net.minecraft.world.item.ItemStack tool) {
        int count = state.getValue(DOUBLE) ? 2 : 1;
        IPhantomBlock.handlePlayerDestroy(this, level, player, pos, state, blockEntity, tool, count);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(DOUBLE) ? Shapes.block() : SHAPES.get(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canPlayerInteract(context) ? (state.getValue(DOUBLE) ? Shapes.block() : SHAPES.get(state.getValue(FACING))) : Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return net.kafgchan.kitsunebi.client.KitsunebiClientHelper.isVisionActive()
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (state.getValue(DOUBLE) || !context.getItemInHand().is(this.asItem())) return false;
        Direction facing = state.getValue(FACING);
        double hit = facing.getAxis() == Direction.Axis.X
                ? context.getClickLocation().x - context.getClickedPos().getX()
                : context.getClickLocation().z - context.getClickedPos().getZ();
        if (context.getClickedFace().getAxis() == facing.getAxis())
            hit += context.getClickedFace().getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.00001 : -0.00001;
        return hit >= 0 && hit < 1 && ((facing == Direction.NORTH || facing == Direction.WEST) ? hit >= 0.5 : hit < 0.5);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) return existing.setValue(DOUBLE, true).setValue(WATERLOGGED, false);

        Direction clickedFace = context.getClickedFace();
        Direction facing;

        if (clickedFace.getAxis().isHorizontal()) {
            facing = clickedFace.getOpposite();
        } else {
            // 上下面をクリックした場合は、クリック位置のマス内オフセットで判定
            double hitX = context.getClickLocation().x - context.getClickedPos().getX();
            double hitZ = context.getClickLocation().z - context.getClickedPos().getZ();
            double dx = hitX - 0.5;
            double dz = hitZ - 0.5;

            if (Math.abs(dx) > Math.abs(dz)) {
                facing = dx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                facing = dz > 0 ? Direction.SOUTH : Direction.NORTH;
            }
        }

        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, facing)
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
        builder.add(FACING, WATERLOGGED, DOUBLE);
    }
}
