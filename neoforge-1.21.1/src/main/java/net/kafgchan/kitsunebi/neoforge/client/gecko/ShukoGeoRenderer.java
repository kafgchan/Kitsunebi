package net.kafgchan.kitsunebi.neoforge.client.gecko;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import java.util.function.Consumer;

/** Geometry/UV-preserving renderer, selected by GeckoLib's own armor-layer hook. */
public final class ShukoGeoRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> {
    public ShukoGeoRenderer(){
        super(new ShukoGeoModel<>());
        addRenderLayer(new software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer<T>(this){
            @Override protected net.minecraft.client.renderer.RenderType getRenderType(T item,net.minecraft.client.renderer.MultiBufferSource buffers){
                return ((net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem)item).style()==net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.SHINKO?super.getRenderType(item,buffers):null;
            }
        });
    }
    public static <I extends Item & GeoItem> void provide(I item,Consumer<GeoRenderProvider> consumer){
        consumer.accept(new GeoRenderProvider(){
            private ShukoGeoRenderer<I> renderer;
            @Override public <E extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(E wearer,ItemStack stack,EquipmentSlot slot,HumanoidModel<E> original){
                if(renderer==null)renderer=new ShukoGeoRenderer<>();return renderer;
            }
        });
    }
    private void show(String name,boolean visible){getGeoModel().getBone(name).orElseThrow().setHidden(!visible);}
    @Override protected void applyBoneVisibilityBySlot(EquipmentSlot slot){
        // Root visibility must be restored on every pass: GeoModels are shared by all wearers.
        for(String name:new String[]{"armorHead","hat","armorBody","armorRightArm","armorLeftArm","armorRightLeg","armorLeftLeg","armorRightBoot","armorLeftBoot"})show(name,true);
        boolean chest=slot==EquipmentSlot.CHEST,legs=slot==EquipmentSlot.LEGS,feet=slot==EquipmentSlot.FEET;
        boolean tailPass=currentEntity instanceof LivingEntity wearer&&ArmorTailGrowth.owner(wearer,ArmorTailGrowth.FOX)==slot;
        show("armorHead",slot==EquipmentSlot.HEAD);show("hat",false);
        show("armorBody",chest||legs||tailPass);
        show("armorRightArm",chest);show("armorLeftArm",chest);
        show("armorRightLeg",legs||chest);show("armorLeftLeg",legs||chest);
        show("armorRightBoot",feet);show("armorLeftBoot",feet);
        show("kimono",chest);show("haori",chest);show("obi",legs);
        for(String side:new String[]{"right","left"}){show(side+"_hakama",legs);show(side+"_coat",chest);show(side+"_tabi",feet);}
        int tails=currentEntity instanceof LivingEntity wearer?ArmorTailGrowth.tails(ArmorTailGrowth.count(wearer,ArmorTailGrowth.FOX)):0;
        for(int i=0;i<9;i++)show("tail_"+i,tailPass&&i<tails);
        if(currentEntity instanceof LivingEntity wearer)net.kafgchan.kitsunebi.neoforge.verification.GeckoNetworkClientVerifier.rendered(wearer,slot,tailPass?tails:0);
    }
    @Override protected void applyBaseTransformations(HumanoidModel<?> base){
        super.applyBaseTransformations(base);
        // The source model uses +/-1.9 instead of GeckoLib's default +/-2 leg pivots.
        for(String side:new String[]{"Right","Left"}){
            var part=side.equals("Right")?base.rightLeg:base.leftLeg;
            for(String suffix:new String[]{"Leg","Boot"})getGeoModel().getBone("armor"+side+suffix).orElseThrow().updatePosition(part.x+(side.equals("Right")?1.9F:-1.9F),12-part.y,part.z);
        }
    }
}

