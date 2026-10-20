package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 狐火ツールの妖術・操作インターフェース
 */
public interface IKitsunebiTool {

    int getModeCount();

    String getModeTranslationKey(int mode);

    default void cycleMode(ItemStack stack, Player player, boolean forward) {
        ToolStateHelper.cycleMode(stack, getModeCount(), forward);
        notifyStatus(stack, player);
    }

    default void toggleActive(ItemStack stack, Player player) {
        boolean next = !ToolStateHelper.isActive(stack);
        ToolStateHelper.setActive(stack, next);
        notifyStatus(stack, player);
    }

    default void notifyStatus(ItemStack stack, Player player) {
        if (player != null && !player.level().isClientSide) {
            boolean active = ToolStateHelper.isActive(stack);
            int mode = ToolStateHelper.getMode(stack);
            String modeKey = getModeTranslationKey(mode);
            Component status = Component.translatable("message.kitsunebi.tool_status",
                    stack.getHoverName(),
                    Component.translatable(modeKey),
                    active ? Component.translatable("message.kitsunebi.active") : Component.translatable("message.kitsunebi.inactive")
            );
            player.displayClientMessage(status, true);
        }
    }

    /**
     * Vキーによる妖術発動
     */
    void executeSkill(ItemStack stack, ServerPlayer player);
}
