package net.kafgchan.kitsunebi.neoforge.npc;

import java.nio.file.Files;
import java.nio.file.Path;
import net.kafgchan.kitsunebi.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Opt-in real-world render fixture. Creates a fresh flat save only in the isolated run directory. */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ShukoNpcVerification {
    private static boolean started, prepared;
    private static volatile boolean ready;
    private static int ticks, phase = -1;
    private static String capture;
    private static final java.util.List<ShukoNpcEntity> crowd = new java.util.ArrayList<>();
    private static final java.util.List<Long> frameNanos = new java.util.ArrayList<>();
    private static final StringBuilder timings = new StringBuilder();
    private static long lastFrame;
    private static ShukoNpcEntity npc;
    private static final int[] TAILS = {0, 1, 3, 6, 9};

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("kitsunebi.verifyShukoNpc")) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (!started && mc.screen instanceof TitleScreen title) {
                started = true;
                var settings = new LevelSettings("Shuko NPC verification", GameType.CREATIVE, false,
                        Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel("shuko-npc-" + System.currentTimeMillis(), settings,
                        new WorldOptions(515150L, false, false),
                        registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT)
                                .value().createWorldDimensions(), title);
                return;
            }
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
            if (!prepared) {
                prepared = true;
                var playerId = mc.player.getUUID();
                mc.getSingleplayerServer().execute(() -> {
                    try {
                        var server = mc.getSingleplayerServer();
                        var level = server.overworld();
                        level.setDayTime(6000);
                        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++)
                            level.setBlock(new BlockPos(x, 64, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                        npc = ShukoNpcRegistry.SHUKO.get().create(level);
                        if (npc == null) throw new AssertionError("NPC factory returned null");
                        npc.setNoAi(true);
                        npc.moveTo(0, 65, 0, 180, 0);
                        npc.setYHeadRot(180);
                        npc.yBodyRot = 180;
                        verifyData(npc);
                        level.addFreshEntity(npc);
                        var player = server.getPlayerList().getPlayer(playerId);
                        player.setGameMode(GameType.SPECTATOR);
                        player.teleportTo(0, 64.4, -6);
                        player.setYRot(0);
                        player.setXRot(0);
                        ready = true;
                    } catch (Throwable error) { fail(mc, error); }
                });
                return;
            }
            if (!ready || !mc.level.entitiesForRendering().iterator().hasNext()) return;
            mc.options.hideGui = true;
            if (++ticks % 80 == 1) {
                phase++;
                if (phase > 21) {
                    Files.writeString(Path.of("shuko-npc-verification.txt"),
                            "PASS: real integrated-server flat world, registered Shuko NPC rendered in front/right/back/left views; "
                            + "six synchronized face textures, 0/1/3/6/9 tails, idle/walk/talk render captures; NBT roundtrip and clamps checked. "
                            + "Server-to-client appearance data verified. Face/chest/obi closeups captured. "
                            + "Screenshots require visual review; this is not a multiplayer test or general FPS guarantee.\n" + timings);
                    mc.stop();
                    return;
                }
                if (phase == 21) recordTiming(1);
                int stage = phase;
                var playerId = mc.player.getUUID();
                mc.getSingleplayerServer().execute(() -> {
                    try {
                        var player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
                        npc.getNavigation().stop();
                        npc.setNoAi(true);
                        npc.moveTo(0, 65, 0, 180, 0);
                        npc.setYHeadRot(180);
                        npc.yBodyRot = 180;
                        npc.setTailCount(stage >= 10 && stage < 15 ? TAILS[stage - 10] : 9);
                        npc.setExpression(stage >= 4 && stage < 10 ? stage - 4 : 0);
                        double x = stage == 1 ? -6 : stage == 3 ? 6 : 0;
                        double z = stage == 2 ? 6 : stage == 1 || stage == 3 ? 0 : -6;
                        float yaw = stage == 1 ? -90 : stage == 2 ? 180 : stage == 3 ? 90 : 0;
                        double cameraY = 64.4;
                        float pitch = 0;
                        if (stage >= 17 && stage <= 19) {
                            z = -2.3;
                            cameraY = stage == 17 ? 64.8 : stage == 18 ? 64.4 : 64.0;
                        }
                        if (stage >= 20) {
                            x = 0; z = -10; cameraY = 68; pitch = 16;
                        }
                        player.teleportTo(player.serverLevel(), x, cameraY, z, yaw, pitch);
                        if (stage == 21) {
                            for (int i = 1; i < 16; i++) {
                                var extra = ShukoNpcRegistry.SHUKO.get().create(npc.level());
                                if (extra == null) throw new AssertionError("Crowd factory");
                                extra.setNoAi(true);
                                extra.moveTo((i % 4 - 1.5) * 2, 65, (i / 4) * 2, 180, 0);
                                extra.setYHeadRot(180);
                                extra.yBodyRot = 180;
                                player.serverLevel().addFreshEntity(extra);
                                crowd.add(extra);
                            }
                        }
                        if (stage == 15) {
                            npc.setNoAi(false);
                            npc.getNavigation().moveTo(3, 65, 0, 0.7);
                        }
                        if (stage == 16) npc.playTalkAnimation();
                    } catch (Throwable error) { fail(mc, error); }
                });
            }
            // Allow teleport/data packets and animation controllers to settle before capturing.
            if (ticks % 80 == (phase == 16 ? 12 : 40)) {
                var synced = mc.level.getEntity(npc.getId());
                if (!(synced instanceof ShukoNpcEntity received)) throw new AssertionError("NPC did not synchronize to client");
                int expectedTail = phase >= 10 && phase < 15 ? TAILS[phase - 10] : 9;
                int expectedFace = phase >= 4 && phase < 10 ? phase - 4 : 0;
                if (received.getTailCount() != expectedTail || received.getExpression() != expectedFace)
                    throw new AssertionError("Client appearance mismatch at phase " + phase);
                if (!(mc.getEntityRenderDispatcher().getRenderer(received) instanceof ShukoNpcRenderer renderer))
                    throw new AssertionError("Standalone NPC renderer not registered");
                var model = renderer.getGeoModel();
                for (String animation : new String[]{"idle", "walk", "talk"})
                    if (model.getAnimation(received, "animation.shuko." + animation) == null)
                        throw new AssertionError("Missing animation " + animation);
                int visibleTails = 0;
                for (int i = 1; i <= 9; i++) {
                    var bone = model.getBone(String.format(java.util.Locale.ROOT, "tail_%02d", i)).orElseThrow();
                    boolean visible = ShukoNpcModel.isTailVisible(expectedTail, i);
                    if (bone.isHidden() == visible || bone.isHidingChildren() == visible)
                        throw new AssertionError("Tail subtree visibility " + i + " phase " + phase);
                    if (!bone.isHidden()) visibleTails++;
                }
                if (visibleTails != expectedTail) throw new AssertionError("Rendered tail count mismatch");
                capture = String.format(java.util.Locale.ROOT, "shuko-npc-%02d.png", phase);
                if (phase >= 20) { frameNanos.clear(); lastFrame = 0; }
            }
            if (phase == 21 && ticks % 80 == 79) recordTiming(16);
        } catch (Throwable error) { fail(mc, error); }
    }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (phase >= 20 && ticks % 80 >= 40) {
            long now = System.nanoTime();
            if (lastFrame != 0) frameNanos.add(now - lastFrame);
            lastFrame = now;
        }
        if (capture == null) return;
        var name = capture;
        capture = null;
        var mc = Minecraft.getInstance();
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(Path.of(name));
        } catch (Throwable error) { fail(mc, error); }
    }

    private static void recordTiming(int count) {
        var sorted = new java.util.ArrayList<>(frameNanos);
        if (sorted.isEmpty()) throw new AssertionError("No frame timing samples");
        java.util.Collections.sort(sorted);
        double mean = sorted.stream().mapToLong(Long::longValue).average().orElseThrow() / 1_000_000.0;
        double p95 = sorted.get(Math.min(sorted.size() - 1, (int) (sorted.size() * 0.95))) / 1_000_000.0;
        timings.append(String.format(java.util.Locale.ROOT,
                "%d NPC(s), %d samples, mean frame cadence %.3f ms, p95 %.3f ms; same camera/settings, 40-tick warmup; includes frame cap/vsync and whole-world cost.\n",
                count, sorted.size(), mean, p95));
        frameNanos.clear();
        lastFrame = 0;
    }

    private static void verifyData(ShukoNpcEntity entity) {
        var tag = new CompoundTag();
        for (int tail : TAILS) for (int face = 0; face < ShukoNpcEntity.EXPRESSIONS.length; face++) {
            entity.setTailCount(tail);
            entity.setExpression(face);
            entity.addAdditionalSaveData(tag);
            entity.setTailCount(0);
            entity.setExpression(0);
            entity.readAdditionalSaveData(tag);
            if (entity.getTailCount() != tail || entity.getExpression() != face) throw new AssertionError("Appearance NBT roundtrip");
        }
        entity.setTailCount(Integer.MAX_VALUE);
        entity.setExpression(Integer.MAX_VALUE);
        if (entity.getTailCount() != 9 || entity.getExpression() != 5) throw new AssertionError("Upper clamps");
        entity.setTailCount(Integer.MIN_VALUE);
        entity.setExpression(Integer.MIN_VALUE);
        if (entity.getTailCount() != 0 || entity.getExpression() != 0) throw new AssertionError("Lower clamps");
        entity.setTailCount(9);
        entity.setExpression(0);
    }

    private static void fail(Minecraft mc, Throwable error) {
        error.printStackTrace();
        try { Files.writeString(Path.of("shuko-npc-verification.txt"), "FAIL: " + error); }
        catch (Exception ignored) {}
        mc.execute(mc::stop);
    }

    private ShukoNpcVerification() {}
}
