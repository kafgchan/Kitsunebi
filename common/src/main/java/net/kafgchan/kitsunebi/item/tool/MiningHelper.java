package net.kafgchan.kitsunebi.item.tool;

import net.kafgchan.kitsunebi.config.KitsunebiConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * 狐火ツールの範囲採掘・鉱脈採掘・トンネル掘削・一括伐採ヘルパー
 * - 通常採掘経路 (player.gameMode.destroyBlock) による保護Mod・幸運・シルクタッチ完全対応
 * - 自然木判定による建築物保護
 * - Config連携および厳密な上限保証
 */
public class MiningHelper {

    private static final Set<BlockPos> CURRENTLY_BREAKING = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 面採掘 (3x3 または 5x5)
     */
    public static void breakArea(ServerLevel level, ServerPlayer player, BlockPos centerPos, int radius, ItemStack tool) {
        Direction face = player.getDirection();
        double pitch = player.getXRot();
        boolean vertical = pitch > 60 || pitch < -60;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x == 0 && y == 0) continue;
                if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) return;

                BlockPos target = vertical
                        ? centerPos.offset(x, 0, y)
                        : (face.getAxis() == Direction.Axis.Z
                            ? centerPos.offset(x, y, 0)
                            : centerPos.offset(0, y, x));

                tryHarvestBlock(level, player, target, tool);
            }
        }
    }

    /**
     * 同種連結鉱脈採掘 (最大上限は Config に準拠)
     */
    public static void breakVein(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, int requestedMax) {
        BlockState targetState = level.getBlockState(origin);
        Block targetBlock = targetState.getBlock();
        if (targetState.isAir()) return;

        int maxBlocks = Math.min(requestedMax, KitsunebiConfig.get().maxVeinBlocks);
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        int broken = 0;
        while (!queue.isEmpty() && broken < maxBlocks) {
            if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) break;
            BlockPos current = queue.poll();

            for (Direction dir : Direction.values()) {
                if (broken >= maxBlocks) break;
                BlockPos neighbor = current.relative(dir);
                if (visited.add(neighbor)) {
                    BlockState neighborState = level.getBlockState(neighbor);
                    if (neighborState.is(targetBlock)) {
                        queue.add(neighbor);
                        if (tryHarvestBlock(level, player, neighbor, tool)) {
                            broken++;
                        }
                    }
                }
            }
        }
    }

    /**
     * トンネル掘削 (5x5x32、Config上限準拠)
     */
    public static void breakTunnel(ServerLevel level, ServerPlayer player, BlockPos start, ItemStack tool, int requestedLength) {
        Direction dir = player.getDirection();
        int length = Math.min(requestedLength, KitsunebiConfig.get().tunnelLength);

        for (int dist = 0; dist < length; dist++) {
            if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) break;
            BlockPos sliceCenter = start.relative(dir, dist);

            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = 0; dy <= 4; dy++) {
                    if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) return;

                    BlockPos target = (dir.getAxis() == Direction.Axis.Z)
                            ? sliceCenter.offset(dx, dy, 0)
                            : sliceCenter.offset(0, dy, dx);

                    tryHarvestBlock(level, player, target, tool);
                }
            }
        }
    }

    /**
     * 単木一括伐採 (自然木判定付き、最大上限は Config に準拠)
     */
    public static void fellTree(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, int requestedMax) {
        BlockState originState = level.getBlockState(origin);
        if (!originState.is(BlockTags.LOGS)) return;

        // 自然木判定: 原木塊の周囲に葉が存在するか検証（葉のない原木建築物の破壊を防止）
        if (!isNaturalTree(level, origin)) {
            return;
        }

        int maxLogs = Math.min(requestedMax, KitsunebiConfig.get().maxTreeFellLogs);
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        int broken = 0;
        while (!queue.isEmpty() && broken < maxLogs) {
            if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) break;
            BlockPos current = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (broken >= maxLogs) break;

                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (visited.add(neighbor)) {
                            BlockState state = level.getBlockState(neighbor);
                            if (state.is(BlockTags.LOGS)) {
                                queue.add(neighbor);
                                if (tryHarvestBlock(level, player, neighbor, tool)) {
                                    broken++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 起点原木から上部・周囲に葉 (BlockTags.LEAVES) が一定数存在するか検査し、自然木か判定
     */
    private static boolean isNaturalTree(ServerLevel level, BlockPos origin) {
        int leavesCount = 0;
        // 原木の直上から最大30ブロック上、半径4ブロック以内の葉を探索
        for (int y = 0; y <= 30; y++) {
            BlockPos currentY = origin.above(y);
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos checkPos = currentY.offset(dx, 0, dz);
                    if (level.getBlockState(checkPos).is(BlockTags.LEAVES)) {
                        leavesCount++;
                        if (leavesCount >= 3) {
                            return true; // 3個以上の葉が見つかれば自然木と判定
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * プレイヤーの採掘権限、領域保護、幸運・シルクタッチドロップを尊重したブロック破壊
     */
    private static boolean tryHarvestBlock(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
            return false; // 岩盤などの破壊不能ブロックは保護
        }
        if (CURRENTLY_BREAKING.contains(pos)) {
            return false;
        }
        if (!level.mayInteract(player, pos)) {
            return false; // 保護領域や操作権限の確認
        }

        CURRENTLY_BREAKING.add(pos);
        try {
            // ServerPlayerGameMode.destroyBlock はツール情報、イベント発火、保護判定をすべて処理する
            return player.gameMode.destroyBlock(pos);
        } finally {
            CURRENTLY_BREAKING.remove(pos);
        }
    }
}
