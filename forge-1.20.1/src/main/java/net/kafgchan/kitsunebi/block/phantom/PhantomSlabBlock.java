package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 幻影横ハーフブロック
 * バニラのハーフブロック（下付き・上付き・ダブル）相当。
 */
public class PhantomSlabBlock extends SlabBlock implements IPhantomBlock {
    public PhantomSlabBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noLootTable()
                .noOcclusion());
    }

    @Override
    public void playerDestroy(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity, net.minecraft.world.item.ItemStack tool) {
        int count = state.getValue(TYPE) == net.minecraft.world.level.block.state.properties.SlabType.DOUBLE ? 2 : 1;
        IPhantomBlock.handlePlayerDestroy(this, level, player, pos, state, blockEntity, tool, count);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getShape(state, level, pos, context);
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
