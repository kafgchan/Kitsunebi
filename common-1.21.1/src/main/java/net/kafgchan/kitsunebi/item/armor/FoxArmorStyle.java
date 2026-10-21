package net.kafgchan.kitsunebi.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Cosmetic families share the same abilities, durability and tail growth. */
public enum FoxArmorStyle {
    SHUKO("shuko"), BYAKKO("byakko"), KOKKO("kokko"), SHINKO("shinko");

    public final String id;
    FoxArmorStyle(String id) { this.id = id; }

    public static FoxArmorStyle of(ItemStack stack) {
        return stack.getItem() instanceof KitsunebiArmorItem armor ? armor.style() : SHUKO;
    }

    /** One tail pass even with mixed colors; leggings choose the tail color first. */
    public static EquipmentSlot tailOwner(LivingEntity wearer) {
        for (var slot : new EquipmentSlot[]{EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.FEET})
            if (wearer.getItemBySlot(slot).getItem() instanceof KitsunebiArmorItem) return slot;
        return null;
    }
}
