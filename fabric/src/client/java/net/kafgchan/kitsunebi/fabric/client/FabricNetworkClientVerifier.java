package net.kafgchan.kitsunebi.fabric.client;

import java.nio.file.Files;
import java.nio.file.Path;
import net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState;
import net.kafgchan.kitsunebi.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;

/** Opt-in real TCP connection check, with no world or item changes outside the test server. */
public final class FabricNetworkClientVerifier {
    private static int ticks, phase, deadline;
    private static boolean finished;
    public static void tick(Minecraft mc) {
        if (!Boolean.getBoolean("kitsunebi.verifyNetworkClient") || finished) return;
        ticks++;
        try {
            mc.options.pauseOnLostFocus = false;
            if (ticks > 1800) throw new AssertionError("Connection test timed out, phase " + phase);
            if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
            if (mc.player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) return;
            var owner = KitsunebiAirWalkClientState.class.getDeclaredField("owner");
            owner.setAccessible(true);
            boolean active = owner.get(null) == mc.player;
            if (phase == 0) {
                toggle(mc); phase = 1; deadline = ticks + 200;
            } else if (phase == 1 && active) {
                System.out.println("PASS: C2S activation reached dedicated server and S2C approval reached client");
                toggle(mc); phase = 2; deadline = ticks + 200;
            } else if (phase == 2 && !active) {
                finish(mc, "PASS: real Fabric client connected to dedicated Fabric server; C2S activation/cancellation and S2C state synchronization completed.");
            } else if (ticks > deadline) throw new AssertionError("Missing S2C reply, phase " + phase);
        } catch (Throwable error) { finish(mc, "FAIL: " + error); }
    }
    private static void toggle(Minecraft mc) {
        // The vanilla sneak command and custom request travel in order over the same connection.
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        KitsunebiNetwork.sendToServer(new KitsunebiAirWalkPacket(true));
    }
    private static void finish(Minecraft mc, String result) {
        finished = true;
        try { Files.writeString(Path.of("network-client-verification.txt"), result); }
        catch (Exception error) { throw new RuntimeException(error); }
        System.out.println(result);
        mc.stop();
    }
    private FabricNetworkClientVerifier() {}
}
