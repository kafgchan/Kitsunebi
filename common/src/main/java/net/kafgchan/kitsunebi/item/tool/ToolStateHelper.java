package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 狐火ツールの状態（妖術ON/OFF、現在選択中のモード）を保持・更新するヘルパー
 */
public class ToolStateHelper {
    private static final String KEY_ACTIVE = "KitsunebiSkillActive";
    private static final String KEY_MODE = "KitsunebiSkillMode";

    public static boolean isActive(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return true; // デフォルトON
        CompoundTag tag = data.copyTag();
        return !tag.contains(KEY_ACTIVE) || tag.getBoolean(KEY_ACTIVE);
    }

    public static void setActive(ItemStack stack, boolean active) {
        updateTag(stack, tag -> tag.putBoolean(KEY_ACTIVE, active));
    }

    public static int getMode(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        return data.copyTag().getInt(KEY_MODE);
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
        CustomData current = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = current.copyTag();
        consumer.accept(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
