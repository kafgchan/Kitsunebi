package net.kafgchan.kitsunebi.forge;
import net.kafgchan.kitsunebi.KitsunebiCommon;
import net.kafgchan.kitsunebi.forge.platform.ForgePlatformHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
@Mod("kitsunebi")
public final class KitsunebiForge {
 public KitsunebiForge(FMLJavaModLoadingContext context){
  var bus=context.getModEventBus();
  KitsunebiCommon.init();ForgePlatformHelper.register(bus);ForgeNetwork.init();
  if(Boolean.getBoolean("kitsunebi.verifyPort"))net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.kafgchan.kitsunebi.forge.verification.SlopeMovementVerifier::verify);
  if(net.minecraftforge.fml.ModList.get().isLoaded("curios")){net.kafgchan.kitsunebi.forge.compat.CuriosCompat.register(bus);if(FMLEnvironment.dist.isClient())net.kafgchan.kitsunebi.forge.compat.CuriosCompat.registerClient(bus);}
  if(FMLEnvironment.dist.isClient())ForgeClient.register(bus);
 }
}
