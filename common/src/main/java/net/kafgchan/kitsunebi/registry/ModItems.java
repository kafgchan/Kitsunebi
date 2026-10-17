package net.kafgchan.kitsunebi.registry;

import net.kafgchan.kitsunebi.item.KitsunebiItem;
import net.kafgchan.kitsunebi.item.KitsunebiToolItem;
import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModItems {
    private static final Map<String, Supplier<Item>> KITSUNEBI_ITEM_MAP = new LinkedHashMap<>();
    private static final Map<String, Supplier<Item>> PHANTOM_ITEM_MAP = new LinkedHashMap<>();

    public static final Supplier<Item> KITSUNEBI_GOHEI = Services.PLATFORM.registerItem(
            "kitsunebi_gohei",
            () -> new KitsunebiToolItem(new Item.Properties())
    );

    public static final Supplier<Item> KITSUNEBI_GOGGLES = Services.PLATFORM.registerItem(
            "kitsunebi_goggles",
            () -> new net.kafgchan.kitsunebi.item.KitsunebiGogglesItem(new Item.Properties())
    );

    static {
        // 光源狐火アイテム
        for (String color : ModBlocks.COLORS) {
            String name = "kitsunebi_" + color;
            Supplier<Item> item = Services.PLATFORM.registerItem(
                    name,
                    () -> new KitsunebiItem(ModBlocks.getBlock(color).get(), new Item.Properties())
            );
            KITSUNEBI_ITEM_MAP.put(color, item);
        }

        // 幻影建築ブロックのアイテム登録
        for (Map.Entry<String, Supplier<Block>> entry : ModBlocks.PHANTOM_BLOCKS.entrySet()) {
            String name = entry.getKey();
            Supplier<Block> blockSupplier = entry.getValue();
            Supplier<Item> item = Services.PLATFORM.registerItem(
                    name,
                    () -> new BlockItem(blockSupplier.get(), new Item.Properties())
            );
            PHANTOM_ITEM_MAP.put(name, item);
        }
    }

    public static final Map<String, Supplier<Item>> KITSUNEBI_ITEMS = Collections.unmodifiableMap(KITSUNEBI_ITEM_MAP);
    public static final Map<String, Supplier<Item>> PHANTOM_ITEMS = Collections.unmodifiableMap(PHANTOM_ITEM_MAP);

    public static Supplier<Item> getItem(String color) {
        return KITSUNEBI_ITEMS.get(color);
    }

    public static Supplier<Item> getPhantomItem(String name) {
        return PHANTOM_ITEMS.get(name);
    }

    public static void init() {
        // クラスロード用
    }
}
