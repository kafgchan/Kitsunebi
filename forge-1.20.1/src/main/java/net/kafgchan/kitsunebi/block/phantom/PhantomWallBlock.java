package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 幻影太い柵（石の塀準拠）
 * バニラの石の塀（WallBlock）相当の柱・接続形状と衝突判定。
 */
public class PhantomWallBlock extends WallBlock implements IPhantomBlock {
    public PhantomWallBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noLootTable()
                .noOcclusion());
    }

    @Override
    public void playerDestroy(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity, net.minecraft.world.item.ItemStack tool) {
        IPhantomBlock.handlePlayerDestroy(this, level, player, pos, state, blockEntity, tool, 1);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canPlayerInteract(context) ? super.getShape(state, level, pos, context) : Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return net.kafgchan.kitsunebi.client.KitsunebiClientHelper.isVisionActive()
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }
}
