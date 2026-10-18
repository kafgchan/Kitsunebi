package net.kafgchan.kitsunebi.fabric;

import net.fabricmc.api.ModInitializer;
import net.kafgchan.kitsunebi.KitsunebiCommon;

public class KitsunebiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        KitsunebiCommon.init();

        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            float processed = net.kafgchan.kitsunebi.item.armor.KitsunebiArmorHandler.processDamage(entity, source, amount);
            return processed > 0.0F;
        });

        // 妖術・空中走行ネットワークパケット登録
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(net.kafgchan.kitsunebi.network.KitsunebiSkillPacket.TYPE, net.kafgchan.kitsunebi.network.KitsunebiSkillPacket.STREAM_CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket.TYPE, net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket.STREAM_CODEC);

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(net.kafgchan.kitsunebi.network.KitsunebiSkillPacket.TYPE, (payload, context) -> {
            context.server().execute(() -> net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler.handleSkillAction(context.player(), payload.action()));
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket.TYPE, (payload, context) -> {
            context.server().execute(() -> net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler.handleAirWalk(context.player(), payload.jump()));
        });

        // プレイヤーtick（足袋の空中走行等）
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.tickPlayer(player);
            }
        });
    }
}
