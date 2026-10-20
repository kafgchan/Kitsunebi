package net.kafgchan.kitsunebi.forge;
import net.kafgchan.kitsunebi.item.armor.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
@Mod.EventBusSubscriber(modid="kitsunebi")
public final class ForgeEvents {
 private static final java.util.Map<java.util.UUID,Integer> AIR_WALK_RELEASE = new java.util.HashMap<>();
 private static final java.lang.reflect.Field FLOATING_TICKS=net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(net.minecraft.server.network.ServerGamePacketListenerImpl.class,"f_9737_");
 private static final java.lang.reflect.Field FLOATING=net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(net.minecraft.server.network.ServerGamePacketListenerImpl.class,"f_9736_");
 @SubscribeEvent public static void damage(LivingHurtEvent event){
  float amount=KitsunebiArmorHandler.processDamage(event.getEntity(),event.getSource(),event.getAmount());
  event.setAmount(amount);if(amount==0)event.setCanceled(true);
 }
 @SubscribeEvent public static void knockback(LivingKnockBackEvent e){if(KitsunebiArmorHandler.shouldCancelKnockback(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof net.minecraft.server.level.ServerPlayer p))return;
  KitsunebiAirWalkManager.tickPlayer(p);
  // Only server-approved air walking bypasses vanilla's 80-tick floating kick.
  // SRG field names are resolved by Forge in both development and production.
  boolean active=KitsunebiAirWalkManager.isActive(p);
  if(active)AIR_WALK_RELEASE.put(p.getUUID(),120);
  int remaining=AIR_WALK_RELEASE.getOrDefault(p.getUUID(),0);
  if(!active&&remaining>0){if(p.onGround()||remaining==1)AIR_WALK_RELEASE.remove(p.getUUID());else AIR_WALK_RELEASE.put(p.getUUID(),remaining-1);}
  // After release, server-simulated falling can leave vanilla floating detection stale.
  // Grant only a bounded falling grace period after an approved session; hovering is excluded.
  if(active||(remaining>0&&p.getDeltaMovement().y<-.03125))try{FLOATING_TICKS.setInt(p.connection,0);FLOATING.setBoolean(p.connection,false);}catch(IllegalAccessException error){throw new IllegalStateException("Cannot synchronize air-walk floating state",error);}
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){KitsunebiAirWalkManager.clearPlayer(e.getEntity().getUUID());AIR_WALK_RELEASE.remove(e.getEntity().getUUID());}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){KitsunebiAirWalkManager.clearPlayer(e.getEntity().getUUID());AIR_WALK_RELEASE.remove(e.getEntity().getUUID());}
}

