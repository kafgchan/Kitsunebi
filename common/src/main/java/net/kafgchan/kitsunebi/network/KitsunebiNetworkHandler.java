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
        if(action==4&&net.kafgchan.kitsunebi.item.armor.KitsunebiArmorHandler.hasTabi(player)) {
            var feet=player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
            boolean enabled=!net.kafgchan.kitsunebi.item.tool.ToolStateHelper.isActive(feet);
            net.kafgchan.kitsunebi.item.tool.ToolStateHelper.setActive(feet,enabled);
            if(!enabled)KitsunebiAirWalkManager.clearPlayer(player.getUUID());
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(enabled?"message.kitsunebi.air_walk_skill_on":"message.kitsunebi.air_walk_skill_off"),true);
            return;
        }
        if (stack.getItem() instanceof IKitsunebiTool tool) {
            switch (action) {
                case 0 -> {
                    if (net.kafgchan.kitsunebi.item.tool.ToolStateHelper.isActive(stack)) {
                        tool.executeSkill(stack, player);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("kitsunebi.skill.state.off"), true);
                    }
                }
                case 1 -> tool.cycleMode(stack, player, true);  // 次のモード
                case 2 -> tool.cycleMode(stack, player, false); // 前のモード
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
