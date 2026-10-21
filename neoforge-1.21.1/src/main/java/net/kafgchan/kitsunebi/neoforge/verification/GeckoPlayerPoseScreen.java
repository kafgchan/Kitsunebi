package net.kafgchan.kitsunebi.neoforge.verification;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.nio.file.*;

/** Actual PlayerRenderer pose checks using an untracked copy of the synchronized remote player. */
public final class GeckoPlayerPoseScreen extends Screen {
    private final RemotePlayer sample;
    private int pose,frames,page;
    private Entity mount;
    private final net.minecraft.world.entity.decoration.ArmorStand[] stands=new net.minecraft.world.entity.decoration.ArmorStand[4];
    private net.minecraft.resources.ResourceLocation overlayTexture;
    private final String[] names={"walk","sprint","crouch","jump","swim","elytra","riding"};
    public GeckoPlayerPoseScreen(net.minecraft.client.player.AbstractClientPlayer source){
        super(Component.literal("Shuko player pose verification"));
        sample=new RemotePlayer(Minecraft.getInstance().level,source.getGameProfile()){
            @Override public boolean isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart part){return true;}
            @Override public net.minecraft.client.resources.PlayerSkin getSkin(){
                if(page%9<3||overlayTexture==null)return super.getSkin();
                return new net.minecraft.client.resources.PlayerSkin(overlayTexture,"",null,null,(page%9)%2==0?net.minecraft.client.resources.PlayerSkin.Model.SLIM:net.minecraft.client.resources.PlayerSkin.Model.WIDE,false);
            }
            @Override public int getFallFlyingTicks(){return pose==5?20:0;}
            @Override public float getSwimAmount(float partial){return pose==4?1:0;}
        };
        for(var slot:net.kafgchan.kitsunebi.neoforge.client.gecko.ArmorTailGrowth.SLOTS)sample.setItemSlot(slot,source.getItemBySlot(slot).copy());
        if(Boolean.getBoolean("kitsunebi.verifySkinFit")){
            net.minecraft.world.item.Item[] armor={net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HEADDRESS.get(),net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HAORI.get(),net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_HAKAMA.get(),net.kafgchan.kitsunebi.registry.ModItems.KITSUNEBI_TABI.get()};
            for(int i=0;i<4;i++)sample.setItemSlot(net.kafgchan.kitsunebi.neoforge.client.gecko.ArmorTailGrowth.SLOTS[i],new net.minecraft.world.item.ItemStack(armor[i]));
        }
        var image=new com.mojang.blaze3d.platform.NativeImage(64,64,false);
        for(int y=0;y<64;y++)for(int x=0;x<64;x++)image.setPixelRGBA(x,y,0xFFFF00FF);
        overlayTexture=Minecraft.getInstance().getTextureManager().register("kitsunebi-skin-fit",new net.minecraft.client.renderer.texture.DynamicTexture(image));
        sample.tickCount=100;
    }
    private void verifyCoveredTorso(com.mojang.blaze3d.platform.NativeImage image){
        double sx=image.getWidth()/(double)width,sy=image.getHeight()/(double)height;
        int magenta=0;
        // Jump fixture: frontal torso, excluding exposed head, hands and neck.
        for(int y=(int)(68*sy);y<(int)(81*sy);y++)for(int x=(int)(346*sx);x<(int)(354*sx);x++){
            int pixel=image.getPixelRGBA(x,y);int r=pixel&255,g=(pixel>>8)&255,b=(pixel>>16)&255;
            if(r>90&&b>90&&g<20)magenta++;
        }
        if(magenta>0)throw new AssertionError("Skin rendered through covered torso: "+magenta+" pixels; page="+page);
        for(int y=(int)(108*sy);y<(int)(121*sy);y++)for(int x=(int)(346*sx);x<(int)(354*sx);x++){int pixel=image.getPixelRGBA(x,y);int r=pixel&255,g=(pixel>>8)&255,b=(pixel>>16)&255;if(r>90&&b>90&&g<20)throw new AssertionError("Skin rendered through covered footwear; page="+page);}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        try{
            if(page>=36){renderArmorStands(g);return;}
            for(int i=0;i<4;i++)sample.setItemSlot(net.kafgchan.kitsunebi.neoforge.client.gecko.ArmorTailGrowth.SLOTS[i],new net.minecraft.world.item.ItemStack(net.kafgchan.kitsunebi.registry.ModItems.foxArmorItems().get((page/9)*4+i).get()));
            int view=page%9;
            g.fill(0,0,width,height,0xFF20232D);
            g.drawString(font,new String[]{"Shuko","Byakko","Kokko","Shinko"}[page/9]+" / PlayerRenderer / "+new String[]{"front","side","back","opaque / wide / front","opaque / slim / front","opaque / wide / side","opaque / slim / side","opaque / wide / back","opaque / slim / back"}[view],8,8,0xFFFFFFFF);
            for(pose=0;pose<7;pose++){
                sample.stopRiding();sample.stopFallFlying();sample.setSwimming(false);sample.setShiftKeyDown(false);sample.setSprinting(false);sample.setPose(Pose.STANDING);
                sample.setDeltaMovement(.1,0,0);sample.tickCount=100+frames;
                if(pose==1){sample.setSprinting(true);sample.setDeltaMovement(.3,0,0);}
                if(pose==2){sample.setShiftKeyDown(true);sample.setPose(Pose.CROUCHING);}
                if(pose==3)sample.setDeltaMovement(0,.3,0);
                if(pose==4){sample.setSwimming(true);sample.setPose(Pose.SWIMMING);}
                if(pose==5){sample.startFallFlying();sample.setPose(Pose.FALL_FLYING);sample.setDeltaMovement(.5,-.1,0);}
                if(pose==6){if(mount==null)mount=EntityType.BOAT.create(minecraft.level);if(mount==null||!sample.startRiding(mount,true))throw new AssertionError("Cannot create riding fixture");}
                sample.walkAnimation.update(pose<2?.8F:0,1);
                sample.yBodyRot=sample.yBodyRotO=180;
                sample.setYRot(180);sample.yRotO=180;sample.setXRot(0);sample.xRotO=0;sample.yHeadRot=sample.yHeadRotO=180;
                int before=GeckoNetworkClientVerifier.renderCalls;
                var rotation=new Quaternionf().rotationZ((float)Math.PI).rotateY((float)Math.toRadians(view>=3?((view-3)/2)*90:view==0?0:view==1?90:180));
                InventoryScreen.renderEntityInInventory(g,50+(pose%4)*100,120+(pose/4)*130,43,new Vector3f(0,0,0),rotation,null,sample);
                if(GeckoNetworkClientVerifier.renderCalls<=before)throw new AssertionError("Player armor layer was not invoked for "+names[pose]);
                g.drawString(font,names[pose],10+(pose%4)*100,135+(pose/4)*130,0xFFFFFFFF);
            }
            if(++frames==35){
                g.flush();try(var shot=net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){shot.writeToFile(Path.of("gecko-player-poses-"+page+".png"));
                    if(view==3||view==4)verifyCoveredTorso(shot);}
                if(++page<46){frames=0;return;}
                Files.writeString(Path.of("gecko-player-poses.txt"),"PASS: Shuko/Byakko/Kokko/Shinko, real PlayerRenderer + GeckoLib armor layer; walking/sprinting/crouching/jump/swimming/elytra/riding render fixtures, front/side/back; opaque outer-skin layers on wide/slim players, torso pixel coverage. Poses forced on an untracked remote-player copy; not a physics/gameplay test.");minecraft.stop();
            }
        }catch(Throwable error){try{Files.writeString(Path.of("gecko-player-poses.txt"),"FAIL: "+error);}catch(Exception ignored){}throw new RuntimeException(error);}
    }
    /** Uses the real ArmorStandRenderer, where collapsed palette UVs were reported. */
    private void renderArmorStands(GuiGraphics g) throws java.io.IOException {
        g.fill(0,0,width,height,0xFF20232D);
        boolean studio=page>=38;
        int studioFamily=studio?(page-38)/2:0;boolean closeUp=studio&&(page-38)%2==1;
        g.drawString(font,studio?new String[]{"Shuko","Byakko","Kokko","Shinko"}[studioFamily]+" / reference remodel / "+(!closeUp?"front + back":"head + torso"):"ArmorStandRenderer / "+(page==36?"front":"oblique"),8,8,-1);
        for(int index=0;index<(studio?2:4);index++){
            int family=studio?studioFamily:index;
            var stand=stands[family];
            if(stand==null){
                stand=new net.minecraft.world.entity.decoration.ArmorStand(minecraft.level,0,64,0);
                stand.setShowArms(true);stands[family]=stand;
                for(int i=0;i<4;i++)stand.setItemSlot(net.kafgchan.kitsunebi.neoforge.client.gecko.ArmorTailGrowth.SLOTS[i],new net.minecraft.world.item.ItemStack(net.kafgchan.kitsunebi.registry.ModItems.foxArmorItems().get(family*4+i).get()));
            }
            stand.tickCount=100+frames;
            stand.setYRot(180);stand.yRotO=180;stand.yBodyRot=stand.yBodyRotO=180;stand.yHeadRot=stand.yHeadRotO=180;
            double angle=studio?(!closeUp?index*180:index*65):(page==36?0:35)+frames*.08;
            var rotation=new Quaternionf().rotationZ((float)Math.PI).rotateY((float)Math.toRadians(angle));
            InventoryScreen.renderEntityInInventory(g,studio?100+index*200:50+family*100,closeUp?435:245,studio?(closeUp?180:102):61,new Vector3f(0,0,0),rotation,null,stand);
            if(!studio)g.drawString(font,new String[]{"Shuko","Byakko","Kokko","Shinko"}[family],27+family*100,262,-1);
        }
        if(++frames==35){
            g.flush();try(var image=net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){image.writeToFile(Path.of("fox-armor-stands-"+(page-36)+".png"));}
            if(++page<46){frames=0;return;}
            Files.writeString(Path.of("gecko-player-poses.txt"),"PASS: Shuko/Byakko/Kokko/Shinko, real PlayerRenderer poses and wide/slim skin coverage; actual ArmorStandRenderer front/oblique UV regression captures. Forced poses are not gameplay tests.");
            minecraft.stop();
        }
    }
}






