package net.kafgchan.kitsunebi.platform.services;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public interface IPlatformHelper {
    /**
     * 現在動作中のプラットフォーム名 (NeoForge / Fabric) を返す
     */
    String getPlatformName();

    /**
     * 開発環境（Dev環境）かどうかを判定する
     */
    boolean isDevelopmentEnvironment();

    /**
     * アイテムを登録する
     */
    <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item);

    /**
     * ブロックを登録する
     */
    <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block);

    /**
     * クリエイティブタブを登録する
     */
    Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab);

    CreativeModeTab.Builder creativeTabBuilder();

    /**
     * Curios (NeoForge) または Accessories / Trinkets (Fabric) のアクセサリースロットにアイテムが装備されているか判定
     */
    default boolean isCurioOrAccessoryEquipped(net.minecraft.world.entity.LivingEntity entity, Item item) {
        return false;
    }
}
