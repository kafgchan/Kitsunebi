package net.kafgchan.kitsunebi.forge.platform;
import net.kafgchan.kitsunebi.platform.services.IPlatformHelper;
import net.kafgchan.kitsunebi.network.KitsunebiPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import java.util.function.Supplier;
public final class ForgePlatformHelper implements IPlatformHelper {
 public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,"kitsunebi");
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(Registries.ITEM,"kitsunebi");
 public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"kitsunebi");
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"kitsunebi");
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);TABS.register(bus);MENUS.register(bus);}
 public String getPlatformName(){return "Forge";}
 public boolean isDevelopmentEnvironment(){return !FMLLoader.isProduction();}
 public <T extends Item> Supplier<T> registerItem(String name,Supplier<T> value){return ITEMS.register(name,value);}
 public <T extends Block> Supplier<T> registerBlock(String name,Supplier<T> value){return BLOCKS.register(name,value);}
 public Supplier<CreativeModeTab> registerCreativeTab(String name,Supplier<CreativeModeTab> value){return TABS.register(name,value);}
 public CreativeModeTab.Builder creativeTabBuilder(){return CreativeModeTab.builder();}
 public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(String name,MenuFactory<T> factory){return MENUS.register(name,()->new MenuType<>(factory::create,net.minecraft.world.flag.FeatureFlags.VANILLA_SET));}
 public boolean isCurioOrAccessoryEquipped(net.minecraft.world.entity.LivingEntity entity,Item item){return net.minecraftforge.fml.ModList.get().isLoaded("curios")&&net.kafgchan.kitsunebi.forge.compat.CuriosCompat.equipped(entity,item);}
 public ArmorMaterial registerArmorMaterial(String name,Supplier<ArmorMaterial> material){return material.get();}
 public void sendToServer(KitsunebiPacket packet){net.kafgchan.kitsunebi.forge.ForgeNetwork.CHANNEL.sendToServer(packet);}
 public void sendToPlayer(net.minecraft.server.level.ServerPlayer player,KitsunebiPacket packet){net.kafgchan.kitsunebi.forge.ForgeNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->player),packet);}
}
