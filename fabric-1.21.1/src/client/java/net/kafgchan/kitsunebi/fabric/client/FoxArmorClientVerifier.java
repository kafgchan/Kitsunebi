package net.kafgchan.kitsunebi.fabric.client;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisuals;
import net.kafgchan.kitsunebi.item.armor.FoxArmorStyle;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import java.nio.file.*;

/** Opt-in native Fabric rendering check, without a player world. */
public final class FoxArmorClientVerifier {
    private static boolean started;
    public static void tick(Minecraft mc) {
        if (!Boolean.getBoolean("kitsunebi.verifyFoxArmor") || started || !(mc.screen instanceof TitleScreen)) return;
        started=true;mc.setScreen(new Preview());
    }
    private static final class Preview extends Screen {
        private int page,frames;
        private final EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        private final ItemStack[] gear={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};
        private final net.minecraft.world.entity.decoration.ArmorStand wearer=new net.minecraft.world.entity.decoration.ArmorStand(EntityType.ARMOR_STAND,null){
            @Override public ItemStack getItemBySlot(EquipmentSlot slot){for(int i=0;i<4;i++)if(slots[i]==slot)return gear[i];return ItemStack.EMPTY;}
        };
        private final HumanoidModel<LivingEntity> base=new HumanoidModel<>(LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64).bakeRoot());
        private final java.util.Map<FoxArmorStyle,net.kafgchan.kitsunebi.client.model.KitsunebiArmorVisualModel<LivingEntity>> models=new java.util.EnumMap<>(FoxArmorStyle.class);
        {for(var style:FoxArmorStyle.values())models.put(style,KitsunebiArmorVisuals.create(style));}
        Preview(){super(Component.literal("Fox armor / Fabric"));}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){
            try {
                g.fill(0,0,width,height,0xFF20232D);
                g.drawString(font,"Fabric / "+new String[]{"Shuko","Byakko","Kokko","Shinko","mixed"}[page/3]+" / "+new String[]{"front","side","back"}[page%3],8,8,-1);
                for(int mask=0;mask<16;mask++){
                    for(int i=0;i<4;i++){int family=page<12?page/3:(mask+i)%4;gear[i]=(mask&(1<<i))==0?ItemStack.EMPTY:new ItemStack(ModItems.foxArmorItems().get(family*4+i).get());}
                    wearer.tickCount=frames;wearer.setDeltaMovement(0,0,0);
                    base.setupAnim(wearer,0,0,frames,0,0);
                    g.pose().pushPose();g.pose().translate(35+(mask%4)*95,40+(mask/4)*62,100);g.pose().scale(23,23,23);
                    g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(page%3==0?180:page%3==1?90:0));
                    int drawn=0;
                    for(int i=0;i<4;i++)if(!gear[i].isEmpty()){
                        var model=models.get(FoxArmorStyle.of(gear[i]));
                        KitsunebiArmorVisuals.prepare(model,base,wearer,slots[i]);
                        int count=0;for(int j=0;j<9;j++)if(model.body.getChild("tail_"+j).visible)count++;
                        int pieces=Integer.bitCount(mask);int expected=new int[]{0,1,3,6,9}[pieces];
                        if(count!=(FoxArmorStyle.tailOwner(wearer)==slots[i]?expected:0))throw new AssertionError("Native tail count "+mask);
                        drawn+=count;
                        ArmorRenderer.renderPart(g.pose(),g.bufferSource(),15728880,gear[i],model,ResourceLocation.fromNamespaceAndPath("kitsunebi","textures/entity/"+FoxArmorStyle.of(gear[i]).id+"_shrine_palette.png"));
                    }
                    if(drawn!=new int[]{0,1,3,6,9}[Integer.bitCount(mask)])throw new AssertionError("Duplicate native tails");
                    g.flush();g.pose().popPose();g.drawString(font,"mask "+mask,5+(mask%4)*95,84+(mask/4)*62,-1);
                }
                if(++frames==30){
                    g.flush();try(var image=net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){image.writeToFile(Path.of("fox-armor-"+page+".png"));}
                    if(++page<15){frames=0;return;}
                    Files.writeString(Path.of("fox-armor-verification.txt"),"PASS: Fabric native armor renderer, Shuko/Byakko/Kokko/Shinko and mixed colors, all 16 subsets, front/side/back, exactly one 0/1/3/6/9-tail pass.");minecraft.stop();
                }
            }catch(Throwable failure){try{Files.writeString(Path.of("fox-armor-verification.txt"),"FAIL: "+failure);}catch(Exception ignored){}throw new RuntimeException(failure);}
        }
    }
    private FoxArmorClientVerifier(){}
}
