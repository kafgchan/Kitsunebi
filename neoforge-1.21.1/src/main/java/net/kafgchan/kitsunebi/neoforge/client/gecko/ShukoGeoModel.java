package net.kafgchan.kitsunebi.neoforge.client.gecko;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public final class ShukoGeoModel<T extends Item & GeoItem> extends GeoModel<T> {
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("kitsunebi",path);}
    @Override public ResourceLocation getModelResource(T item){return id(((net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem)item).style()==net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.SHINKO?"geo/shinko_armor.geo.json":"geo/shuko_armor.geo.json");}
    @Override public ResourceLocation getTextureResource(T item){return id("textures/entity/"+((net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem)item).style().id+"_shrine_palette.png");}
    @Override public ResourceLocation getAnimationResource(T item){return id(((net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem)item).style()==net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.SHINKO?"animations/shinko_armor.animation.json":"animations/shuko_armor.animation.json");}
    @Override public void setCustomAnimations(T item,long id,AnimationState<T> state){
        var entity=state.getData(DataTickets.ENTITY);
        if(!(entity instanceof net.minecraft.world.entity.LivingEntity wearer))return;
        animateTails(wearer,wearer.tickCount+state.getPartialTick());
        if(((net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem)item).style()==net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.SHINKO){
            float age=wearer.tickCount+state.getPartialTick();
            for(int i=0;i<4;i++)getBone("divine_flame_"+i).orElseThrow().setPosY(-(float)Math.sin(age*.045F+i)*.12F);
        }
    }
    public void animateTails(net.minecraft.world.entity.LivingEntity wearer,float age){
        int tails=ArmorTailGrowth.tails(ArmorTailGrowth.count(wearer,ArmorTailGrowth.FOX));
        double speed=wearer.getDeltaMovement().horizontalDistance();
        float lift=(float)Math.min(.32,speed*.8);
        for(int i=0;i<9;i++){
            var bone=getBone("tail_"+i).orElseThrow();
            float fan=(i-(tails-1)/2F)*(tails<=1?0:2.7F/(tails-1));
            bone.setPosX((i-(tails-1)/2F)*.45F);
            bone.setRotY(-fan-(float)Math.sin(age*.045F+i*.7F)*.07F);
            bone.setRotX(1.05F-lift-(float)Math.sin(i*1.7F)*.025F-(float)Math.sin(age*.04F+i*.9F)*.06F);
            getBone("tail_"+i+"_mid").orElseThrow().setRotX(-.04F-lift*.15F-(float)Math.sin(age*.06F-i*.4F)*.025F);
        }
    }
}
