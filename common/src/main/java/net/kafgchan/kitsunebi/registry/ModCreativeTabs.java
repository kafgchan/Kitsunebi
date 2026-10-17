package net.kafgchan.kitsunebi.registry;

import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final Supplier<CreativeModeTab> KITSUNEBI_TAB = Services.PLATFORM.registerCreativeTab(
            "kitsunebi_tab",
            () -> Services.PLATFORM.creativeTabBuilder()
                    .title(Component.translatable("itemGroup.kitsunebi.kitsunebi_tab"))
                    .icon(() -> new ItemStack(ModItems.getItem("white").get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.KITSUNEBI_GOHEI.get());
                        output.accept(ModItems.KITSUNEBI_GOGGLES.get());
                        for (String color : ModBlocks.COLORS) {
                            output.accept(ModItems.getItem(color).get());
                        }
                        for (var itemSupplier : ModItems.PHANTOM_ITEMS.values()) {
                            output.accept(itemSupplier.get());
                        }
                    })
                    .build()
    );

    public static void init() {
        // クラスロード用
    }
}
