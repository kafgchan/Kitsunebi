package net.kafgchan.kitsunebi.compat;
import java.util.*;
import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.kafgchan.kitsunebi.registry.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
/** Viewer data is derived from the same engine as the server; never registers workbench recipes. */
public final class AltarRecipes {
 public record Entry(ResourceLocation id,List<List<ItemStack>> inputs,ItemStack output) {}
 public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("kitsunebi",path);}
 public static ItemStack altar(){return new ItemStack(ModItems.KITSUNEBI_ALTAR.get());}
 public static List<Entry> all(){
  List<Entry> entries=new ArrayList<>();
  for(var category:AltarCraftingEngine.Category.values()) {
   int count=switch(category){case KITSUNEBI->7;case ARCHITECTURAL->1;case PHANTOM->17;case EQUIPMENT->9;};
   for(int i=0;i<count;i++) {
    int q=category==AltarCraftingEngine.Category.PHANTOM?switch(i){case 1,2->6;case 3,4->18;case 5,6,7,8,9,10->4;case 13->3;default->1;}:1;
    var result=AltarCraftingEngine.calculate(new SimpleContainer(15),category,i,q);
    List<List<ItemStack>> inputs=new ArrayList<>();
    for(var required:result.ingredients())inputs.add(choices(required.label(),required.requiredCount(),i));
    entries.add(new Entry(id("/altar/"+category.name().toLowerCase(Locale.ROOT)+"/"+i),inputs,result.result()));
   }
  }
  // Representative damaged inputs preserve enchantments in the actual altar repair engine.
  for(Item item:List.of(ModItems.KITSUNEBI_HEADDRESS.get(),ModItems.KITSUNEBI_HAORI.get(),ModItems.KITSUNEBI_HAKAMA.get(),ModItems.KITSUNEBI_TABI.get(),ModItems.KITSUNEBI_SWORD.get(),ModItems.KITSUNEBI_PICKAXE.get(),ModItems.KITSUNEBI_AXE.get(),ModItems.KITSUNEBI_SHOVEL.get(),ModItems.KITSUNEBI_HOE.get())) {
   for(int repair:new int[]{4096,32768}) {
    ItemStack damaged=new ItemStack(item);damaged.setDamageValue(repair);
    var material=repair==4096?fires(1):List.of(new ItemStack(ModBlocks.ARCHITECTURAL_KITSUNEBI.get()));
    entries.add(new Entry(id("/altar/repair/"+net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath()+"/"+repair),List.of(List.of(damaged),material),new ItemStack(item)));
   }
  }
  return List.copyOf(entries);
 }
 private static List<ItemStack> fires(int count){return Arrays.stream(ModBlocks.COLORS).map(c->new ItemStack(ModItems.getItem(c).get(),count)).toList();}
 private static List<ItemStack> choices(String label,int count,int color){
  if(label.equals("Kitsunebi Fire"))return fires(count);
  Item[] items=switch(label){
   case "Glow Dust / Glow Ink"->new Item[]{Items.GLOWSTONE_DUST,Items.GLOW_INK_SAC};
   case "Amethyst Shard"->new Item[]{Items.AMETHYST_SHARD};
   case "Bamboo"->new Item[]{Items.BAMBOO};case "Lapis Lazuli"->new Item[]{Items.LAPIS_LAZULI};
   case "Dye"->new Item[]{AltarCraftingEngine.getDyeForColor(ModBlocks.COLORS[color])};
   case "Amethyst Block"->new Item[]{Items.AMETHYST_BLOCK};
   case "Kitsunebi Block"->new Item[]{ModBlocks.ARCHITECTURAL_KITSUNEBI.get().asItem()};
   case "Slime / Honey Block"->new Item[]{Items.SLIME_BLOCK,Items.HONEY_BLOCK};
   case "Soul Sand / Soul Soil"->new Item[]{Items.SOUL_SAND,Items.SOUL_SOIL};
   case "Phantom Block"->new Item[]{ModBlocks.PHANTOM_BLOCK.get().asItem()};
   case "Phantom Slab (H/V)"->new Item[]{ModBlocks.PHANTOM_SLAB.get().asItem(),ModBlocks.PHANTOM_VERTICAL_SLAB.get().asItem()};
   case "Phantom Quarter (H/V)"->new Item[]{ModBlocks.PHANTOM_QUARTER_SLAB.get().asItem(),ModBlocks.PHANTOM_VERTICAL_QUARTER.get().asItem()};
   case "White Kitsunebi"->new Item[]{ModItems.getItem("white").get()};
   case "Red Kitsunebi"->new Item[]{ModItems.getItem("red").get()};case "Stick"->new Item[]{Items.STICK};
   default->throw new IllegalArgumentException("Unknown altar ingredient "+label);
  };
  return Arrays.stream(items).map(item->new ItemStack(item,count)).toList();
 }
}
