package net.kafgchan.kitsunebi.network;

import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * マルチローダー共通ネットワーク送信用ラッパー
 */
public class KitsunebiNetwork {

    public static void sendToServer(CustomPacketPayload packet) {
        Services.PLATFORM.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        Services.PLATFORM.sendToPlayer(player, packet);
    }
}
