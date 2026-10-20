package net.kafgchan.kitsunebi.forge;
import net.kafgchan.kitsunebi.client.*;
import net.kafgchan.kitsunebi.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.*;
public final class ForgeClient {
 public static void register(IEventBus bus){bus.addListener(ForgeClient::setup);bus.addListener(ForgeClient::keys);bus.addListener(ForgeClient::models);}
 private static void setup(FMLClientSetupEvent e){
  e.enqueueWork(()->MenuScreens.register(ModMenus.KITSUNEBI_ALTAR_MENU.get(),KitsunebiAltarScreen::new));
  MinecraftForge.EVENT_BUS.addListener(ForgeClient::tick);
 }
 private static void keys(RegisterKeyMappingsEvent e){e.register(KitsunebiKeyHandler.KEY_SKILL);e.register(KitsunebiKeyHandler.KEY_TOGGLE);e.register(KitsunebiKeyHandler.KEY_MODE_NEXT);e.register(KitsunebiKeyHandler.KEY_MODE_PREVIOUS);e.register(KitsunebiKeyHandler.KEY_FEET_TOGGLE);}
 private static void models(ModelEvent.ModifyBakingResult e){e.getModels().replaceAll((id,model)->id instanceof net.minecraft.client.resources.model.ModelResourceLocation location ? SmoothSlopeModel.replace(location,model) : model);}
 private static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();net.kafgchan.kitsunebi.forge.verification.AltarGuiVerifier.tick(mc);net.kafgchan.kitsunebi.forge.verification.ConnectionClientVerifier.tick(mc);if(!Boolean.getBoolean("kitsunebi.verifyConnection")||net.kafgchan.kitsunebi.forge.verification.ConnectionClientVerifier.completed())net.kafgchan.kitsunebi.forge.verification.CompatClientVerifier.tick(mc);KitsunebiKeyHandler.onClientTick(mc);if(KitsunebiClientHelper.tickClient(mc.player,mc.level)&&mc.player!=null&&mc.level!=null)KitsunebiClientHelper.refreshVisionSections(mc.level,mc.player.blockPosition(),mc.options.renderDistance().get()+1,mc.levelRenderer::setSectionDirty);}
 public static void receiveAirWalk(boolean active){KitsunebiAirWalkClientState.apply(Minecraft.getInstance().player,active);}
}
