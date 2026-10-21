package net.kafgchan.kitsunebi.neoforge.client.gecko;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.function.Predicate;

/** Family-independent selection policy. No abilities, attributes or packets are changed. */
public final class ArmorTailGrowth {
    public static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final EquipmentSlot[] OWNER_ORDER={EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD,EquipmentSlot.FEET};
    public static final Predicate<ItemStack> FOX=stack->stack.getItem() instanceof net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem;
    public static int count(LivingEntity wearer,Predicate<ItemStack> family){
        int count=0;for(var slot:SLOTS)if(family.test(wearer.getItemBySlot(slot)))count++;return count;
    }
    public static int tails(int pieces){return switch(pieces){case 1->1;case 2->3;case 3->6;case 4->9;default->0;};}
    public static EquipmentSlot owner(LivingEntity wearer,Predicate<ItemStack> family){
        for(var slot:OWNER_ORDER)if(family.test(wearer.getItemBySlot(slot)))return slot;return null;
    }
    private ArmorTailGrowth(){}
}

