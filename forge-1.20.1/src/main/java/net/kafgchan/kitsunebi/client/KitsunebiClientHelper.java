package net.kafgchan.kitsunebi.client;

import net.kafgchan.kitsunebi.util.KitsunebiVisionHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.kafgchan.kitsunebi.block.phantom.IPhantomBlock;

/** Client-local visibility, read by terrain compilation workers as well. */
public final class KitsunebiClientHelper {
    private static volatile boolean visionActive;
    private KitsunebiClientHelper() {}
    public static boolean isVisionActive() { return visionActive; }

    /** Returns true only when terrain needs rebuilding. No particles. */
    public static boolean tickClient(Player player, Level level) {
        boolean next = level != null && level.isClientSide && player != null
                && KitsunebiVisionHelper.canSeePhantom(player);
        if (next == visionActive) return false;
        visionActive = next;
        return true;
    }

    @FunctionalInterface
    public interface SectionDirtyMarker {
        void mark(int x, int y, int z);
    }

    /** Keep existing GPU meshes; only queue sections containing our models. */
    public static void refreshVisionSections(Level level, BlockPos center, int radius,
                                             SectionDirtyMarker marker) {
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                // Do not load chunks just to refresh visualization.
                var chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                var sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    var section = sections[i];
                    if (section.hasOnlyAir()) continue;
                    if (section.maybeHas(state -> state.getBlock() instanceof KitsunebiBlock
                            || state.getBlock() instanceof IPhantomBlock)) {
                        marker.mark(x, level.getSectionYFromSectionIndex(i), z);
                    }
                }
            }
        }
    }
}
