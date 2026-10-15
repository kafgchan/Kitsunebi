package net.kafgchan.kitsunebi.neoforge;

import net.kafgchan.kitsunebi.Constants;
import net.kafgchan.kitsunebi.KitsunebiCommon;
import net.kafgchan.kitsunebi.neoforge.client.KitsunebiNeoForgeClient;
import net.kafgchan.kitsunebi.neoforge.platform.NeoForgePlatformHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(Constants.MOD_ID)
public class KitsunebiNeoForge {
    public KitsunebiNeoForge(IEventBus modEventBus) {
        KitsunebiCommon.init();
        NeoForgePlatformHelper.registerBuses(modEventBus);

        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(KitsunebiNeoForgeClient::onClientSetup);
        }
    }
}
