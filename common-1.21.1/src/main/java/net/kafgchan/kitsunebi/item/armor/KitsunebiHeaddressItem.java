package net.kafgchan.kitsunebi.item.armor;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/**
 * 狐火の頭飾り
 * - 狐火・幻影ブロック可視化・回収支援
 * - 常時暗視
 * - 有害デバフ完全無効化（有益効果は維持、速度上昇は付与しない）
 */
public class KitsunebiHeaddressItem extends KitsunebiArmorItem {

    public KitsunebiHeaddressItem(Properties properties) {
        this(properties, FoxArmorStyle.SHUKO);
    }

    public KitsunebiHeaddressItem(Properties properties, FoxArmorStyle style) {
        super(ModArmorMaterials.KITSUNEBI, ArmorItem.Type.HELMET, properties, style);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.HEAD) == stack) {
                // 1. 常時暗視（視覚妨害のない300tick持続、アンビエント・パーティクル非表示）
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));

                // 2. 有害デバフの完全無効化（有害効果をリストアップして安全に除去）
                var harmfulEffects = new ArrayList<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>>();
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                        harmfulEffects.add(effect.getEffect());
                    }
                }
                for (var effectHolder : harmfulEffects) {
                    player.removeEffect(effectHolder);
                }
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}
