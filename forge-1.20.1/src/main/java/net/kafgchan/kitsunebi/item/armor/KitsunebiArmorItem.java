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

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisualModel<net.minecraft.world.entity.LivingEntity> model;
            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(net.minecraft.world.entity.LivingEntity entity, ItemStack stack, net.minecraft.world.entity.EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<?> original) {
                if (model == null) model = net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals.create();
                net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals.prepare(model, original, entity, slot);
                return model;
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, String type) {
        return "kitsunebi:textures/entity/kitsunebi_shrine_palette.png";
    }

    public KitsunebiArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties
                .durability(MAX_DURABILITY)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // 金床修理不可、祭壇修理のみ
    }
}
