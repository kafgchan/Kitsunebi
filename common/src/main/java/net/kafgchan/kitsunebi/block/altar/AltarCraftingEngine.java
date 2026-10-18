package net.kafgchan.kitsunebi.block.altar;

import net.kafgchan.kitsunebi.item.KitsunebiItem;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * 狐火の祭壇におけるクラフト検証・素材消費エンジン
 */
public class AltarCraftingEngine {

    public enum Category {
        KITSUNEBI("category.kitsunebi.fire"),
        ARCHITECTURAL("category.kitsunebi.block"),
        PHANTOM("category.kitsunebi.phantom");

        private final String translationKey;
        Category(String translationKey) { this.translationKey = translationKey; }
        public String getTranslationKey() { return translationKey; }
    }

    public record RecipeResult(ItemStack result, List<RequiredIngredient> ingredients, boolean canCraft) {}

    public record RequiredIngredient(String label, int requiredCount, int providedCount) {
        public boolean isSatisfied() { return providedCount >= requiredCount; }
    }

    /**
     * 現在のコンテナ内容と選択状態から、製作可能判定および完成品プレビュー・必要素材を計算
     */
    public static RecipeResult calculate(Container input, Category category, int selectionIndex, int quantity) {
        int q = Math.max(1, Math.min(64, quantity));
        ItemStack outputStack = ItemStack.EMPTY;
        List<RequiredIngredient> required = new ArrayList<>();

        if (category == Category.KITSUNEBI) {
            String[] colors = ModBlocks.COLORS;
            int idx = Math.max(0, Math.min(colors.length - 1, selectionIndex));
            String color = colors[idx];
            outputStack = new ItemStack(ModItems.getItem(color).get(), q);

            // 1. 発光素材 (グロウストーンダスト または 輝くイカスミ)
            int providedLight = countItems(input, Items.GLOWSTONE_DUST, Items.GLOW_INK_SAC);
            required.add(new RequiredIngredient("Glow Dust / Glow Ink", q, providedLight));

            // 2. アメジストの欠片
            int providedAmethyst = countItems(input, Items.AMETHYST_SHARD);
            required.add(new RequiredIngredient("Amethyst Shard", q, providedAmethyst));

            // 3. 竹
            int providedBamboo = countItems(input, Items.BAMBOO);
            required.add(new RequiredIngredient("Bamboo", 4 * q, providedBamboo));

            // 4. ラピスラズリ
            int providedLapis = countItems(input, Items.LAPIS_LAZULI);
            required.add(new RequiredIngredient("Lapis Lazuli", 2 * q, providedLapis));

            // 5. 各色染料
            Item dye = getDyeForColor(color);
            int providedDye = countItems(input, dye);
            required.add(new RequiredIngredient("Dye", q, providedDye));

        } else if (category == Category.ARCHITECTURAL) {
            // 狐火ブロック
            outputStack = new ItemStack(ModBlocks.ARCHITECTURAL_KITSUNEBI.get().asItem(), q);

            // 1. 狐火×8 (7色のどれでも可・混在可)
            int providedKitsunebi = countKitsunebiFire(input);
            required.add(new RequiredIngredient("Kitsunebi Fire", 8 * q, providedKitsunebi));

            // 2. アメジストブロック×1
            int providedAmethystBlock = countItems(input, Items.AMETHYST_BLOCK);
            required.add(new RequiredIngredient("Amethyst Block", q, providedAmethystBlock));

        } else if (category == Category.PHANTOM) {
            outputStack = getPhantomOutputStack(selectionIndex, q);
            if (selectionIndex == 0) {
                // 幻影フルブロック
                int providedKitsunebiBlock = countItems(input, ModBlocks.ARCHITECTURAL_KITSUNEBI.get().asItem());
                required.add(new RequiredIngredient("Kitsunebi Block", q, providedKitsunebiBlock));

                int providedSlimeGroup = countItems(input, Items.SLIME_BLOCK, Items.HONEY_BLOCK);
                required.add(new RequiredIngredient("Slime / Honey Block", 4 * q, providedSlimeGroup));

                int providedSoulGroup = countItems(input, Items.SOUL_SAND, Items.SOUL_SOIL);
                required.add(new RequiredIngredient("Soul Sand / Soul Soil", 4 * q, providedSoulGroup));
            } else if (selectionIndex == 1 || selectionIndex == 2) {
                // 横ハーフ / 縦ハーフ (3フル -> 6ハーフ)
                int neededFull = (int) Math.ceil(q * 3.0 / 6.0);
                int provided = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", neededFull, provided));
            } else if (selectionIndex == 3 || selectionIndex == 4) {
                // 横クォーター / 縦クォーター (3フル -> 18クォーター)
                int neededFull = (int) Math.ceil(q * 3.0 / 18.0);
                int provided = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", neededFull, provided));
            } else if (selectionIndex == 5 || selectionIndex == 8) {
                // 高階段 / 高スロープ (6フル -> 4高)
                int neededFull = (int) Math.ceil(q * 6.0 / 4.0);
                int provided = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", neededFull, provided));
            } else if (selectionIndex == 6 || selectionIndex == 9) {
                // 中階段 / 中スロープ (6ハーフ -> 4中、横・縦混合可)
                int neededHalf = (int) Math.ceil(q * 6.0 / 4.0);
                int provided = countItems(input, ModBlocks.PHANTOM_SLAB.get().asItem(), ModBlocks.PHANTOM_VERTICAL_SLAB.get().asItem());
                required.add(new RequiredIngredient("Phantom Slab (H/V)", neededHalf, provided));
            } else if (selectionIndex == 7 || selectionIndex == 10) {
                // 低階段 / 低スロープ (6クォーター -> 4低、横・縦混合可)
                int neededQuarter = (int) Math.ceil(q * 6.0 / 4.0);
                int provided = countItems(input, ModBlocks.PHANTOM_QUARTER_SLAB.get().asItem(), ModBlocks.PHANTOM_VERTICAL_QUARTER.get().asItem());
                required.add(new RequiredIngredient("Phantom Quarter (H/V)", neededQuarter, provided));
            } else if (selectionIndex == 11 || selectionIndex == 12 || selectionIndex == 16) {
                // 壁(11), フェンス(12), ボタン(16) (フル1対1)
                int provided = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", q, provided));
            } else if (selectionIndex == 13) {
                // 扉 (6フル -> 3扉)
                int neededFull = (int) Math.ceil(q * 6.0 / 3.0);
                int provided = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", neededFull, provided));
            } else if (selectionIndex == 14) {
                // 感圧板 (2フル -> 1)
                int neededFull = 2 * q;
                int provided = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", neededFull, provided));
            } else if (selectionIndex == 15) {
                // レバー (1フル + 1棒 -> 1)
                int providedFull = countItems(input, ModBlocks.PHANTOM_BLOCK.get().asItem());
                required.add(new RequiredIngredient("Phantom Block", q, providedFull));
                int providedStick = countItems(input, Items.STICK);
                required.add(new RequiredIngredient("Stick", q, providedStick));
            }
        }

        boolean canCraft = !required.isEmpty() && required.stream().allMatch(RequiredIngredient::isSatisfied);
        return new RecipeResult(canCraft ? outputStack : ItemStack.EMPTY, required, canCraft);
    }

