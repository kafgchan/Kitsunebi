package net.kafgchan.kitsunebi.block.phantom;

import net.kafgchan.kitsunebi.util.KitsunebiVisionHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

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
}
