package net.kafgchan.kitsunebi.item.armor;
import com.google.common.collect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ArmorItem;
public class KitsunebiTabiItem extends KitsunebiArmorItem {
 private static final java.util.UUID SPEED_ID=java.util.UUID.nameUUIDFromBytes("kitsunebi:Tabi.speed".getBytes(java.nio.charset.StandardCharsets.UTF_8));
 public KitsunebiTabiItem(Properties properties){super(ModArmorMaterials.KITSUNEBI,ArmorItem.Type.BOOTS,properties);}
 @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
  var builder=ImmutableMultimap.<Attribute,AttributeModifier>builder().putAll(super.getDefaultAttributeModifiers(slot));
  if(slot==EquipmentSlot.FEET)builder.put(Attributes.MOVEMENT_SPEED,new AttributeModifier(SPEED_ID,"kitsunebi speed",.75,AttributeModifier.Operation.MULTIPLY_BASE));
  return builder.build();
 }
}
