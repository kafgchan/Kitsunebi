package net.kafgchan.kitsunebi.neoforge.compat;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
public final class CuriosCompat {
 public static boolean equipped(LivingEntity entity,Item item){return CuriosApi.getCuriosInventory(entity).map(inventory->inventory.findFirstCurio(item).isPresent()).orElse(false);}
 public static void register(IEventBus bus){
  bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event)->event.enqueueWork(()->CuriosApi.registerCurio(ModItems.KITSUNEBI_GOGGLES.get(),new top.theillusivec4.curios.api.type.capability.ICurioItem(){})));
 }
 public static void registerClient(IEventBus bus){
  bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event)->event.enqueueWork(()->top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.KITSUNEBI_GOGGLES.get(),net.kafgchan.kitsunebi.neoforge.client.CuriosMaskRenderer::new)));
 }
}
