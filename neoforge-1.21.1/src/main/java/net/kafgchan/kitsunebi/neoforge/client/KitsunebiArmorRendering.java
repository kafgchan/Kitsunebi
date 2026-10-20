package net.kafgchan.kitsunebi.neoforge.client;

import net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisualModel;
import net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class KitsunebiArmorRendering {
    public static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private KitsunebiArmorVisualModel<LivingEntity> model;
            @Override public Model getGenericArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original) {
                if(model==null)model=KitsunebiArmorVisuals.create();
                KitsunebiArmorVisuals.prepare(model,original,entity,slot);
                return model;
            }
        },ModItems.KITSUNEBI_HEADDRESS.get(),ModItems.KITSUNEBI_HAORI.get(),ModItems.KITSUNEBI_HAKAMA.get(),ModItems.KITSUNEBI_TABI.get());
    }
    private KitsunebiArmorRendering(){}
}
