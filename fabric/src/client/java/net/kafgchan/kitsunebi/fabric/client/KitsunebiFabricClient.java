package net.kafgchan.kitsunebi.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kafgchan.kitsunebi.client.KitsunebiClientHelper;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class KitsunebiFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(net.kafgchan.kitsunebi.network.KitsunebiAirWalkStatePacket.TYPE,
                (payload,context)->context.client().execute(()->net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState.apply(context.player(),payload.active())));
        KitsunebiArmorRendering.register();
        net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(plugin ->
                plugin.modifyModelAfterBake().register((model, context) ->
                        net.kafgchan.kitsunebi.client.SmoothSlopeModel.replace(context.topLevelId(), model)));
        for (Supplier<Block> blockSupplier : ModBlocks.KITSUNEBI_BLOCKS.values()) {
            BlockRenderLayerMap.INSTANCE.putBlock(blockSupplier.get(), RenderType.translucent());
        }

        net.minecraft.client.gui.screens.MenuScreens.register(
                net.kafgchan.kitsunebi.registry.ModMenus.KITSUNEBI_ALTAR_MENU.get(),
                net.kafgchan.kitsunebi.client.KitsunebiAltarScreen::new
        );

        // キーバインド初期化
        KitsunebiFabricKeyHandler.init();

        // 装備・手持ちの変更時のみ可視化モデルを再構築、およびキー入力処理
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null) {
                CompatClientVerifier.tick(client);
                KitsunebiFabricKeyHandler.onClientTick(client);
                if (KitsunebiClientHelper.tickClient(client.player, client.level) && client.level != null && client.player != null) {
                    KitsunebiClientHelper.refreshVisionSections(client.level, client.player.blockPosition(),
                            client.options.renderDistance().get() + 1, client.levelRenderer::setSectionDirty);
                }
            }
        });
    }
}
