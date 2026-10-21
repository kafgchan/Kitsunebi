package net.kafgchan.kitsunebi.neoforge.npc;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ShukoNpcRenderer extends GeoEntityRenderer<ShukoNpcEntity> {
    public ShukoNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new ShukoNpcModel());
        withScale(0.8F);
        shadowRadius = 0.35F;
    }
}
