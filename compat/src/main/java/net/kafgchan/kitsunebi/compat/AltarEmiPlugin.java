package net.kafgchan.kitsunebi.compat;
import java.util.List;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
@EmiEntrypoint
public final class AltarEmiPlugin implements EmiPlugin {
 public static int registeredRecipes;

 public static final EmiRecipeCategory CATEGORY=new EmiRecipeCategory(AltarRecipes.id("altar"),EmiStack.of(AltarRecipes.altar()));
 public void register(EmiRegistry registry){
  registry.addCategory(CATEGORY);registry.addWorkstation(CATEGORY,EmiStack.of(AltarRecipes.altar()));
  for(var entry:AltarRecipes.all())registry.addRecipe(new Recipe(entry));registeredRecipes=AltarRecipes.all().size();
 }
 private record Recipe(AltarRecipes.Entry entry) implements EmiRecipe {
  public EmiRecipeCategory getCategory(){return CATEGORY;}public ResourceLocation getId(){return entry.id();}
  public List<EmiIngredient> getInputs(){return entry.inputs().stream().map(items->EmiIngredient.of(items.stream().map(s->EmiStack.of(s.copyWithCount(1))).toList(),items.getFirst().getCount())).toList();}
  public List<EmiStack> getOutputs(){return List.of(EmiStack.of(entry.output()));}
  public int getDisplayWidth(){return 150;}public int getDisplayHeight(){return 60;}
  public void addWidgets(WidgetHolder w){var inputs=getInputs();for(int i=0;i<inputs.size();i++)w.addSlot(inputs.get(i),i%3*20,i/3*20);w.addFillingArrow(76,20,1000);w.addSlot(getOutputs().getFirst(),120,20).recipeContext(this);}
 }
}
