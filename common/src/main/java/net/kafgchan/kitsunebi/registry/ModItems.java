package net.kafgchan.kitsunebi.registry;

import net.kafgchan.kitsunebi.item.KitsunebiItem;
import net.kafgchan.kitsunebi.item.KitsunebiToolItem;
import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModItems {
    private static final Map<String, Supplier<Item>> KITSUNEBI_ITEM_MAP = new LinkedHashMap<>();

    public static final Supplier<Item> KITSUNEBI_GOHEI = Services.PLATFORM.registerItem(
            "kitsunebi_gohei",
            () -> new KitsunebiToolItem(new Item.Properties())
    );

    static {
        for (String color : ModBlocks.COLORS) {
            String name = "kitsunebi_" + color;
            Supplier<Item> item = Services.PLATFORM.registerItem(
                    name,
                    () -> new KitsunebiItem(ModBlocks.getBlock(color).get(), new Item.Properties())
            );
            KITSUNEBI_ITEM_MAP.put(color, item);
        }
    }

    public static final Map<String, Supplier<Item>> KITSUNEBI_ITEMS = Collections.unmodifiableMap(KITSUNEBI_ITEM_MAP);

    public static Supplier<Item> getItem(String color) {
        return KITSUNEBI_ITEMS.get(color);
    }

    public static void init() {
        // クラスロード用
    }
}
