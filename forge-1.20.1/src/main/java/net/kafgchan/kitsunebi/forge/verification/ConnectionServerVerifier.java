package net.kafgchan.kitsunebi.forge.verification;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
@Mod.EventBusSubscriber(modid="kitsunebi")
public final class ConnectionServerVerifier {
 private static int ticks;
 @SubscribeEvent public static void joined(PlayerEvent.PlayerLoggedInEvent event){
  if(!Boolean.getBoolean("kitsunebi.verifyConnectionServer"))return;
  if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player){
   player.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.KITSUNEBI_TABI.get()));
   player.teleportTo(.5,250,.5);
  }
 }
 @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent event){
  if(Boolean.getBoolean("kitsunebi.verifyConnectionServer")&&event.getEntity().getServer()!=null)event.getEntity().getServer().halt(false);
 }
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
  if(!Boolean.getBoolean("kitsunebi.verifyConnectionServer")||event.phase!=TickEvent.Phase.END)return; if(++ticks>3600)throw new IllegalStateException("Connection test timed out");
 }
}


