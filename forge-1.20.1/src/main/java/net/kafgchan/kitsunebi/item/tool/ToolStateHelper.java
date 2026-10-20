package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
/**
 * 狐火ツールの状態（妖術ON/OFF、現在選択中のモード）を保持・更新するヘルパー
 */
public class ToolStateHelper {
    private static final String KEY_ACTIVE = "KitsunebiSkillActive";
    private static final String KEY_MODE = "KitsunebiSkillMode";

    public static boolean isActive(ItemStack stack) {
        CompoundTag data = stack.getTag();
        if (data == null) return true; // デフォルトON
        CompoundTag tag = data;
        return !tag.contains(KEY_ACTIVE) || tag.getBoolean(KEY_ACTIVE);
    }

    public static void setActive(ItemStack stack, boolean active) {
        updateTag(stack, tag -> tag.putBoolean(KEY_ACTIVE, active));
    }

    public static int getMode(ItemStack stack) {
        CompoundTag data = stack.getTag();
        if (data == null) return stack.getItem() instanceof KitsunebiAxeItem ? 1 : 0;
        CompoundTag tag = data;
        return tag.contains(KEY_MODE) ? tag.getInt(KEY_MODE) : (stack.getItem() instanceof KitsunebiAxeItem ? 1 : 0);
    }

    public static void setMode(ItemStack stack, int mode) {
        updateTag(stack, tag -> tag.putInt(KEY_MODE, Math.max(0, mode)));
    }

    public static void cycleMode(ItemStack stack, int totalModes, boolean forward) {
        if (totalModes <= 0) return;
        int current = getMode(stack);
        int next = forward ? (current + 1) % totalModes : (current - 1 + totalModes) % totalModes;
        setMode(stack, next);
    }

    private static void updateTag(ItemStack stack, java.util.function.Consumer<CompoundTag> consumer) {
        consumer.accept(stack.getOrCreateTag());
    }
}
