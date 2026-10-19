package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 狐火の剣
 * - 最大耐久: 1,048,576
 * - 通常攻撃: 超高火力
 * - 妖術1「飛炎斬」: 射程64m遠距離斬撃、地形破壊なし
 * - 妖術2「九尾乱舞」: 半径16m範囲殲滅、地形破壊なし
 */
public class KitsunebiSwordItem extends SwordItem implements IKitsunebiTool {

    public KitsunebiSwordItem(Properties properties) {
        super(ModToolTiers.KITSUNEBI, properties
                .durability(ModToolTiers.KITSUNEBI.getUses())
                .attributes(SwordItem.createAttributes(ModToolTiers.KITSUNEBI, 9999, -2.4F))
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public int getModeCount() {
        return 3;
    }

    @Override
    public String getModeTranslationKey(int mode) {
        return switch (mode) {
            case 1 -> "kitsunebi.skill.sword.slash";
            case 2 -> "kitsunebi.skill.sword.dance";
            default -> "kitsunebi.skill.sword.normal";
        };
    }

    @Override
    public void executeSkill(ItemStack stack, ServerPlayer player) {
        if (!ToolStateHelper.isActive(stack)) return;
        int mode = ToolStateHelper.getMode(stack);
        ServerLevel level = player.serverLevel();
        net.kafgchan.kitsunebi.config.KitsunebiConfig config = net.kafgchan.kitsunebi.config.KitsunebiConfig.get();

        if (mode == 1) {
            // 飛炎斬: 射程 (config.swordSlashRange) 遠距離斬撃 (重複排除・最大数制限付き)
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            DamageSource source = level.damageSources().playerAttack(player);
            java.util.Set<LivingEntity> hitTargets = new java.util.HashSet<>();
            int range = config.swordSlashRange;
            int maxTargets = config.maxAttackTargets;

            for (double d = 1.0; d <= range && hitTargets.size() < maxTargets; d += 1.0) {
                Vec3 point = eyePos.add(lookVec.scale(d));
                if (config.enableParticles) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 2, 0.1, 0.1, 0.1, 0.02);
                    level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0.01);
                }

                AABB box = new AABB(point.x - 1.2, point.y - 1.2, point.z - 1.2, point.x + 1.2, point.y + 1.2, point.z + 1.2);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive() && !hitTargets.contains(e));
                for (LivingEntity target : targets) {
                    if (hitTargets.add(target)) {
                        target.hurt(source, 5000.0F);
                        if (hitTargets.size() >= maxTargets) break;
                    }
                }
            }
            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);

        } else if (mode == 2) {
            // 九尾乱舞: 半径 (config.swordDanceRadius) 範囲殲滅
            Vec3 pos = player.position();
            double radius = config.swordDanceRadius;
            AABB area = new AABB(pos.x - radius, pos.y - 8.0, pos.z - radius, pos.x + radius, pos.y + 8.0, pos.z + radius);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && (e instanceof Enemy || e.isInvulnerableTo(level.damageSources().generic())));

            DamageSource source = level.damageSources().playerAttack(player);
            int count = 0;
            int maxTargets = config.maxAttackTargets;
            for (LivingEntity target : targets) {
                if (count >= maxTargets) break;
                target.hurt(source, 5000.0F);
                if (config.enableParticles) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.3, 0.5, 0.3, 0.05);
                }
                count++;
            }

            if (config.enableParticles) {
                for (int i = 0; i < 9; i++) {
                    double angle = i * (Math.PI * 2.0 / 9.0);
                    double px = pos.x + Math.cos(angle) * 3.0;
                    double pz = pos.z + Math.sin(angle) * 3.0;
                    level.sendParticles(ParticleTypes.FLAME, px, pos.y + 1.0, pz, 10, 0.2, 0.5, 0.2, 0.08);
                }
            }
            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
    }
}
