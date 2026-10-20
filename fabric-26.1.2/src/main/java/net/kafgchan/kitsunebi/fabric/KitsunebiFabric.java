package net.kafgchan.kitsunebi.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.LoggerFactory;

/** Version-specific porting bootstrap. Gameplay migration is not complete. */
public final class KitsunebiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LoggerFactory.getLogger("kitsunebi").info("Kitsunebi porting bootstrap loaded; gameplay migration is not complete.");
    }
}
