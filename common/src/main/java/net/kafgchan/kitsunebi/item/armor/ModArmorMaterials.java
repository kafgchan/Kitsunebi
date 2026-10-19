package net.kafgchan.kitsunebi.item.armor;

import net.kafgchan.kitsunebi.Constants;
import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/**
 * 狐火防具アーマーマテリアル定義
 * 耐久値 1,048,576、和装白×朱赤
 */
public class ModArmorMaterials {

    public static final Holder<ArmorMaterial> KITSUNEBI = Services.PLATFORM.registerArmorMaterial(
            "kitsunebi",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 8);
                    }),
                    25,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.EMPTY, // 金床修理不可（祭壇修理のみ）
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "kitsunebi"))),
                    3.0F,
                    0.1F
            )
    );

    public static void init() {
        // Ensure class is loaded and ARMOR_MATERIALS is registered before modEventBus registers DeferredRegisters
    }
}
