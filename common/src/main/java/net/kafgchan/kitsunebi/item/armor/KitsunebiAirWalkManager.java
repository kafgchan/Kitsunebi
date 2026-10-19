package net.kafgchan.kitsunebi.item.armor;

import net.kafgchan.kitsunebi.config.KitsunebiConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 狐火の足袋: 空中走行（仮想足場）マネージャー
 * - 状態機械 (GROUND -> FIRST_JUMP -> AIR_WALKING -> FALLING_LOCKED)
 * - 2段ジャンプで起動、走行/停止/待機、3段ジャンプで解除
 * - 解除後は着地するまで再起動不可（着地ラッチ）
 * - ログアウト・ディメンション移動・クリエイティブ飛行時の状態クリア
 */
public class KitsunebiAirWalkManager {

    public enum AirWalkState {
        GROUND,
        FIRST_JUMP,
        AIR_WALKING,
        FALLING_LOCKED
    }

    private static final Map<UUID, AirWalkState> PLAYER_STATES = new ConcurrentHashMap<>();

    public static void onPlayerJumpInput(ServerPlayer player) {
        if (!KitsunebiArmorHandler.hasTabi(player) || player.getAbilities().flying) {
            PLAYER_STATES.remove(player.getUUID());
            return;
        }

        UUID uuid = player.getUUID();
        AirWalkState current = PLAYER_STATES.getOrDefault(uuid, AirWalkState.GROUND);

        if (player.onGround()) {
            // 地上でのジャンプ: 1段ジャンプ状態へ
            PLAYER_STATES.put(uuid, AirWalkState.FIRST_JUMP);
        } else if (current == AirWalkState.FIRST_JUMP) {
            // 空中でのジャンプ（2段ジャンプ）: 空中走行起動
            PLAYER_STATES.put(uuid, AirWalkState.AIR_WALKING);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.0, motion.z);
            player.hasImpulse = true;
        } else if (current == AirWalkState.AIR_WALKING) {
            // 3回目のジャンプ: 解除して落下、着地まで再起動ロック
            PLAYER_STATES.put(uuid, AirWalkState.FALLING_LOCKED);
        }
        // FALLING_LOCKED の場合は着地するまで何もしない
    }

    public static void tickPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        AirWalkState state = PLAYER_STATES.get(uuid);
        if (state == null && !player.onGround()) return;

        // 地面に着地したら状態を GROUND にリセット
        if (player.onGround()) {
            PLAYER_STATES.put(uuid, AirWalkState.GROUND);
            return;
        }

        // 足袋未装備、死亡、飛行中は即時解除
        if (!KitsunebiArmorHandler.hasTabi(player) || !player.isAlive() || player.getAbilities().flying) {
            PLAYER_STATES.remove(uuid);
            return;
        }

        if (state == AirWalkState.AIR_WALKING) {
            // 落下を防止（Y速度を0に保ちつつ、プレイヤーの水平移動は維持）
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < 0.0) {
                player.setDeltaMovement(motion.x, 0.0, motion.z);
                player.hasImpulse = true;
            }
            player.fallDistance = 0.0F;

            // 白・朱赤の狐火パーティクル同期
            if (KitsunebiConfig.get().enableParticles && player.level() instanceof ServerLevel serverLevel) {
                double px = player.getX();
                double py = player.getY();
                double pz = player.getZ();

                serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.2, 0.0, 0.2, 0.01);
                serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0.2, 0.0, 0.2, 0.01);
            }
        }
    }

    public static void clearPlayer(UUID uuid) {
        PLAYER_STATES.remove(uuid);
    }
}
