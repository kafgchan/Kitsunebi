package net.kafgchan.kitsunebi.forge;
import net.kafgchan.kitsunebi.network.*;
import net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;
public final class ForgeNetwork {
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath("kitsunebi","main"),()->"1","1"::equals,"1"::equals);
 public static void init(){
  CHANNEL.registerMessage(0,KitsunebiSkillPacket.class,(p,b)->b.writeVarInt(p.action()),b->new KitsunebiSkillPacket(b.readVarInt()),(p,c)->{var ctx=c.get();ctx.enqueueWork(()->KitsunebiNetworkHandler.handleSkillAction(ctx.getSender(),p.action()));ctx.setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));
  CHANNEL.registerMessage(1,KitsunebiAirWalkPacket.class,(p,b)->b.writeBoolean(p.jump()),b->new KitsunebiAirWalkPacket(b.readBoolean()),(p,c)->{var ctx=c.get();ctx.enqueueWork(()->KitsunebiNetworkHandler.handleAirWalk(ctx.getSender(),p.jump()));ctx.setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));
  CHANNEL.registerMessage(2,KitsunebiAirWalkMovePacket.class,(p,b)->b.writeByte(p.vertical()),b->new KitsunebiAirWalkMovePacket(b.readByte()),(p,c)->{var ctx=c.get();ctx.enqueueWork(()->{if(ctx.getSender()!=null)KitsunebiAirWalkManager.setVerticalInput(ctx.getSender(),p.vertical());});ctx.setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));
  CHANNEL.registerMessage(3,KitsunebiAirWalkStatePacket.class,(p,b)->b.writeBoolean(p.active()),b->new KitsunebiAirWalkStatePacket(b.readBoolean()),(p,c)->{var ctx=c.get();ctx.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->ForgeClient.receiveAirWalk(p.active())));ctx.setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_CLIENT));
 }
}
