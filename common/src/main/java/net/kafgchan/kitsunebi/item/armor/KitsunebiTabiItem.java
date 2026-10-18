package net.kafgchan.kitsunebi.item.armor;

import net.kafgchan.kitsunebi.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * 狐火の足袋
 * - 移動速度 +75%（袴と合計 +150% = 基本速度の2.5倍）
 * - 落下ダメージ完全無効
 * - 空中走行（仮想狐火足場）
 */
public class KitsunebiTabiItem extends KitsunebiArmorItem {
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "armor.tabi.speed");

    public KitsunebiTabiItem(Properties properties) {
        super(ModArmorMaterials.KITSUNEBI, ArmorItem.Type.BOOTS, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers();
        return base.withModifierAdded(
                Attributes.MOVEMENT_SPEED,
                new AttributeModifier(SPEED_MODIFIER_ID, 0.75D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.FEET
        );
    }
}
