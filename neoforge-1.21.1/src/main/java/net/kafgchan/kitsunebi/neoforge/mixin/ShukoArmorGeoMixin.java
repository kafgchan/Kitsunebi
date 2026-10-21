package net.kafgchan.kitsunebi.neoforge.mixin;

import net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem;
import net.kafgchan.kitsunebi.neoforge.client.gecko.ShukoGeoRenderer;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;

/** Client-only adapter: existing item registrations and gameplay implementations stay intact. */
@Mixin(KitsunebiArmorItem.class)
public abstract class ShukoArmorGeoMixin implements GeoItem {
    @Unique private final AnimatableInstanceCache kitsunebi$geoCache=GeckoLibUtil.createInstanceCache(this);
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return kitsunebi$geoCache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){}
    @Override public void createGeoRenderer(Consumer<GeoRenderProvider> consumer){
        ShukoGeoRenderer.provide((Item & GeoItem)(Object)this,consumer);
    }
}
