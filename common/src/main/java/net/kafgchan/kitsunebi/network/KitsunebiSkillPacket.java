package net.kafgchan.kitsunebi.network;

import net.kafgchan.kitsunebi.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 妖術操作パケット
 * action:
 * 0 = 妖術発動 (V)
 * 1 = 次のモード (Shift+V)
 * 2 = 前のモード (Ctrl+V)
 * 3 = 妖術ON/OFFトグル (B)
 */
public record KitsunebiSkillPacket(int action) implements CustomPacketPayload {
    public static final Type<KitsunebiSkillPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "skill_action"));
    public static final StreamCodec<FriendlyByteBuf, KitsunebiSkillPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, KitsunebiSkillPacket::action,
            KitsunebiSkillPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
