package net.kafgchan.kitsunebi.compat;
import java.util.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.*;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.*;
import me.shedaniel.math.*;
import net.minecraft.network.chat.Component;
public class AltarReiPlugin implements REIClientPlugin {
 public static int registeredRecipes;

 public static final CategoryIdentifier<AltarDisplay> TYPE=CategoryIdentifier.of("kitsunebi","altar");
 public void registerCategories(CategoryRegistry r){r.add(new Category());r.addWorkstations(TYPE,EntryStacks.of(AltarRecipes.altar()));}
 public void registerDisplays(DisplayRegistry r){for(var e:AltarRecipes.all())r.add(new AltarDisplay(e));registeredRecipes=AltarRecipes.all().size();}
 public static final class AltarDisplay extends BasicDisplay {
  AltarDisplay(AltarRecipes.Entry e){super(e.inputs().stream().map(EntryIngredients::ofItemStacks).toList(),List.of(EntryIngredients.of(e.output())),Optional.of(e.id()));}
  public CategoryIdentifier<?> getCategoryIdentifier(){return TYPE;}
 }
 private static final class Category implements DisplayCategory<AltarDisplay> {
  public CategoryIdentifier<? extends AltarDisplay> getCategoryIdentifier(){return TYPE;}
  public Component getTitle(){return AltarRecipes.altar().getHoverName();}
  public Renderer getIcon(){return EntryStacks.of(AltarRecipes.altar());}
  public int getDisplayHeight(){return 70;}
  public List<Widget> setupDisplay(AltarDisplay d,Rectangle b){
   List<Widget> w=new ArrayList<>();w.add(Widgets.createRecipeBase(b));
   for(int i=0;i<d.getInputEntries().size();i++)w.add(Widgets.createSlot(new Point(b.x+6+i%3*20,b.y+6+i/3*20)).entries(d.getInputEntries().get(i)).markInput());
   w.add(Widgets.createArrow(new Point(b.x+82,b.y+24)));
   w.add(Widgets.createSlot(new Point(b.x+126,b.y+24)).entries(d.getOutputEntries().getFirst()).markOutput());return w;
  }
 }
}
