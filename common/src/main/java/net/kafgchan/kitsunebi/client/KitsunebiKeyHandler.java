package net.kafgchan.kitsunebi.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkMovePacket;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket;
import net.kafgchan.kitsunebi.network.KitsunebiNetwork;
import net.kafgchan.kitsunebi.network.KitsunebiSkillPacket;
import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 共通クライアントキー入力ハンドラー
 */
public class KitsunebiKeyHandler {

    public static final KeyMapping KEY_SKILL = new KeyMapping(
            "key.kitsunebi.skill",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.kitsunebi"
    );

    public static final KeyMapping KEY_TOGGLE = new KeyMapping(
            "key.kitsunebi.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.kitsunebi"
    );

    public static final KeyMapping KEY_MODE_NEXT = new KeyMapping(
            "key.kitsunebi.mode_next",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "key.categories.kitsunebi"
    );

    public static final KeyMapping KEY_MODE_PREVIOUS = new KeyMapping(
            "key.kitsunebi.mode_previous",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.kitsunebi"
    );

    public static final KeyMapping KEY_FEET_TOGGLE = new KeyMapping(
            "key.kitsunebi.feet_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.kitsunebi"
    );

    private static final AirWalkToggleInput AIR_WALK_TOGGLE = new AirWalkToggleInput();

    public static void init() {
    }

    public static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.screen != null || mc.getConnection() == null) {
            while (KEY_SKILL.consumeClick()) { /* discard inputs received behind a GUI */ }
            while (KEY_TOGGLE.consumeClick()) { /* discard inputs received behind a GUI */ }
            while (KEY_MODE_NEXT.consumeClick()) { /* discard GUI input */ }
            while (KEY_MODE_PREVIOUS.consumeClick()) { /* discard GUI input */ }
            while (KEY_FEET_TOGGLE.consumeClick()) { /* discard GUI input */ }
            AIR_WALK_TOGGLE.suspend(mc.options.keyJump.isDown(), mc.options.keyShift.isDown());
            KitsunebiAirWalkClientState.tick(mc.getConnection() == null ? null : mc.player, false);
            sendAirWalkMovement(mc);
            return;
        }

        while (KEY_SKILL.consumeClick()) {
            KitsunebiNetwork.sendToServer(new KitsunebiSkillPacket(0));
        }
        while (KEY_MODE_NEXT.consumeClick()) {
            KitsunebiNetwork.sendToServer(new KitsunebiSkillPacket(1));
        }
        while (KEY_MODE_PREVIOUS.consumeClick()) {
            KitsunebiNetwork.sendToServer(new KitsunebiSkillPacket(2));
        }
        while (KEY_TOGGLE.consumeClick()) {
            KitsunebiNetwork.sendToServer(new KitsunebiSkillPacket(3));
        }
        while (KEY_FEET_TOGGLE.consumeClick()) {
            KitsunebiNetwork.sendToServer(new KitsunebiSkillPacket(4));
        }

        // Both keys must be released before another chord can toggle the mode.
        boolean isJumpDown = mc.options.keyJump.isDown(), sneak = mc.options.keyShift.isDown();
        if (AIR_WALK_TOGGLE.update(isJumpDown, sneak, KitsunebiAirWalkManager.skillEnabled(mc.player))) {
            KitsunebiNetwork.sendToServer(new KitsunebiAirWalkPacket(true));
        }
        KitsunebiAirWalkClientState.tick(mc.player, true, isJumpDown, sneak, mc.options.keySprint.isDown());
        sendAirWalkMovement(mc);
    }

    private static void sendAirWalkMovement(Minecraft mc) {
        Integer vertical = KitsunebiAirWalkClientState.pollVerticalInput(mc.player);
        if (vertical != null && mc.getConnection() != null) {
            KitsunebiNetwork.sendToServer(new KitsunebiAirWalkMovePacket(vertical.byteValue()));
        }
    }
}
