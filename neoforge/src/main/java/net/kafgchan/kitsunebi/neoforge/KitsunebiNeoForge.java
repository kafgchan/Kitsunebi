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
        if(net.neoforged.fml.ModList.get().isLoaded("curios"))net.kafgchan.kitsunebi.neoforge.compat.CuriosCompat.register(modEventBus);
        modEventBus.addListener(net.kafgchan.kitsunebi.neoforge.network.KitsunebiNeoForgeNetwork::registerPayloadHandlers);
        if (Boolean.getBoolean("kitsunebi.verifySlopeMovement")) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.kafgchan.kitsunebi.neoforge.verification.SlopeMovementVerifier::verify);
        }

        if (FMLLoader.getDist().isClient()) {
            if(net.neoforged.fml.ModList.get().isLoaded("curios"))net.kafgchan.kitsunebi.neoforge.compat.CuriosCompat.registerClient(modEventBus);
            modEventBus.addListener(net.kafgchan.kitsunebi.neoforge.client.KitsunebiArmorRendering::register);
            modEventBus.addListener(KitsunebiNeoForgeClient::onClientSetup);
            modEventBus.addListener(KitsunebiNeoForgeClient::onModifyModels);
            modEventBus.addListener(KitsunebiNeoForgeClient::onRegisterMenuScreens);
            modEventBus.addListener(net.kafgchan.kitsunebi.neoforge.client.KitsunebiNeoForgeKeyHandler::registerKeys);
        }
    }
}
