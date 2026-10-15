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
        for (Supplier<Block> blockSupplier : ModBlocks.KITSUNEBI_BLOCKS.values()) {
            BlockRenderLayerMap.INSTANCE.putBlock(blockSupplier.get(), RenderType.translucent());
        }

        // 手持ち時の狐火パーティクル可視化
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null) {
                KitsunebiClientHelper.tickClient(client.player, client.level);
            }
        });
    }
}
