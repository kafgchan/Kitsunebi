package net.kafgchan.kitsunebi.item.armor;

import net.kafgchan.kitsunebi.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * 狐火の袴
 * - 移動速度 +75%
 * - 3D狐尾表示（防具装着数1/2/3/4で1/3/6/9本、Codexモデル）
 */
public class KitsunebiHakamaItem extends KitsunebiArmorItem {
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "armor.hakama.speed");

    public KitsunebiHakamaItem(Properties properties) {
        this(properties, FoxArmorStyle.SHUKO);
    }

    public KitsunebiHakamaItem(Properties properties, FoxArmorStyle style) {
        super(ModArmorMaterials.KITSUNEBI, ArmorItem.Type.LEGGINGS, properties, style);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers();
        return base.withModifierAdded(
                Attributes.MOVEMENT_SPEED,
                new AttributeModifier(SPEED_MODIFIER_ID, 0.75D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.LEGS
        );
    }
}