    /**
     * 製作確定時に、入力スロット番号の昇順（左上から右下）で必要素材を安全に消費
     */
    public static void consumeIngredients(Container input, Category category, int selectionIndex, int quantity) {
        RecipeResult check = calculate(input, category, selectionIndex, quantity);
        if (!check.canCraft()) return;

        int q = Math.max(1, Math.min(64, quantity));

        if (category == Category.KITSUNEBI) {
            String color = ModBlocks.COLORS[Math.max(0, Math.min(ModBlocks.COLORS.length - 1, selectionIndex))];
            consumeAny(input, q, Items.GLOWSTONE_DUST, Items.GLOW_INK_SAC);
            consumeAny(input, q, Items.AMETHYST_SHARD);
            consumeAny(input, 4 * q, Items.BAMBOO);
            consumeAny(input, 2 * q, Items.LAPIS_LAZULI);
            consumeAny(input, q, getDyeForColor(color));

        } else if (category == Category.ARCHITECTURAL) {
            consumeKitsunebiFire(input, 8 * q);
            consumeAny(input, q, Items.AMETHYST_BLOCK);

        } else if (category == Category.PHANTOM) {
            if (selectionIndex == 0) {
                consumeAny(input, q, ModBlocks.ARCHITECTURAL_KITSUNEBI.get().asItem());
                consumeAny(input, 4 * q, Items.SLIME_BLOCK, Items.HONEY_BLOCK);
                consumeAny(input, 4 * q, Items.SOUL_SAND, Items.SOUL_SOIL);
            } else if (selectionIndex == 1 || selectionIndex == 2) {
                int neededFull = (int) Math.ceil(q * 3.0 / 6.0);
                consumeAny(input, neededFull, ModBlocks.PHANTOM_BLOCK.get().asItem());
            } else if (selectionIndex == 3 || selectionIndex == 4) {
                int neededFull = (int) Math.ceil(q * 3.0 / 18.0);
                consumeAny(input, neededFull, ModBlocks.PHANTOM_BLOCK.get().asItem());
            } else if (selectionIndex == 5 || selectionIndex == 8) {
                int neededFull = (int) Math.ceil(q * 6.0 / 4.0);
                consumeAny(input, neededFull, ModBlocks.PHANTOM_BLOCK.get().asItem());
            } else if (selectionIndex == 6 || selectionIndex == 9) {
                int neededHalf = (int) Math.ceil(q * 6.0 / 4.0);
                consumeAny(input, neededHalf, ModBlocks.PHANTOM_SLAB.get().asItem(), ModBlocks.PHANTOM_VERTICAL_SLAB.get().asItem());
            } else if (selectionIndex == 7 || selectionIndex == 10) {
                int neededQuarter = (int) Math.ceil(q * 6.0 / 4.0);
                consumeAny(input, neededQuarter, ModBlocks.PHANTOM_QUARTER_SLAB.get().asItem(), ModBlocks.PHANTOM_VERTICAL_QUARTER.get().asItem());
            } else if (selectionIndex == 11 || selectionIndex == 12 || selectionIndex == 16) {
                consumeAny(input, q, ModBlocks.PHANTOM_BLOCK.get().asItem());
            } else if (selectionIndex == 13) {
                int neededFull = (int) Math.ceil(q * 6.0 / 3.0);
                consumeAny(input, neededFull, ModBlocks.PHANTOM_BLOCK.get().asItem());
            } else if (selectionIndex == 14) {
                consumeAny(input, 2 * q, ModBlocks.PHANTOM_BLOCK.get().asItem());
            } else if (selectionIndex == 15) {
                consumeAny(input, q, ModBlocks.PHANTOM_BLOCK.get().asItem());
                consumeAny(input, q, Items.STICK);
            }
        }
    }

