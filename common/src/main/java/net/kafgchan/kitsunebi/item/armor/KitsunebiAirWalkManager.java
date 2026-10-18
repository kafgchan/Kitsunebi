package net.kafgchan.kitsunebi.item.armor;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 狐火の足袋: 空中走行（仮想足場）マネージャー
 * - 実ブロックを設置せず、足元に追従する仮想浮遊・走行足場
 * - 2段ジャンプで起動、走行/停止/待機、3段ジャンプで解除落下
 * - 白朱赤の狐火パーティクル同期
 */
public class KitsunebiAirWalkManager {

    private static final Set<UUID> AIR_WALKING_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<UUID> JUMPED_ONCE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void onPlayerJumpInput(ServerPlayer player) {
        if (!KitsunebiArmorHandler.hasTabi(player)) {
            AIR_WALKING_PLAYERS.remove(player.getUUID());
            return;
        }

        UUID uuid = player.getUUID();
        if (AIR_WALKING_PLAYERS.contains(uuid)) {
            // 3回目のジャンプ入力: 解除して落下
            AIR_WALKING_PLAYERS.remove(uuid);
        } else if (!player.onGround()) {
            // 空中でのジャンプ入力（2段ジャンプ）: 仮想足場を起動
            AIR_WALKING_PLAYERS.add(uuid);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.0, motion.z);
            player.hasImpulse = true;
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();

        // 着地または足袋未装備時は解除
        if (player.onGround() || !KitsunebiArmorHandler.hasTabi(player) || !player.isAlive()) {
            AIR_WALKING_PLAYERS.remove(uuid);
            return;
        }

        if (AIR_WALKING_PLAYERS.contains(uuid)) {
            // 落下を防止（Y速度を0に保ちつつ、プレイヤーの水平移動は維持）
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < 0.0) {
                player.setDeltaMovement(motion.x, 0.0, motion.z);
                player.hasImpulse = true;
            }
            player.fallDistance = 0.0F;

            // 白・朱赤の狐火パーティクル演出
            if (player.level() instanceof ServerLevel serverLevel) {
                double px = player.getX();
                double py = player.getY();
                double pz = player.getZ();

                serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.2, 0.0, 0.2, 0.01);
                serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0.2, 0.0, 0.2, 0.01);
            }
        }
    }
}
