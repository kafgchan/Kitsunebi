package net.kafgchan.kitsunebi.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals;
import net.kafgchan.kitsunebi.registry.ModItems;

public final class KitsunebiArmorRendering {
    public static void register() {
        var model=KitsunebiArmorVisuals.create();
        ArmorRenderer.register((pose,buffers,stack,entity,slot,light,original)->{
            KitsunebiArmorVisuals.prepare(model,original,entity,slot);
            ArmorRenderer.renderPart(pose,buffers,light,stack,model,KitsunebiArmorVisuals.TEXTURE);
        },ModItems.KITSUNEBI_HEADDRESS.get(),ModItems.KITSUNEBI_HAORI.get(),ModItems.KITSUNEBI_HAKAMA.get(),ModItems.KITSUNEBI_TABI.get());
    }
    private KitsunebiArmorRendering(){}
}
