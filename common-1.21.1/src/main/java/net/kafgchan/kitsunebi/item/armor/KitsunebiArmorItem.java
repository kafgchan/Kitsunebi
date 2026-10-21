package net.kafgchan.kitsunebi.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * 狐火防具の共通基底クラス
 * 最大耐久値: 1,048,576
 */
public class KitsunebiArmorItem extends ArmorItem {
    public static final int MAX_DURABILITY = 1_048_576;
    private final FoxArmorStyle style;

    public KitsunebiArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        this(material, type, properties, FoxArmorStyle.SHUKO);
    }

    public KitsunebiArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, FoxArmorStyle style) {
        super(material, type, properties
                .durability(MAX_DURABILITY)
                .fireResistant()
                .rarity(Rarity.EPIC));
        this.style = style;
    }

    public FoxArmorStyle style() { return style; }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // 金床修理不可、祭壇修理のみ
    }
}
