package net.kafgchan.kitsunebi.forge.verification;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState;
import net.kafgchan.kitsunebi.network.*;
import net.kafgchan.kitsunebi.registry.ModItems;
import java.nio.file.*;
/** Opt-in real client/server protocol regression; never runs in normal play. */
public final class ConnectionClientVerifier {
 private static int ticks,phase,deadline,activatedAt;
 public static boolean completed(){return phase==3;}
 public static void tick(Minecraft mc){
  if(!Boolean.getBoolean("kitsunebi.verifyConnection")||phase==3)return;
  try {
   if(++ticks%100==0)System.out.println("Connection test phase="+phase+" screen="+(mc.screen==null?"none":mc.screen.getClass().getSimpleName()));
   if(ticks>2400)throw new AssertionError("Client/server network verification timed out at phase "+phase);
   if(mc.player==null||mc.level==null||mc.getConnection()==null)return;
   if(!mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).is(ModItems.KITSUNEBI_TABI.get()))return;
   var field=KitsunebiAirWalkClientState.class.getDeclaredField("owner");field.setAccessible(true);
   if(phase==0){
    mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
    KitsunebiNetwork.sendToServer(new KitsunebiAirWalkPacket(true));phase=1;deadline=ticks+200;
   }else if(phase==1&&field.get(null)==mc.player){
    if(activatedAt==0)activatedAt=ticks;
    if(ticks-activatedAt<100)return; if(mc.player.getY()<12)throw new AssertionError("Air-walk test must hover above the ground");
    KitsunebiNetwork.sendToServer(new KitsunebiAirWalkMovePacket((byte)0));
    KitsunebiNetwork.sendToServer(new KitsunebiAirWalkPacket(true));phase=2;deadline=ticks+200;
   }else if(phase==2&&field.get(null)==null){
    phase=3;Files.writeString(Path.of("connection-verification.txt"),"PASS: real Forge client joined dedicated server; C2S activation/movement/cancellation and S2C acknowledgements; hover longer than vanilla floating-kick threshold with allow-flight=false.");
    if(!Boolean.getBoolean("kitsunebi.verifyCompat"))mc.stop();
   }
   if(phase>0&&phase<3&&ticks>deadline)throw new AssertionError("Missing server acknowledgement at phase "+phase);
  }catch(Throwable error){try{Files.writeString(Path.of("connection-verification.txt"),"FAIL: "+error);}catch(Exception ignored){}throw new RuntimeException(error);}
 }
}
