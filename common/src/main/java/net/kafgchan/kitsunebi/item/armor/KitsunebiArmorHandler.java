package net.kafgchan.kitsunebi.item.armor;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 狐火防具の統合イベントハンドラー
 * - 防具装着数カウント（1〜4部位）
 * - ダメージ軽減（1部位:85%, 2部位:90%, 3部位:95%, 4部位:100%完全無効）
 * - ノックバック耐性（4部位時: 100%）
 * - 落下無効（足袋）、炎・溶岩無効（羽織）
 * - 狐火反撃ロジック（羽織装備時、装着数に応じ64/128/256/512ダメージ、再帰防止）
 */
public class KitsunebiArmorHandler {

    // 反撃の再帰連鎖を防止するためのスレッドローカルまたはフラグ管理
    private static final Set<Entity> RETALIATING_ENTITIES = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 装備している狐火防具の部位数をカウント (0〜4)
     */
    public static int getKitsunebiArmorCount(LivingEntity entity) {
        if (entity == null) return 0;
        int count = 0;
        if (isKitsunebiPiece(entity.getItemBySlot(EquipmentSlot.HEAD))) count++;
        if (isKitsunebiPiece(entity.getItemBySlot(EquipmentSlot.CHEST))) count++;
        if (isKitsunebiPiece(entity.getItemBySlot(EquipmentSlot.LEGS))) count++;
        if (isKitsunebiPiece(entity.getItemBySlot(EquipmentSlot.FEET))) count++;
        return count;
    }

    public static boolean isKitsunebiPiece(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof KitsunebiArmorItem;
    }

    public static boolean hasHaori(LivingEntity entity) {
        if (entity == null) return false;
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getItem() instanceof KitsunebiHaoriItem;
    }

    public static boolean hasTabi(LivingEntity entity) {
        if (entity == null) return false;
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);
        return !feet.isEmpty() && feet.getItem() instanceof KitsunebiTabiItem;
    }

    /**
     * ダメージ処理前の計算および反撃発動
     * @return 軽減後の最終ダメージ（0.0F の場合はダメージ無効化）
     */
    public static float processDamage(LivingEntity victim, DamageSource source, float amount) {
        if (victim == null || amount <= 0.0F) return amount;

        // 奈落、/kill などの絶対的ダメージは軽減・無効化の例外
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            return amount;
        }

        int armorCount = getKitsunebiArmorCount(victim);
        if (armorCount == 0) return amount;

        // 1. 狐火反撃ロジック (被ダメージが無効化される前、攻撃元が存在すれば反撃を実行)
        if (hasHaori(victim)) {
            Entity attacker = source.getEntity();
            if (attacker == null) {
                attacker = source.getDirectEntity();
            }
            if (attacker instanceof LivingEntity livingAttacker && attacker != victim) {
                executeRetaliation(victim, livingAttacker, armorCount);
            }
        }

        // 2. 足袋装備時の落下ダメージ完全無効化
        if (hasTabi(victim) && source.is(DamageTypeTags.IS_FALL)) {
            return 0.0F;
        }

        // 3. 羽織装備時の炎・溶岩ダメージ完全無効化
        if (hasHaori(victim) && source.is(DamageTypeTags.IS_FIRE)) {
            return 0.0F;
        }

        // 4. 防具装着数によるダメージ軽減
        return switch (armorCount) {
            case 4 -> 0.0F;            // 4部位: 100%軽減（正確に0ダメージ）
            case 3 -> amount * 0.05F;  // 3部位: 95%軽減
            case 2 -> amount * 0.10F;  // 2部位: 90%軽減
            case 1 -> amount * 0.15F;  // 1部位: 85%軽減
            default -> amount;
        };
    }

    /**
     * 狐火反撃の実行（再帰防止付き）
     */
    private static void executeRetaliation(LivingEntity victim, LivingEntity attacker, int armorCount) {
        if (RETALIATING_ENTITIES.contains(victim) || RETALIATING_ENTITIES.contains(attacker)) {
            return; // 再帰連鎖禁止
        }

        RETALIATING_ENTITIES.add(victim);
        try {
            float retaliationDamage = switch (armorCount) {
                case 4 -> 512.0F;
                case 3 -> 256.0F;
                case 2 -> 128.0F;
                default -> 64.0F;
            };

            DamageSource retaliationSource = victim.damageSources().magic();
            attacker.hurt(retaliationSource, retaliationDamage);

            // 演出パーティクル
            if (victim.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        attacker.getX(), attacker.getY() + attacker.getBbHeight() * 0.5D, attacker.getZ(),
                        30, 0.4D, 0.4D, 0.4D, 0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        attacker.getX(), attacker.getY() + attacker.getBbHeight() * 0.5D, attacker.getZ(),
                        30, 0.4D, 0.4D, 0.4D, 0.08D
                );
            }
        } finally {
            RETALIATING_ENTITIES.remove(victim);
        }
    }

    /**
     * 4部位装備時のノックバック無効化判定
     */
    public static boolean shouldCancelKnockback(LivingEntity entity) {
        return getKitsunebiArmorCount(entity) >= 4;
    }
}
