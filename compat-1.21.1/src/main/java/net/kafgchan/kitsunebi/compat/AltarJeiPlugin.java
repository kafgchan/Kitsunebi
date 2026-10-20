package net.kafgchan.kitsunebi.compat;
import mezz.jei.api.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
@JeiPlugin
public final class AltarJeiPlugin implements IModPlugin {
 public static int registeredRecipes;

 public static final RecipeType<AltarRecipes.Entry> TYPE=new RecipeType<>(AltarRecipes.id("altar"),AltarRecipes.Entry.class);
 public static mezz.jei.api.runtime.IJeiRuntime runtime;
 public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime value){runtime=value;}
 public ResourceLocation getPluginUid(){return AltarRecipes.id("jei");}
 public void registerCategories(IRecipeCategoryRegistration r){r.addRecipeCategories(new Category());}
 public void registerRecipes(IRecipeRegistration r){r.addRecipes(TYPE,AltarRecipes.all());registeredRecipes=AltarRecipes.all().size();}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(AltarRecipes.altar(),TYPE);}
 private static final class Category implements IRecipeCategory<AltarRecipes.Entry> {
  public RecipeType<AltarRecipes.Entry> getRecipeType(){return TYPE;}
  public Component getTitle(){return AltarRecipes.altar().getHoverName();}
  public IDrawable getIcon(){return null;}
  public int getWidth(){return 150;}public int getHeight(){return 60;}
  public void createRecipeExtras(mezz.jei.api.gui.widgets.IRecipeExtrasBuilder b,AltarRecipes.Entry r,IFocusGroup f){b.addRecipeArrow().setPosition(78,22);}
  public void setRecipe(IRecipeLayoutBuilder b,AltarRecipes.Entry r,IFocusGroup f){
   for(int i=0;i<r.inputs().size();i++)b.addSlot(RecipeIngredientRole.INPUT,2+i%3*20,2+i/3*20).addItemStacks(r.inputs().get(i)).setStandardSlotBackground();
   b.addSlot(RecipeIngredientRole.OUTPUT,122,22).addItemStack(r.output()).setOutputSlotBackground();
  }
 }
}
