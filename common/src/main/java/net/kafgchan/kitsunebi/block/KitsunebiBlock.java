package net.kafgchan.kitsunebi.block;

import net.kafgchan.kitsunebi.item.KitsunebiItem;
import net.kafgchan.kitsunebi.item.KitsunebiToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 狐火ブロック（Common）
 */
public class KitsunebiBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // 中央にコンパクトな選択ボックス (8x8x8)
    private static final VoxelShape SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    private final String colorName;

    public KitsunebiBlock(String colorName, BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .noCollission()
                .instabreak()
                .sound(SoundType.AMETHYST)
                .lightLevel(state -> state.getValue(LIT) ? 15 : 0)
                .noOcclusion());

        this.colorName = colorName;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, true)
                .setValue(VISIBLE, false)
                .setValue(WATERLOGGED, false));
    }

    public String getColorName() {
        return this.colorName;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            if (isHoldingKitsunebiRelatedItem(player)) {
                return SHAPE;
            }
        }
        return Shapes.empty();
    }

    public static boolean isHoldingKitsunebiRelatedItem(Player player) {
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return isRelatedItem(main) || isRelatedItem(off);
    }

    public static boolean isRelatedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof KitsunebiItem || stack.getItem() instanceof KitsunebiToolItem;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean isWater = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState()
                .setValue(WATERLOGGED, isWater)
                .setValue(LIT, true)
                .setValue(VISIBLE, false);
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
        builder.add(LIT, VISIBLE, WATERLOGGED);
    }
}
