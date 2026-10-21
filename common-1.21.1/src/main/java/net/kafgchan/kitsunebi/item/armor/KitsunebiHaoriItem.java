package net.kafgchan.kitsunebi.item.armor;

import net.minecraft.world.item.ArmorItem;

/**
 * 狐火の羽織
 * - 炎・溶岩ダメージ完全無効
 * - 狐火反撃: 攻撃元に対して狐火防具装着数（1/2/3/4）に応じて 64/128/256/512 ダメージ
 */
public class KitsunebiHaoriItem extends KitsunebiArmorItem {

    public KitsunebiHaoriItem(Properties properties) {
        this(properties, FoxArmorStyle.SHUKO);
    }

    public KitsunebiHaoriItem(Properties properties, FoxArmorStyle style) {
        super(ModArmorMaterials.KITSUNEBI, ArmorItem.Type.CHESTPLATE, properties, style);
    }
}
