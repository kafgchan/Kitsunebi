package net.kafgchan.kitsunebi.registry;

import net.kafgchan.kitsunebi.menu.KitsunebiAltarMenu;
import net.kafgchan.kitsunebi.platform.Services;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class ModMenus {
    public static final Supplier<MenuType<KitsunebiAltarMenu>> KITSUNEBI_ALTAR_MENU = Services.PLATFORM.registerMenuType(
            "kitsunebi_altar_menu",
            KitsunebiAltarMenu::new
    );

    public static void init() {
        // クラスロード用
    }
}
