package net.kafgchan.kitsunebi.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket;
import net.kafgchan.kitsunebi.network.KitsunebiSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * NeoForge 側キー入力ハンドラー
 * - V: 妖術発動
 * - N: 次のモード
 * - M: 前のモード
 * - B: ツール妖術ON/OFF
 * - G: 足袋妖術ON/OFF
 * - スニーク＋ジャンプ: 空中歩行の切り替え
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

    public static final KeyMapping KEY_MODE_NEXT = new KeyMapping("key.kitsunebi.mode_next", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.kitsunebi");

    public static final KeyMapping KEY_MODE_PREVIOUS = new KeyMapping("key.kitsunebi.mode_previous", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.kitsunebi");

    public static final KeyMapping KEY_FEET_TOGGLE = new KeyMapping("key.kitsunebi.feet_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.kitsunebi");

    private static final net.kafgchan.kitsunebi.client.AirWalkToggleInput AIR_WALK_TOGGLE=new net.kafgchan.kitsunebi.client.AirWalkToggleInput();

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_SKILL);
        event.register(KEY_TOGGLE);
        event.register(KEY_MODE_NEXT);
        event.register(KEY_MODE_PREVIOUS);
        event.register(KEY_FEET_TOGGLE);

    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.getConnection() == null) {
            while(KEY_SKILL.consumeClick()){ /* discard inputs received behind a GUI */ }
            while(KEY_TOGGLE.consumeClick()){ /* discard inputs received behind a GUI */ }
            while(KEY_MODE_NEXT.consumeClick()){ /* discard GUI input */ }
            while(KEY_MODE_PREVIOUS.consumeClick()){ /* discard GUI input */ }
            while(KEY_FEET_TOGGLE.consumeClick()){ /* discard GUI input */ }
            AIR_WALK_TOGGLE.suspend(mc.options.keyJump.isDown(),mc.options.keyShift.isDown());
            net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState.tick(mc.getConnection()==null?null:mc.player,false);
            sendAirWalkMovement(mc);
            return;
        }

        while (KEY_SKILL.consumeClick()) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(0)));
        }
        while (KEY_MODE_NEXT.consumeClick()) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(1)));
        }
        while (KEY_MODE_PREVIOUS.consumeClick()) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(2)));
        }
        while (KEY_TOGGLE.consumeClick()) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(3)));
        }
        while (KEY_FEET_TOGGLE.consumeClick()) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiSkillPacket(4)));
        }

        // Both keys must be released before another chord can toggle the mode.
        boolean isJumpDown=mc.options.keyJump.isDown(),sneak=mc.options.keyShift.isDown();
        if(AIR_WALK_TOGGLE.update(isJumpDown,sneak,net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.skillEnabled(mc.player))) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new KitsunebiAirWalkPacket(true)));
        }
        net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState.tick(mc.player,true,isJumpDown,sneak,mc.options.keySprint.isDown());
        sendAirWalkMovement(mc);
    }
    private static void sendAirWalkMovement(Minecraft mc) {
        Integer vertical=net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState.pollVerticalInput(mc.player);
        if(vertical!=null&&mc.getConnection()!=null)mc.getConnection().send(new ServerboundCustomPayloadPacket(new net.kafgchan.kitsunebi.network.KitsunebiAirWalkMovePacket(vertical.byteValue())));
    }
}
