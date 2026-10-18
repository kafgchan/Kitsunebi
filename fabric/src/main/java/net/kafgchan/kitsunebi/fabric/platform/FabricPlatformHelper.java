package net.kafgchan.kitsunebi.fabric.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.kafgchan.kitsunebi.Constants;
import net.kafgchan.kitsunebi.platform.services.IPlatformHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder();
    }
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        T registered = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), item.get());
        return () -> registered;
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        T registered = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), block.get());
        return () -> registered;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        CreativeModeTab registered = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), tab.get());
        return () -> registered;
    }

    @Override
    public <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<net.minecraft.world.inventory.MenuType<T>> registerMenuType(String name, MenuFactory<T> factory) {
        net.minecraft.world.inventory.MenuType<T> registered = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                new net.minecraft.world.inventory.MenuType<>(factory::create, net.minecraft.world.flag.FeatureFlagSet.of())
        );
        return () -> registered;
    }

    @Override
    public net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> registerArmorMaterial(String name, Supplier<net.minecraft.world.item.ArmorMaterial> materialSupplier) {
        return Registry.registerForHolder(
                BuiltInRegistries.ARMOR_MATERIAL,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                materialSupplier.get()
        );
    }

    @Override
    public boolean isCurioOrAccessoryEquipped(net.minecraft.world.entity.LivingEntity entity, Item item) {
        // 1. Accessories API (io.wispforest.accessories)
        try {
            Class<?> accessoriesCapabilityClass = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
            java.lang.reflect.Method getMethod = accessoriesCapabilityClass.getMethod("get", net.minecraft.world.entity.LivingEntity.class);
            Object capability = getMethod.invoke(null, entity);
            if (capability != null) {
                java.lang.reflect.Method isEquipped = capability.getClass().getMethod("isEquipped", Item.class);
                Object res = isEquipped.invoke(capability, item);
                if (Boolean.TRUE.equals(res)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Trinkets API (dev.emi.trinkets)
        try {
            Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            java.lang.reflect.Method getTrinketComp = trinketsApiClass.getMethod("getTrinketComponent", net.minecraft.world.entity.LivingEntity.class);
            Object compOpt = getTrinketComp.invoke(null, entity);
            if (compOpt instanceof java.util.Optional<?> opt && opt.isPresent()) {
                Object comp = opt.get();
                java.lang.reflect.Method isEquipped = comp.getClass().getMethod("isEquipped", Item.class);
                Object res = isEquipped.invoke(comp, item);
                if (Boolean.TRUE.equals(res)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }

        return false;
    }
}
