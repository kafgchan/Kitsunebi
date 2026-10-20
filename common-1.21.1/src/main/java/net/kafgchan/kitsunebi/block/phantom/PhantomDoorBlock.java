package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 幻影扉
 * バニラの木の扉相当の開閉動作を持つ不可視扉。
 */
public class PhantomDoorBlock extends DoorBlock implements IPhantomBlock {
    public PhantomDoorBlock(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noLootTable()
                .noOcclusion());
    }

    @Override
    public void playerDestroy(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity, net.minecraft.world.item.ItemStack tool) {
        // UPPER / LOWER のどちらをプレイヤーが直接破壊しても1回だけ回収判定を行う
        // 相方の半分は近傍更新（updateShape）により playerDestroy を経由せず消滅するため二重ドロップしない
        IPhantomBlock.handlePlayerDestroy(this, level, player, pos, state, blockEntity, tool, 1);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 開閉操作ができるよう通常時も開閉当たり判定を保持するか、または可視化時のみ選択
        return canPlayerInteract(context) ? super.getShape(state, level, pos, context) : Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return net.kafgchan.kitsunebi.client.KitsunebiClientHelper.isVisionActive()
                ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }
}
