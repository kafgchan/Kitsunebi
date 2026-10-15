package net.kafgchan.kitsunebi;

import net.kafgchan.kitsunebi.platform.Services;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.kafgchan.kitsunebi.registry.ModCreativeTabs;
import net.kafgchan.kitsunebi.registry.ModItems;

public class KitsunebiCommon {
    public static void init() {
        Constants.LOG.info("Initializing {} on platform: {}", Constants.MOD_NAME, Services.PLATFORM.getPlatformName());
        ModBlocks.init();
        ModItems.init();
        ModCreativeTabs.init();
    }
}
