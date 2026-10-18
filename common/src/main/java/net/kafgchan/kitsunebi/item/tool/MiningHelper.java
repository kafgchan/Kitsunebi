package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * 狐火ツールの範囲採掘・鉱脈採掘・トンネル掘削・一括伐採ヘルパー
 */
public class MiningHelper {

    private static final Set<BlockPos> CURRENTLY_BREAKING = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 面採掘 (3x3 または 5x5)
     */
    public static void breakArea(ServerLevel level, ServerPlayer player, BlockPos centerPos, int radius, ItemStack tool) {
        Direction face = player.getDirection();
        // プレイヤーの視線から破壊面を推定
        double pitch = player.getXRot();
        boolean vertical = pitch > 60 || pitch < -60;

        int broken = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x == 0 && y == 0) continue;
                BlockPos target = vertical
                        ? centerPos.offset(x, 0, y)
                        : (face.getAxis() == Direction.Axis.Z
                            ? centerPos.offset(x, y, 0)
                            : centerPos.offset(0, y, x));

                if (tryHarvestBlock(level, player, target, tool)) {
                    broken++;
                }
            }
        }
        if (broken > 0) {
            tool.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
    }

    /**
     * 同種連結鉱脈採掘 (最大128ブロック)
     */
    public static void breakVein(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, int maxBlocks) {
        BlockState targetState = level.getBlockState(origin);
        Block targetBlock = targetState.getBlock();
        if (targetState.isAir()) return;

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        int broken = 0;
        while (!queue.isEmpty() && broken < maxBlocks) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
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
        if (broken > 0) {
            tool.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
    }

    /**
     * トンネル掘削 (5x5x32)
     */
    public static void breakTunnel(ServerLevel level, ServerPlayer player, BlockPos start, ItemStack tool, int length) {
        Direction dir = player.getDirection();
        int broken = 0;

        for (int dist = 0; dist < length; dist++) {
            BlockPos sliceCenter = start.relative(dir, dist);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos target = (dir.getAxis() == Direction.Axis.Z)
                            ? sliceCenter.offset(dx, dy, 0)
                            : sliceCenter.offset(0, dy, dx);

                    if (tryHarvestBlock(level, player, target, tool)) {
                        broken++;
                    }
                }
            }
        }
        if (broken > 0) {
            tool.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
    }

    /**
     * 単木一括伐採 (最大512ブロック)
     */
    public static void fellTree(ServerLevel level, ServerPlayer player, BlockPos origin, ItemStack tool, int maxLogs) {
        BlockState originState = level.getBlockState(origin);
        if (!originState.is(BlockTags.LOGS)) return;

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        int broken = 0;
        while (!queue.isEmpty() && broken < maxLogs) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
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
        if (broken > 0) {
            tool.hurtAndBreak(broken, player, EquipmentSlot.MAINHAND);
        }
    }

    private static boolean tryHarvestBlock(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
            return false; // 岩盤などの破壊不能ブロックは保護
        }
        if (CURRENTLY_BREAKING.contains(pos)) {
            return false;
        }

        CURRENTLY_BREAKING.add(pos);
        try {
            return level.destroyBlock(pos, true, player);
        } finally {
            CURRENTLY_BREAKING.remove(pos);
        }
    }
}
