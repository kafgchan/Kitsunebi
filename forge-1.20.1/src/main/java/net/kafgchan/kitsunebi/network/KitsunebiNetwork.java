package net.kafgchan.kitsunebi.network;

import net.kafgchan.kitsunebi.platform.Services;
import net.kafgchan.kitsunebi.network.KitsunebiPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * マルチローダー共通ネットワーク送信用ラッパー
 */
public class KitsunebiNetwork {

    public static void sendToServer(KitsunebiPacket packet) {
        Services.PLATFORM.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, KitsunebiPacket packet) {
        Services.PLATFORM.sendToPlayer(player, packet);
    }
}
