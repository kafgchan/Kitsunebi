package net.kafgchan.kitsunebi.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals;
import net.kafgchan.kitsunebi.registry.ModItems;

public final class KitsunebiArmorRendering {
    public static void register() {
        var models=new java.util.EnumMap<net.kafgchan.kitsunebi.item.armor.FoxArmorStyle,net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisualModel<net.minecraft.world.entity.LivingEntity>>(net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.class);
        for(var style:net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.values())models.put(style,KitsunebiArmorVisuals.create(style));
        ArmorRenderer.register((pose,buffers,stack,entity,slot,light,original)->{
            var style=net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.of(stack);
            var model=models.get(style);
            KitsunebiArmorVisuals.prepare(model,original,entity,slot);
            ArmorRenderer.renderPart(pose,buffers,light,stack,model,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kitsunebi", "textures/entity/"+net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.of(stack).id+"_shrine_palette.png"));
            if(style==net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.SHINKO)
                model.renderToBuffer(pose,buffers.getBuffer(net.minecraft.client.renderer.RenderType.eyes(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kitsunebi","textures/entity/shinko_shrine_palette_glowmask.png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,-1);
        },ModItems.foxArmorItems().stream().map(java.util.function.Supplier::get).toArray(net.minecraft.world.item.Item[]::new));
    }
    private KitsunebiArmorRendering(){}
}
