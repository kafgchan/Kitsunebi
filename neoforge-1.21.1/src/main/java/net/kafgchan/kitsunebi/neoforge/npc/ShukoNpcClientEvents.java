package net.kafgchan.kitsunebi.neoforge.npc;

import net.kafgchan.kitsunebi.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Kept on the client mod bus so a dedicated server never resolves renderer classes. */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ShukoNpcClientEvents {
    @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ShukoNpcRegistry.SHUKO.get(), ShukoNpcRenderer::new);
    }

    private ShukoNpcClientEvents() {}
}
