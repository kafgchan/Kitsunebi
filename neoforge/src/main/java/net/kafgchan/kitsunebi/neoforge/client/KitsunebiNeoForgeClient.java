package net.kafgchan.kitsunebi.neoforge.client;

import net.kafgchan.kitsunebi.client.KitsunebiClientHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;


public class KitsunebiNeoForgeClient {
    public static void onModifyModels(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll(net.kafgchan.kitsunebi.client.SmoothSlopeModel::replace);
    }
    public static void onRegisterMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(net.kafgchan.kitsunebi.registry.ModMenus.KITSUNEBI_ALTAR_MENU.get(), net.kafgchan.kitsunebi.client.KitsunebiAltarScreen::new);
    }
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 装備・手持ちの変更時のみ可視化モデルを再構築
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post tickEvent) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                net.kafgchan.kitsunebi.neoforge.verification.AltarGuiVerifier.tick(mc);
                if (KitsunebiClientHelper.tickClient(mc.player, mc.level) && mc.level != null && mc.player != null) {
                    KitsunebiClientHelper.refreshVisionSections(mc.level, mc.player.blockPosition(),
                            mc.options.renderDistance().get() + 1, mc.levelRenderer::setSectionDirty);
                }
            }
        });
    }
}
