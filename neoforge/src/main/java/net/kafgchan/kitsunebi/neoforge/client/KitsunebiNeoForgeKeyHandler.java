package net.kafgchan.kitsunebi.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kafgchan.kitsunebi.item.armor.KitsunebiArmorHandler;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket;
import net.kafgchan.kitsunebi.network.KitsunebiSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * NeoForge 側キー入力ハンドラー
 * - V: 妖術発動
 * - Shift+V: 次のモード
 * - Ctrl+V: 前のモード
 * - B: 妖術ON/OFF
 * - 空中ジャンプ: 足袋の仮想足場
 */
@EventBusSubscriber(modid = net.kafgchan.kitsunebi.Constants.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class KitsunebiNeoForgeKeyHandler {

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

    private static boolean wasJumpPressed = false;

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_SKILL);
        event.register(KEY_TOGGLE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.getConnection() == null) {
            wasJumpPressed = false;
            return;
        }

        // 1. 妖術発動・モード切り替え (V, Shift+V, Ctrl+V)
        while (KEY_SKILL.consumeClick()) {
            int action = 0;
            if (Screen.hasShiftDown()) {
                action = 1; // 次のモード
            } else if (Screen.hasControlDown()) {
                action = 2; // 前のモード
            }
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(action)));
        }

        // 2. 妖術ON/OFFトグル (B)
        while (KEY_TOGGLE.consumeClick()) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(3)));
        }

        // 3. 空中走行（二段ジャンプ検知）
        boolean isJumpDown = mc.options.keyJump.isDown();
        if (isJumpDown && !wasJumpPressed) {
            if (!mc.player.onGround() && KitsunebiArmorHandler.hasTabi(mc.player)) {
                mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiAirWalkPacket(true)));
            }
        }
        wasJumpPressed = isJumpDown;
    }
}
