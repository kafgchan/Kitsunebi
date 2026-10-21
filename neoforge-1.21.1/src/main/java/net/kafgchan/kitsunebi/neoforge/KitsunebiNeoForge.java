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
        net.kafgchan.kitsunebi.neoforge.npc.ShukoNpcRegistry.register(modEventBus);
        NeoForgePlatformHelper.registerBuses(modEventBus);
        if(net.neoforged.fml.ModList.get().isLoaded("curios"))net.kafgchan.kitsunebi.neoforge.compat.CuriosCompat.register(modEventBus);

        // Architectury NetworkManager パケット受信登録
        dev.architectury.networking.NetworkManager.registerReceiver(
                dev.architectury.networking.NetworkManager.c2s(),
                net.kafgchan.kitsunebi.network.KitsunebiAirWalkMovePacket.TYPE,
                net.kafgchan.kitsunebi.network.KitsunebiAirWalkMovePacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.setVerticalInput(player, payload.vertical());
                    }
                })
        );
        dev.architectury.networking.NetworkManager.registerReceiver(
                dev.architectury.networking.NetworkManager.s2c(),
                net.kafgchan.kitsunebi.network.KitsunebiAirWalkStatePacket.TYPE,
                net.kafgchan.kitsunebi.network.KitsunebiAirWalkStatePacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState.apply(context.getPlayer(), payload.active());
                })
        );
        dev.architectury.networking.NetworkManager.registerReceiver(
                dev.architectury.networking.NetworkManager.c2s(),
                net.kafgchan.kitsunebi.network.KitsunebiSkillPacket.TYPE,
                net.kafgchan.kitsunebi.network.KitsunebiSkillPacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler.handleSkillAction(player, payload.action());
                    }
                })
        );
        dev.architectury.networking.NetworkManager.registerReceiver(
                dev.architectury.networking.NetworkManager.c2s(),
                net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket.TYPE,
                net.kafgchan.kitsunebi.network.KitsunebiAirWalkPacket.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler.handleAirWalk(player, payload.jump());
                    }
                })
        );

        if (Boolean.getBoolean("kitsunebi.verifySlopeMovement")) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                    net.kafgchan.kitsunebi.neoforge.verification.SlopeMovementVerifier::verify);
        }

        if (FMLLoader.getDist().isClient()) {
            if(net.neoforged.fml.ModList.get().isLoaded("curios"))net.kafgchan.kitsunebi.neoforge.compat.CuriosCompat.registerClient(modEventBus);
            // GeckoLib GeoRenderProvider now owns armor rendering; do not also register the legacy extension.
            modEventBus.addListener(KitsunebiNeoForgeClient::onClientSetup);
            modEventBus.addListener(KitsunebiNeoForgeClient::onModifyModels);
            modEventBus.addListener(KitsunebiNeoForgeClient::onRegisterMenuScreens);
            modEventBus.addListener(net.kafgchan.kitsunebi.neoforge.client.KitsunebiNeoForgeKeyHandler::registerKeys);
        }
    }
}

