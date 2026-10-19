package net.kafgchan.kitsunebi.fabric.client;

import net.kafgchan.kitsunebi.client.KitsunebiKeyHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Fabric 側キー入力ハンドラー（共通 KitsunebiKeyHandler への委譲）
 */
public class KitsunebiFabricKeyHandler {

    public static final KeyMapping KEY_SKILL = KitsunebiKeyHandler.KEY_SKILL;
    public static final KeyMapping KEY_TOGGLE = KitsunebiKeyHandler.KEY_TOGGLE;
    public static final KeyMapping KEY_MODE_NEXT = KitsunebiKeyHandler.KEY_MODE_NEXT;
    public static final KeyMapping KEY_MODE_PREVIOUS = KitsunebiKeyHandler.KEY_MODE_PREVIOUS;
    public static final KeyMapping KEY_FEET_TOGGLE = KitsunebiKeyHandler.KEY_FEET_TOGGLE;

    public static void init() {
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(KEY_SKILL);
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(KEY_TOGGLE);
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(KEY_MODE_NEXT);
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(KEY_MODE_PREVIOUS);
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(KEY_FEET_TOGGLE);
    }

    public static void onClientTick(Minecraft mc) {
        KitsunebiKeyHandler.onClientTick(mc);
    }
}
