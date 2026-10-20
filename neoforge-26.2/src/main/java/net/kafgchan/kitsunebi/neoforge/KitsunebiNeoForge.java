package net.kafgchan.kitsunebi.neoforge;

import net.neoforged.fml.common.Mod;
import org.slf4j.LoggerFactory;

/** Version-specific porting bootstrap. Gameplay migration is not complete. */
@Mod("kitsunebi")
public final class KitsunebiNeoForge {
    public KitsunebiNeoForge() {
        LoggerFactory.getLogger("kitsunebi").info("Kitsunebi porting bootstrap loaded; gameplay migration is not complete.");
    }
}
