package net.kafgchan.kitsunebi.neoforge.verification;
import net.kafgchan.kitsunebi.compat.AltarRecipes;
import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
public final class AltarCompatVerifier {
 public static void verify(net.minecraft.server.level.ServerLevel level){
  int checked=0;
  BlockPos wall=new BlockPos(4920,250,4920);
  level.setBlock(wall,Blocks.STONE.defaultBlockState(),3);
  for(var direction:net.minecraft.core.Direction.values()) {
   BlockPos adjacent=wall.relative(direction);
   level.setBlock(adjacent,net.kafgchan.kitsunebi.registry.ModBlocks.KITSUNEBI_ALTAR.get().defaultBlockState(),3);
   if(!net.minecraft.world.level.block.Block.shouldRenderFace(level.getBlockState(wall),level,wall,direction,adjacent))throw new AssertionError("Altar hides wall face: "+direction);
   level.setBlock(adjacent,Blocks.AIR.defaultBlockState(),3);
  }

  for(var e:AltarRecipes.all()) {
   SimpleContainer input=new SimpleContainer(15);int slot=0;
   for(var alternatives:e.inputs())input.setItem(slot++,alternatives.getFirst().copy());
   String[] path=e.id().getPath().split("/");
   boolean repair=path[2].equals("repair");
   var category=repair?AltarCraftingEngine.Category.EQUIPMENT:AltarCraftingEngine.Category.valueOf(path[2].toUpperCase(java.util.Locale.ROOT));
   int selection=repair?9:Integer.parseInt(path[3]);
   var result=AltarCraftingEngine.calculate(input,category,selection,e.output().getCount());
   if(!result.canCraft()||!ItemStack.isSameItemSameComponents(result.result(),e.output()))throw new AssertionError("Viewer recipe differs from altar: "+e.id());
   checked++;
  }
  var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("5211aa25-7745-4dbd-a2d2-3dfd82298c95"),"CuriosVerifier"));
  var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElseThrow();
  var head=inventory.getStacksHandler("head").orElseThrow();
  ItemStack mask=new ItemStack(ModItems.KITSUNEBI_GOGGLES.get());
  if(!head.getStacks().isItemValid(0,mask))throw new AssertionError("Mask not valid for Curios head");
  head.getStacks().setStackInSlot(0,mask);
  if(!net.kafgchan.kitsunebi.util.KitsunebiVisionHelper.isEquippedGoggles(player))throw new AssertionError("Curios mask vision missing");
  head.getStacks().setStackInSlot(0,ItemStack.EMPTY);
  player.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.KITSUNEBI_TABI.get()));
  BlockPos water=new BlockPos(4888,250,4888);
  for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.getChunk((water.getX()>>4)+x,(water.getZ()>>4)+z);level.setBlock(water,Blocks.WATER.defaultBlockState(),3);level.setBlock(water.above(),Blocks.WATER.defaultBlockState(),3);
  player.setPos(water.getX()+.5,water.getY(),water.getZ()+.5);player.baseTick();
  if(!player.isInWaterOrBubble())throw new AssertionError("Water test did not enter fluid");
  player.setShiftKeyDown(true);net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.onPlayerJumpInput(player);
  net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.tickPlayer(player);
  if(!net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.isActive(player))throw new AssertionError("Water cancelled air walk");
  net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.clearPlayer(player.getUUID());
  System.out.println("PASS: "+checked+" altar viewer recipes, Curios head mask and water air walk.");
 }
}
