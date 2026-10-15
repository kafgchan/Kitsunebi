package net.kafgchan.kitsunebi.fabric;

import net.fabricmc.api.ModInitializer;
import net.kafgchan.kitsunebi.KitsunebiCommon;

public class KitsunebiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        KitsunebiCommon.init();
    }
}
