package net.kafgchan.kitsunebi.neoforge.verification;
import net.kafgchan.kitsunebi.compat.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
public final class CompatClientVerifier {
 private static int ticks,frames;private static String viewer;
 public static void tick(Minecraft mc){
  if(!Boolean.getBoolean("kitsunebi.verifyCompat")||mc.level==null||mc.player==null)return;
  if(++ticks<100)return;
  try {
   if(viewer==null){
    int registered;
    if(ModList.get().isLoaded("jei")){viewer="jei";registered=Jei.open();}
    else if(ModList.get().isLoaded("emi")){viewer="emi";registered=Emi.open();}
    else {viewer="rei";registered=Rei.open();}
    if(registered!=52)throw new AssertionError(viewer+" registered "+registered+" recipes");
   }
   if(++frames==60){
    if(mc.screen==null)throw new AssertionError("Viewer screen did not open");
    net.minecraft.client.Screenshot.grab(mc.gameDirectory,"altar-"+viewer+".png",mc.getMainRenderTarget(),message->{});
    java.nio.file.Files.writeString(java.nio.file.Path.of("compat-"+viewer+".txt"),"PASS: 52 altar recipes registered and recipe GUI rendered: "+mc.screen.getClass().getName());
    mc.stop();
   }
  }catch(Throwable error){try{java.nio.file.Files.writeString(java.nio.file.Path.of("compat-failure.txt"),error.toString());}catch(Exception ignored){}throw new RuntimeException(error);}
 }
 private static final class Jei {static int open(){AltarJeiPlugin.runtime.getRecipesGui().showTypes(java.util.List.of(AltarJeiPlugin.TYPE));return (int)AltarJeiPlugin.runtime.getRecipeManager().createRecipeLookup(AltarJeiPlugin.TYPE).get().count();}}
 private static final class Emi {static int open(){dev.emi.emi.api.EmiApi.displayRecipeCategory(AltarEmiPlugin.CATEGORY);return dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes(AltarEmiPlugin.CATEGORY).size();}}
 private static final class Rei {static int open(){me.shedaniel.rei.api.client.view.ViewSearchBuilder.builder().addCategories(java.util.List.of(AltarReiPlugin.TYPE)).open();return me.shedaniel.rei.api.client.registry.display.DisplayRegistry.getInstance().get(AltarReiPlugin.TYPE).size();}}
}
