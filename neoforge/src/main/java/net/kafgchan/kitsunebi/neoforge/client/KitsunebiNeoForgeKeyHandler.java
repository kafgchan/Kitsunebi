package net.kafgchan.kitsunebi.neoforge.client;

import net.kafgchan.kitsunebi.client.KitsunebiKeyHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * NeoForge 側キー入力ハンドラー（共通 KitsunebiKeyHandler への委譲）
 */
@EventBusSubscriber(modid = net.kafgchan.kitsunebi.Constants.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class KitsunebiNeoForgeKeyHandler {

    public static final KeyMapping KEY_SKILL = KitsunebiKeyHandler.KEY_SKILL;
    public static final KeyMapping KEY_TOGGLE = KitsunebiKeyHandler.KEY_TOGGLE;
    public static final KeyMapping KEY_MODE_NEXT = KitsunebiKeyHandler.KEY_MODE_NEXT;
    public static final KeyMapping KEY_MODE_PREVIOUS = KitsunebiKeyHandler.KEY_MODE_PREVIOUS;
    public static final KeyMapping KEY_FEET_TOGGLE = KitsunebiKeyHandler.KEY_FEET_TOGGLE;

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        // Architectury KeyMappingRegistry で登録されるが、NeoForge イベントリスナー側でも安全に登録
        event.register(KEY_SKILL);
        event.register(KEY_TOGGLE);
        event.register(KEY_MODE_NEXT);
        event.register(KEY_MODE_PREVIOUS);
        event.register(KEY_FEET_TOGGLE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        KitsunebiKeyHandler.onClientTick(Minecraft.getInstance());
    }
}
