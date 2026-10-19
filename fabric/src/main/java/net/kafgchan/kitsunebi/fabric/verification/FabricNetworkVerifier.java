package net.kafgchan.kitsunebi.fabric.verification;

import dev.architectury.impl.NetworkAggregator.BufCustomPacketPayload;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.kafgchan.kitsunebi.network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

/** Opt-in dedicated-server regression test; never opens or changes a player world. */
public final class FabricNetworkVerifier {
    public static void init() {
        boolean connectionTest = Boolean.getBoolean("kitsunebi.verifyNetworkConnection");
        if (connectionTest) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                handler.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,
                        new net.minecraft.world.item.ItemStack(net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_TABI.get()));
                handler.player.connection.teleport(0.5, 250, 0.5, 0, 0);
            });
            net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> server.halt(false));
            final int[] ticks = {0};
            net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
                if (++ticks[0] > 2400) server.halt(false);
            });
        }
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            String result;
            try {
                if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER)
                    throw new AssertionError("Test must run without client initialization");
                for (boolean active : new boolean[]{true, false}) {
                    var packets = NetworkManager.toPackets(NetworkManager.s2c(), new KitsunebiAirWalkStatePacket(active), server.registryAccess());
                    if (packets.size() != 1 || !(packets.getFirst() instanceof ClientboundCustomPayloadPacket packet)
                            || !(packet.payload() instanceof BufCustomPacketPayload wrapped))
                        throw new AssertionError("Expected one server-to-client state packet");
                    var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(wrapped.payload()));
                    try {
                        if (KitsunebiAirWalkStatePacket.STREAM_CODEC.decode(buffer).active() != active || buffer.isReadable())
                            throw new AssertionError("State payload did not round trip");
                    } finally { buffer.release(); }
                }
                if (registeredCodec(PayloadTypeRegistry.playS2C(), KitsunebiAirWalkStatePacket.TYPE.id()) == null)
                    throw new AssertionError("Fabric wire codec missing for air-walk state");
                for (var id : java.util.List.of(KitsunebiSkillPacket.TYPE.id(), KitsunebiAirWalkPacket.TYPE.id(), KitsunebiAirWalkMovePacket.TYPE.id()))
                    if (registeredCodec(PayloadTypeRegistry.playC2S(), id) == null) throw new AssertionError("C2S codec missing: " + id);
                result = "PASS: Fabric dedicated server; activation/cancellation packets encoded and decoded; S2C and all three C2S types registered without client initialization.";
            } catch (Throwable error) {
                result = "FAIL: " + error;
                error.printStackTrace();
            }
            try { Files.writeString(Path.of("network-verification.txt"), result); }
            catch (Exception error) { throw new RuntimeException(error); }
            System.out.println(result);
            if (!connectionTest || !result.startsWith("PASS:")) server.halt(false);
        });
    }
    // Fabric 1.21.1 exposes codec lookup only on its registry implementation.
    private static Object registeredCodec(Object registry, net.minecraft.resources.ResourceLocation id) throws ReflectiveOperationException {
        return registry.getClass().getMethod("get", net.minecraft.resources.ResourceLocation.class).invoke(registry, id);
    }
    private FabricNetworkVerifier() {}
}
