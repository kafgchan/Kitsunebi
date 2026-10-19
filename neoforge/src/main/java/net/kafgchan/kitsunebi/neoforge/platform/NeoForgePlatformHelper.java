package net.kafgchan.kitsunebi.neoforge.platform;

import net.kafgchan.kitsunebi.Constants;
import net.kafgchan.kitsunebi.platform.services.IPlatformHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.item.ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Constants.MOD_ID);

    public static void registerBuses(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        return CREATIVE_MODE_TABS.register(name, tab);
    }

    @Override
    public <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<net.minecraft.world.inventory.MenuType<T>> registerMenuType(String name, MenuFactory<T> factory) {
        return MENU_TYPES.register(name, () -> new net.minecraft.world.inventory.MenuType<>(factory::create, net.minecraft.world.flag.FeatureFlagSet.of()));
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> registerArmorMaterial(String name, Supplier<net.minecraft.world.item.ArmorMaterial> materialSupplier) {
        return ARMOR_MATERIALS.register(name, materialSupplier);
    }

    @Override
    public boolean isCurioOrAccessoryEquipped(net.minecraft.world.entity.LivingEntity entity, Item item) {
        if (!net.neoforged.fml.ModList.get().isLoaded("curios")) return false;
        return net.kafgchan.kitsunebi.neoforge.compat.CuriosCompat.equipped(entity,item);
    }

    @Override
    public void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload packet) {
        dev.architectury.networking.NetworkManager.sendToServer(packet);
    }

    @Override
    public void sendToPlayer(net.minecraft.server.level.ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload packet) {
        dev.architectury.networking.NetworkManager.sendToPlayer(player, packet);
    }
}
