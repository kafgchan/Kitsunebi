package net.kafgchan.kitsunebi.util;

import net.kafgchan.kitsunebi.item.KitsunebiGogglesItem;
import net.kafgchan.kitsunebi.item.KitsunebiItem;
import net.kafgchan.kitsunebi.item.KitsunebiToolItem;
import net.kafgchan.kitsunebi.platform.Services;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

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
        if (!headStack.isEmpty() && (headStack.getItem() instanceof KitsunebiGogglesItem || headStack.getItem() instanceof net.kafgchan.kitsunebi.item.armor.KitsunebiHeaddressItem)) {
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

    /**
     * 狐火・幻影ブロックを破壊した際に対応アイテムをドロップするか判定
     * - 狐火の御幣で破壊: 常に対応アイテムをドロップ
     * - 狐火の仮面を装備 ＋ シルクタッチ付き道具で破壊: 対応アイテムをドロップ
     * - それ以外（素手、通常道具、シルクタッチのみ、仮面のみ、クリエイティブ等）: ドロップなし
     */
    public static boolean shouldDropBlock(Player player, ItemStack tool) {
        if (player == null || player.isCreative()) return false;

        // 1. 狐火の御幣で破壊した場合
        if (tool != null && tool.getItem() instanceof KitsunebiToolItem) {
            return true;
        }

        // 2. 狐火の仮面を装備し、シルクタッチ付き道具で破壊した場合
        if (isEquippedGoggles(player) && tool != null && hasSilkTouch(tool)) {
            return true;
        }

        return false;
    }

    /**
     * 道具にシルクタッチ（Silk Touch）エンチャントが付与されているか判定
     */
    public static boolean hasSilkTouch(ItemStack tool) {
        if (tool == null || tool.isEmpty()) return false;
        ItemEnchantments enchantments = tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(Enchantments.SILK_TOUCH)) {
                return true;
            }
        }
        return false;
    }
}
