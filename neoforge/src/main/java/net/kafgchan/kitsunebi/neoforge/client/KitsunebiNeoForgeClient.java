package net.kafgchan.kitsunebi.neoforge.client;

import net.kafgchan.kitsunebi.client.KitsunebiClientHelper;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Supplier;

public class KitsunebiNeoForgeClient {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Supplier<Block> blockSupplier : ModBlocks.KITSUNEBI_BLOCKS.values()) {
                ItemBlockRenderTypes.setRenderLayer(blockSupplier.get(), RenderType.translucent());
            }
        });

        // 手持ち時の狐火パーティクル可視化
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post tickEvent) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                KitsunebiClientHelper.tickClient(mc.player, mc.level);
            }
        });
    }
}
