package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 幻影横クォーターブロック（1/4）
 * 高さ4/16、幅・奥行16/16。上下4段階（layer: 0..3）に配置可能な薄型スラブ。
 */
public class PhantomQuarterSlabBlock extends Block implements IPhantomBlock, SimpleWaterloggedBlock {
    public static final IntegerProperty EXTRA = IntegerProperty.create("extra", 0, 15);
    public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, 3);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 4.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 8.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0)
    };

    public PhantomQuarterSlabBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(EXTRA, 0)
                .setValue(LAYER, 0)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return stackedShape(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canPlayerInteract(context) ? stackedShape(state) : Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return net.kafgchan.kitsunebi.client.KitsunebiClientHelper.isVisionActive()
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }


    private VoxelShape stackedShape(BlockState state) {
        int mask = state.getValue(EXTRA) | (1 << state.getValue(LAYER));
        VoxelShape result = Shapes.empty();
        for (int i=0;i<4;i++) if ((mask & (1 << i)) != 0) result = Shapes.or(result, SHAPES[i]);
        return result;
    }

    private int addition(BlockState state, BlockPlaceContext context) {
        Direction.Axis axis = Direction.Axis.Y;
        double hit = context.getClickLocation().y - context.getClickedPos().getY();
        if (context.getClickedFace().getAxis() == axis)
            hit += context.getClickedFace().getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.00001 : -0.00001;
        if (false) hit = 1-hit;
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

        double hitY = context.getClickLocation().y - context.getClickedPos().getY();
        if (hitY < 0.0) hitY += 1.0;
        int layer = Math.min(3, Math.max(0, (int) (hitY * 4.0)));

        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
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
        builder.add(EXTRA, LAYER, WATERLOGGED);
    }
}
