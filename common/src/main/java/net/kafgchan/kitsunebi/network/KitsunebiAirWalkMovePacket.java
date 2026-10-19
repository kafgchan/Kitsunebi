package net.kafgchan.kitsunebi.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** -1 descend, 0 hover, +1 ascend; server validates equipment and active mode. */
public record KitsunebiAirWalkMovePacket(byte vertical) implements CustomPacketPayload {
    public static final Type<KitsunebiAirWalkMovePacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kitsunebi","air_walk_move"));
    public static final StreamCodec<FriendlyByteBuf,KitsunebiAirWalkMovePacket> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BYTE,KitsunebiAirWalkMovePacket::vertical,KitsunebiAirWalkMovePacket::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
