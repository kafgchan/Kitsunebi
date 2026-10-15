package net.kafgchan.kitsunebi.registry;

import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final String[] COLORS = new String[] {
            "white",
            "blue",
            "cyan",
            "green",
            "purple",
            "orange",
            "red"
    };

    private static final Map<String, Supplier<Block>> KITSUNEBI_BLOCK_MAP = new LinkedHashMap<>();

    static {
        for (String color : COLORS) {
            String name = "kitsunebi_" + color;
            Supplier<Block> block = Services.PLATFORM.registerBlock(name, () -> new KitsunebiBlock(color, BlockBehaviour.Properties.of()));
            KITSUNEBI_BLOCK_MAP.put(color, block);
        }
    }

    public static final Map<String, Supplier<Block>> KITSUNEBI_BLOCKS = Collections.unmodifiableMap(KITSUNEBI_BLOCK_MAP);

    public static Supplier<Block> getBlock(String color) {
        return KITSUNEBI_BLOCKS.get(color);
    }

    public static void init() {
        // クラスロード用
    }
}
