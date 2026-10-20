package net.kafgchan.kitsunebi.item.armor;
import net.minecraft.world.item.*;
import net.minecraft.sounds.*;
import net.minecraft.world.item.crafting.Ingredient;
public final class ModArmorMaterials {
 public static final ArmorMaterial KITSUNEBI = new ArmorMaterial() {
  public int getDurabilityForType(ArmorItem.Type type){return 1_048_576;}
  public int getDefenseForType(ArmorItem.Type type){return switch(type){case HELMET,BOOTS->3;case LEGGINGS->6;case CHESTPLATE->8;};}
  public int getEnchantmentValue(){return 25;}
  public SoundEvent getEquipSound(){return SoundEvents.ARMOR_EQUIP_NETHERITE;}
  public Ingredient getRepairIngredient(){return Ingredient.EMPTY;}
  public String getName(){return "kitsunebi:kitsunebi";}
  public float getToughness(){return 3;}
  public float getKnockbackResistance(){return .1f;}
 };
 public static void init(){}
}
