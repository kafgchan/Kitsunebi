package net.kafgchan.kitsunebi.block.phantom;

import net.kafgchan.kitsunebi.util.KitsunebiVisionHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 幻影ブロック共通インターフェース
 */
public interface IPhantomBlock {
    /**
     * 指定のコンテキスト（プレイヤー）がブロックを選択・破壊可能（可視化状態）か判定
     */
    default boolean canPlayerInteract(CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            return KitsunebiVisionHelper.canSeePhantom(player);
        }
        // Engine queries (particles, support, cached shapes) have no player.
        // Only an actual player's ray cast should be hidden by vision equipment.
        return true;
    }

    /**
     * プレイヤーによるブロック破壊時のドロップ制御
     * 御幣または仮面＋シルクタッチ時のみドロップ、それ以外はドロップなし
     */
    static void handlePlayerDestroy(Block block, Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, int count) {
        player.awardStat(Stats.BLOCK_MINED.get(block));
        player.causeFoodExhaustion(0.005F);
        if (!level.isClientSide && KitsunebiVisionHelper.shouldDropBlock(player, tool)) {
            Block.popResource(level, pos, new ItemStack(block.asItem(), Math.max(1, count)));
        }
    }
}
