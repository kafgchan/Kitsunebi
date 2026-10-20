package net.kafgchan.kitsunebi.registry;

import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.kafgchan.kitsunebi.block.phantom.*;
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
    private static final Map<String, Supplier<Block>> PHANTOM_BLOCK_MAP = new LinkedHashMap<>();

    // --- 光源狐火（7色） ---
    static {
        for (String color : COLORS) {
            String name = "kitsunebi_" + color;
            Supplier<Block> block = Services.PLATFORM.registerBlock(name, () -> new KitsunebiBlock(color, BlockBehaviour.Properties.of()));
            KITSUNEBI_BLOCK_MAP.put(color, block);
        }
    }

    // --- A: 建築用狐火ブロック（衝突なし・すり抜け） ---
    public static final Supplier<Block> ARCHITECTURAL_KITSUNEBI = registerPhantom(
            "architectural_kitsunebi",
            () -> new ArchitecturalKitsunebiBlock(BlockBehaviour.Properties.of())
    );

    // --- 専用作業台：狐火の祭壇 ---
    public static final Supplier<Block> KITSUNEBI_ALTAR = Services.PLATFORM.registerBlock(
            "kitsunebi_altar",
            () -> new net.kafgchan.kitsunebi.block.altar.KitsunebiAltarBlock(BlockBehaviour.Properties.of())
    );

    // --- B: 幻影ブロック群（衝突あり・通常不可視） ---
    public static final Supplier<Block> PHANTOM_BLOCK = registerPhantom(
            "phantom_block",
            () -> new PhantomBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_SLAB = registerPhantom(
            "phantom_slab",
            () -> new PhantomSlabBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_VERTICAL_SLAB = registerPhantom(
            "phantom_vertical_slab",
            () -> new PhantomVerticalSlabBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_QUARTER_SLAB = registerPhantom(
            "phantom_quarter_slab",
            () -> new PhantomQuarterSlabBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_VERTICAL_QUARTER = registerPhantom(
            "phantom_vertical_quarter",
            () -> new PhantomVerticalQuarterBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_WALL = registerPhantom(
            "phantom_wall",
            () -> new PhantomWallBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_FENCE = registerPhantom(
            "phantom_fence",
            () -> new PhantomFenceBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_DOOR = registerPhantom(
            "phantom_door",
            () -> new PhantomDoorBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_PRESSURE_PLATE = registerPhantom(
            "phantom_pressure_plate",
            () -> new PhantomPressurePlateBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_BUTTON = registerPhantom(
            "phantom_button",
            () -> new PhantomButtonBlock(BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_LEVER = registerPhantom(
            "phantom_lever",
            () -> new PhantomLeverBlock(BlockBehaviour.Properties.of())
    );

    // --- 階段（高・中・低） ---
    public static final Supplier<Block> PHANTOM_STAIRS_HIGH = registerPhantom(
            "phantom_stairs_high",
            () -> new PhantomStairBlock(StairSlopeSize.HIGH, BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_STAIRS_MID = registerPhantom(
            "phantom_stairs_mid",
            () -> new PhantomStairBlock(StairSlopeSize.MID, BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_STAIRS_LOW = registerPhantom(
            "phantom_stairs_low",
            () -> new PhantomStairBlock(StairSlopeSize.LOW, BlockBehaviour.Properties.of())
    );

    // --- スロープ（高・中・低） ---
    public static final Supplier<Block> PHANTOM_SLOPE_HIGH = registerPhantom(
            "phantom_slope_high",
            () -> new PhantomSlopeBlock(StairSlopeSize.HIGH, BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_SLOPE_MID = registerPhantom(
            "phantom_slope_mid",
            () -> new PhantomSlopeBlock(StairSlopeSize.MID, BlockBehaviour.Properties.of())
    );

    public static final Supplier<Block> PHANTOM_SLOPE_LOW = registerPhantom(
            "phantom_slope_low",
            () -> new PhantomSlopeBlock(StairSlopeSize.LOW, BlockBehaviour.Properties.of())
    );

    private static Supplier<Block> registerPhantom(String name, Supplier<Block> supplier) {
        Supplier<Block> block = Services.PLATFORM.registerBlock(name, supplier);
        PHANTOM_BLOCK_MAP.put(name, block);
        return block;
    }

    public static final Map<String, Supplier<Block>> KITSUNEBI_BLOCKS = Collections.unmodifiableMap(KITSUNEBI_BLOCK_MAP);
    public static final Map<String, Supplier<Block>> PHANTOM_BLOCKS = Collections.unmodifiableMap(PHANTOM_BLOCK_MAP);

    public static Supplier<Block> getBlock(String color) {
        return KITSUNEBI_BLOCKS.get(color);
    }

    public static void init() {
        // クラスロード用
    }
}
