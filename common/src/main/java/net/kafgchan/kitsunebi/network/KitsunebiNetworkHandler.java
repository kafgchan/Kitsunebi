package net.kafgchan.kitsunebi.network;

import net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager;
import net.kafgchan.kitsunebi.item.tool.IKitsunebiTool;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 妖術パケットおよび空中ジャンプパケットのサーバー側処理ハンドラー
 */
public class KitsunebiNetworkHandler {

    public static void handleSkillAction(ServerPlayer player, int action) {
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof IKitsunebiTool tool) {
            switch (action) {
                case 0 -> tool.executeSkill(stack, player);
                case 1 -> tool.cycleMode(stack, player, true);  // Shift+V 次
                case 2 -> tool.cycleMode(stack, player, false); // Ctrl+V 前
                case 3 -> tool.toggleActive(stack, player);     // B トグル
            }
        }
    }

    public static void handleAirWalk(ServerPlayer player, boolean jump) {
        if (player != null && jump) {
            KitsunebiAirWalkManager.onPlayerJumpInput(player);
        }
    }
}
