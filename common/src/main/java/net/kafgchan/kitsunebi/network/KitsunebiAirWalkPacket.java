package net.kafgchan.kitsunebi.network;

import net.kafgchan.kitsunebi.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * スニーク＋ジャンプによる空中歩行の切り替え要求
 */
public record KitsunebiAirWalkPacket(boolean jump) implements CustomPacketPayload {
    public static final Type<KitsunebiAirWalkPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "air_walk"));
    public static final StreamCodec<FriendlyByteBuf, KitsunebiAirWalkPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, KitsunebiAirWalkPacket::jump,
            KitsunebiAirWalkPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
