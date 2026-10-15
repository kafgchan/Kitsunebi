package net.kafgchan.kitsunebi.client;

import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class KitsunebiClientHelper {
    private static int scanTimer = 0;
    private static final List<TrackedKitsunebi> NEARBY_KITSUNEBI = new ArrayList<>();

    private record TrackedKitsunebi(BlockPos pos, ParticleOptions particle) {}

    public static void tickClient(Player player, Level level) {
        if (player == null || level == null) {
            NEARBY_KITSUNEBI.clear();
            return;
        }

        // 手に狐火または御幣を持っているか？
        boolean holding = KitsunebiBlock.isHoldingKitsunebiRelatedItem(player);

        if (!holding) {
            NEARBY_KITSUNEBI.clear();
            scanTimer = 0;
            return;
        }

        // 4tickに1回、周囲（水平±10ブロック、垂直±6ブロック）をスキャン
        if (--scanTimer <= 0) {
            scanTimer = 4;
            NEARBY_KITSUNEBI.clear();

            BlockPos center = player.blockPosition();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int dx = -10; dx <= 10; dx++) {
                for (int dy = -6; dy <= 6; dy++) {
                    for (int dz = -10; dz <= 10; dz++) {
                        mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        BlockState state = level.getBlockState(mutable);
                        if (state.getBlock() instanceof KitsunebiBlock kitsunebiBlock) {
                            ParticleOptions particle = getParticle(kitsunebiBlock.getColorName());
                            NEARBY_KITSUNEBI.add(new TrackedKitsunebi(mutable.immutable(), particle));
                        }
                    }
                }
            }
        }

        // 毎tick、検出された狐火ブロックの中心にパーティクルをスポーン（ふわっと浮かび上がる淡い光）
        for (TrackedKitsunebi tracked : NEARBY_KITSUNEBI) {
            BlockPos pos = tracked.pos();
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
            double y = pos.getY() + 0.4 + level.random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
            level.addParticle(tracked.particle(), x, y, z, 0.0, 0.02, 0.0);
        }
    }

    private static ParticleOptions getParticle(String color) {
        return switch (color) {
            case "white" -> ParticleTypes.END_ROD;
            case "blue" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "cyan" -> ParticleTypes.GLOW;
            case "green" -> ParticleTypes.HAPPY_VILLAGER;
            case "purple" -> ParticleTypes.WITCH;
            case "orange" -> ParticleTypes.FLAME;
            case "red" -> ParticleTypes.FLAME;
            default -> ParticleTypes.END_ROD;
        };
    }
}
