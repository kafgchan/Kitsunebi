package net.kafgchan.kitsunebi.fabric;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.ModInitializer;
import net.kafgchan.kitsunebi.KitsunebiCommon;
import net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkMovePacket;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkStatePacket;
import net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler;
import net.kafgchan.kitsunebi.network.KitsunebiSkillPacket;
import net.minecraft.server.level.ServerPlayer;

public class KitsunebiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        KitsunebiCommon.init();
        if (Boolean.getBoolean("kitsunebi.verifyNetwork"))
            net.kafgchan.kitsunebi.fabric.verification.FabricNetworkVerifier.init();

        // The integrated server shares the client registry; dedicated servers need their own S2C codec.
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER) {
            NetworkManager.registerS2CPayloadType(KitsunebiAirWalkStatePacket.TYPE, KitsunebiAirWalkStatePacket.STREAM_CODEC);
        }

        // Architectury NetworkManager を用いたパケット受信登録
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                KitsunebiAirWalkMovePacket.TYPE,
                KitsunebiAirWalkMovePacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        KitsunebiAirWalkManager.setVerticalInput(player, payload.vertical());
                    }
                })
        );
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                KitsunebiSkillPacket.TYPE,
                KitsunebiSkillPacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        KitsunebiNetworkHandler.handleSkillAction(player, payload.action());
                    }
                })
        );
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                KitsunebiAirWalkPacket.TYPE,
                KitsunebiAirWalkPacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        KitsunebiNetworkHandler.handleAirWalk(player, payload.jump());
                    }
                })
        );

        // プレイヤーtick（足袋の空中走行等）
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                KitsunebiAirWalkManager.tickPlayer(player);
            }
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            KitsunebiAirWalkManager.clearPlayer(handler.player.getUUID());
        });
    }
}
