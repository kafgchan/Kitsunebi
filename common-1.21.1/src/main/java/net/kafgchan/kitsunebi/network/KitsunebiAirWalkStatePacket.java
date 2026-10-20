package net.kafgchan.kitsunebi.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server approval / cancellation of virtual footing, sent only to its owner. */
public record KitsunebiAirWalkStatePacket(boolean active) implements CustomPacketPayload {
    public static final Type<KitsunebiAirWalkStatePacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kitsunebi","air_walk_state"));
    public static final StreamCodec<FriendlyByteBuf,KitsunebiAirWalkStatePacket> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,KitsunebiAirWalkStatePacket::active,KitsunebiAirWalkStatePacket::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
