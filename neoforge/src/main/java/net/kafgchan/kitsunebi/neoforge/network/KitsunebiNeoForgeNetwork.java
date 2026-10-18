package net.kafgchan.kitsunebi.neoforge.network;

import net.kafgchan.kitsunebi.Constants;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket;
import net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler;
import net.kafgchan.kitsunebi.network.KitsunebiSkillPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 側ネットワークパケット登録
 */
public class KitsunebiNeoForgeNetwork {

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).versioned("1.0.0");

        registrar.playToServer(
                KitsunebiSkillPacket.TYPE,
                KitsunebiSkillPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        KitsunebiNetworkHandler.handleSkillAction(player, payload.action());
                    }
                })
        );

        registrar.playToServer(
                KitsunebiAirWalkPacket.TYPE,
                KitsunebiAirWalkPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        KitsunebiNetworkHandler.handleAirWalk(player, payload.jump());
                    }
                })
        );
    }
}
