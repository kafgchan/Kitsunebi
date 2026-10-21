package net.kafgchan.kitsunebi.neoforge.verification;

import net.kafgchan.kitsunebi.neoforge.client.gecko.*;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import java.nio.file.*;

/** Opt-in actual GeckoLib render fixture, using no user world. */
public final class GeckoArmorVerifier {
    private static boolean started;
    public static void tick(Minecraft mc){
        if(!Boolean.getBoolean("kitsunebi.verifyGecko")||started||!(mc.screen instanceof TitleScreen))return;
        started=true;mc.setScreen(new Preview());
    }
    private static final class Preview extends Screen {
        private int frames,page;
        private final Item[] items=new Item[4];
        private final ItemStack[] equipment={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};
        private final net.minecraft.world.entity.decoration.ArmorStand wearer=new net.minecraft.world.entity.decoration.ArmorStand(EntityType.ARMOR_STAND,null){
            @Override public ItemStack getItemBySlot(EquipmentSlot slot){for(int i=0;i<4;i++)if(ArmorTailGrowth.SLOTS[i]==slot)return equipment[i];return ItemStack.EMPTY;}
        };
        private final HumanoidModel<LivingEntity> base=new HumanoidModel<>(LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64).bakeRoot());
        Preview(){super(Component.literal("GeckoLib Shuko armor verification"));}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            try{
                for(int i=0;i<4;i++)items[i]=ModItems.foxArmorItems().get(Math.min(3,page/4)*4+i).get();
                g.fill(0,0,width,height,0xFF20232D);
                g.drawString(font,"GeckoLib 4.9.3 / "+new String[]{"Shuko","Byakko","Kokko","Shinko","mixed"}[page/4]+" / "+new String[]{"front","side","back","poses"}[page%4],8,8,0xFFFFFFFF);
                for(int mask=0;mask<16;mask++){
                    if(page>=16)for(int i=0;i<4;i++)items[i]=ModItems.foxArmorItems().get(((mask+i)%4)*4+i).get();
                    for(int i=0;i<4;i++)equipment[i]=(mask&(1<<i))!=0?new ItemStack(items[i]):ItemStack.EMPTY;
                    int count=Integer.bitCount(mask),expected=ArmorTailGrowth.tails(count);
                    if(ArmorTailGrowth.count(wearer,ArmorTailGrowth.FOX)!=count)throw new AssertionError("Combination count "+mask);
                    if(mask==0){if(ArmorTailGrowth.owner(wearer,ArmorTailGrowth.FOX)!=null)throw new AssertionError("Unequipped owner");}
                    g.pose().pushPose();g.pose().translate(35+(mask%4)*95,40+(mask/4)*62,100);g.pose().scale(23,23,23);
                    float angle=page%4==0?180:page%4==1?90:0;
                    g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
                    base.crouching=page%4==3&&mask%4==0;base.riding=page%4==3&&mask%4==3;base.swimAmount=page%4==3&&mask%4==2?1:0;
                    wearer.setSwimming(base.swimAmount>0);wearer.setDeltaMovement(page%4==3&&mask%4==1?.25:0,0,0);
                    wearer.tickCount=frames;
                    base.setupAnim(wearer,frames*.3F,page%4==3?1:0,frames,15,10);
                    int drawn=0;
                    for(int i=0;i<4;i++)if(!equipment[i].isEmpty()){
                        if(!(items[i] instanceof GeoItem))throw new AssertionError("GeoItem adapter missing");
                        var provider=GeoRenderProvider.of(equipment[i]);
                        var renderer=(ShukoGeoRenderer<?>)provider.getGeoArmorRenderer(wearer,equipment[i],ArmorTailGrowth.SLOTS[i],base);
                        if(renderer==null)throw new AssertionError("Geo renderer missing");
                        renderer.prepForRender(wearer,equipment[i],ArmorTailGrowth.SLOTS[i],base,g.bufferSource(),partial,0,0,0,0);
                        draw(renderer,g,items[i],partial);
                        if(frames==0)for(String root:new String[]{"armorHead","armorBody","armorRightArm","armorLeftArm","armorRightLeg","armorLeftLeg","armorRightBoot","armorLeftBoot"})verifyUV(renderer.getGeoModel().getBone(root).orElseThrow(),((net.kafgchan.kitsunebi.item.armor.KitsunebiArmorItem)items[i]).style()==net.kafgchan.kitsunebi.item.armor.FoxArmorStyle.SHINKO?256:128);
                        int visible=0;
                        for(int j=0;j<9;j++)if(!renderer.getGeoModel().getBone("tail_"+j).orElseThrow().isHidden())visible++;
                        int wanted=ArmorTailGrowth.owner(wearer,ArmorTailGrowth.FOX)==ArmorTailGrowth.SLOTS[i]?expected:0;
                        if(visible!=wanted)throw new AssertionError("Tail visibility mask="+mask+" slot="+i+" got="+visible+" expected="+wanted);
                        drawn+=visible;
                    }
                    if(drawn!=expected)throw new AssertionError("Duplicate or missing tails "+mask);
                    g.flush();g.pose().popPose();
                    g.drawString(font,"mask "+mask+" / "+expected+" tails",5+(mask%4)*95,84+(mask/4)*62,0xFFFFFFFF);
                }
                if(++frames==40){
                    g.flush();try(var shot=net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){shot.writeToFile(Path.of("gecko-armor-"+page+".png"));}
                    if(++page<20){frames=0;return;}
                    Files.writeString(Path.of("gecko-verification.txt"),"PASS: actual GeckoLib renderer; Shuko/Byakko/Kokko/Shinko and mixed colors, all 16 armor subsets for each family including removal; one tail owner per wearer, 0/1/3/6/9 tails; front/side/back and crouch/walk/swim/riding pose fixture. This fixture is not a multiplayer test.");minecraft.stop();
                }
            }catch(Throwable error){try{Files.writeString(Path.of("gecko-verification.txt"),"FAIL: "+error);}catch(Exception ignored){}throw new RuntimeException(error);}
        }
        @SuppressWarnings({"rawtypes","unchecked"})
        private void draw(GeoArmorRenderer renderer,GuiGraphics g,Item item,float partial){renderer.defaultRender(g.pose(),(Item & GeoItem)item,g.bufferSource(),null,null,0,partial,15728880);}
        /** Regression: sub-pixel trim must not bake to a palette boundary or a zero-area UV. */
        private void verifyUV(software.bernie.geckolib.cache.object.GeoBone bone,int textureSize){
            for(var cube:bone.getCubes())for(var quad:cube.quads())if(quad!=null){
                float minU=Float.POSITIVE_INFINITY,maxU=Float.NEGATIVE_INFINITY,minV=Float.POSITIVE_INFINITY,maxV=Float.NEGATIVE_INFINITY;
                for(var vertex:quad.vertices()){
                    minU=Math.min(minU,vertex.texU());maxU=Math.max(maxU,vertex.texU());minV=Math.min(minV,vertex.texV());maxV=Math.max(maxV,vertex.texV());
                    float u=(vertex.texU()*textureSize)%32,v=(vertex.texV()*textureSize)%32;
                    if(u<.5F||u>31.5F||v<.5F||v>31.5F)throw new AssertionError("Palette boundary UV: "+bone.getName());
                }
                if(maxU-minU<.00001F||maxV-minV<.00001F)throw new AssertionError("Collapsed UV: "+bone.getName());
            }
            for(var child:bone.getChildBones())verifyUV(child,textureSize);
        }
    }
    private GeckoArmorVerifier(){}
}

