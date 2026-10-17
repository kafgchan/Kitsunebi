package net.kafgchan.kitsunebi.util;

import net.kafgchan.kitsunebi.item.KitsunebiGogglesItem;
import net.kafgchan.kitsunebi.item.KitsunebiItem;
import net.kafgchan.kitsunebi.item.KitsunebiToolItem;
import net.kafgchan.kitsunebi.platform.Services;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * 狐火・幻影ブロックの可視化判定ヘルパー
 */
public class KitsunebiVisionHelper {

    /**
     * プレイヤーが狐火または幻影ブロックを可視化（選択枠表示・パーティクル・ハイライト）できるか総合判定
     */
    public static boolean canSeePhantom(Player player) {
        if (player == null) return false;
        return isHoldingKitsunebiRelatedItem(player) || isEquippedGoggles(player);
    }

    /**
     * 狐火ゴーグルを頭防具スロットまたはCurios/Accessoriesスロットに装備しているか判定
     */
    public static boolean isEquippedGoggles(Player player) {
        if (player == null) return false;

        // 1. バニラ頭防具スロット
        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!headStack.isEmpty() && headStack.getItem() instanceof KitsunebiGogglesItem) {
            return true;
        }

        // 2. Curios (NeoForge) または Accessories / Trinkets (Fabric)
        if (ModItems.KITSUNEBI_GOGGLES != null && ModItems.KITSUNEBI_GOGGLES.get() != null) {
            return Services.PLATFORM.isCurioOrAccessoryEquipped(player, ModItems.KITSUNEBI_GOGGLES.get());
        }

        return false;
    }

    /**
     * メインハンドまたはオフハンドに狐火関連アイテム（狐火、御幣、ゴーグル、幻影ブロック）を所持しているか判定
     */
    public static boolean isHoldingKitsunebiRelatedItem(Player player) {
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return isRelatedItem(main) || isRelatedItem(off);
    }

    /**
     * 指定のアイテムスタックが狐火・幻影ブロック関連アイテムであるか判定
     */
    public static boolean isRelatedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        var item = stack.getItem();
        if (item instanceof KitsunebiItem || item instanceof KitsunebiToolItem || item instanceof KitsunebiGogglesItem) {
            return true;
        }

        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof net.kafgchan.kitsunebi.block.phantom.IPhantomBlock;
        }

        return false;
    }
}