    public static ItemStack getPhantomOutputStack(int selectionIndex, int count) {
        Item item = switch (selectionIndex) {
            case 0 -> ModBlocks.PHANTOM_BLOCK.get().asItem();
            case 1 -> ModBlocks.PHANTOM_SLAB.get().asItem();
            case 2 -> ModBlocks.PHANTOM_VERTICAL_SLAB.get().asItem();
            case 3 -> ModBlocks.PHANTOM_QUARTER_SLAB.get().asItem();
            case 4 -> ModBlocks.PHANTOM_VERTICAL_QUARTER.get().asItem();
            case 5 -> ModBlocks.PHANTOM_STAIRS_HIGH.get().asItem();
            case 6 -> ModBlocks.PHANTOM_STAIRS_MID.get().asItem();
            case 7 -> ModBlocks.PHANTOM_STAIRS_LOW.get().asItem();
            case 8 -> ModBlocks.PHANTOM_SLOPE_HIGH.get().asItem();
            case 9 -> ModBlocks.PHANTOM_SLOPE_MID.get().asItem();
            case 10 -> ModBlocks.PHANTOM_SLOPE_LOW.get().asItem();
            case 11 -> ModBlocks.PHANTOM_WALL.get().asItem();
            case 12 -> ModBlocks.PHANTOM_FENCE.get().asItem();
            case 13 -> ModBlocks.PHANTOM_DOOR.get().asItem();
            case 14 -> ModBlocks.PHANTOM_PRESSURE_PLATE.get().asItem();
            case 15 -> ModBlocks.PHANTOM_LEVER.get().asItem();
            case 16 -> ModBlocks.PHANTOM_BUTTON.get().asItem();
            default -> ModBlocks.PHANTOM_BLOCK.get().asItem();
        };
        return new ItemStack(item, count);
    }

    public static Item getDyeForColor(String color) {
        return switch (color) {
            case "white" -> Items.WHITE_DYE;
            case "blue" -> Items.BLUE_DYE;
            case "cyan" -> Items.LIGHT_BLUE_DYE;
            case "green" -> Items.GREEN_DYE;
            case "purple" -> Items.PURPLE_DYE;
            case "orange" -> Items.ORANGE_DYE;
            case "red" -> Items.RED_DYE;
            default -> Items.WHITE_DYE;
        };
    }

    private static int countItems(Container input, Item... validItems) {
        Set<Item> set = Set.of(validItems);
        int total = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && set.contains(stack.getItem())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int countKitsunebiFire(Container input) {
        int total = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof KitsunebiItem) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consumeAny(Container input, int count, Item... validItems) {
        Set<Item> set = Set.of(validItems);
        int remaining = count;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && set.contains(stack.getItem())) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }

    private static void consumeKitsunebiFire(Container input, int count) {
        int remaining = count;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof KitsunebiItem) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }
}
