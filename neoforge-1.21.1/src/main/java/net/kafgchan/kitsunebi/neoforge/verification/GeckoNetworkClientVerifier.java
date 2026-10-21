package net.kafgchan.kitsunebi.neoforge.verification;

import net.kafgchan.kitsunebi.neoforge.client.gecko.ArmorTailGrowth;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import java.nio.file.*;

/** Observes the actual player armor layer after vanilla equipment synchronization. */
public final class GeckoNetworkClientVerifier {
    public static int renderCalls;
    private static boolean poseStarted;
    private static int ticks,seen,latestMask=-1,latestEntity=-1,renderedAt;
    public static void rendered(LivingEntity wearer,EquipmentSlot slot,int tails){
        if(!Boolean.getBoolean("kitsunebi.verifyGeckoNetwork")||!wearer.getName().getString().equals("GeckoWearer"))return;
        renderCalls++;
        int mask=mask(wearer);
        int expected=ArmorTailGrowth.owner(wearer,ArmorTailGrowth.FOX)==slot?ArmorTailGrowth.tails(Integer.bitCount(mask)):0;
        if(tails!=expected)throw new AssertionError("Actual player armor layer tail count: "+tails+" expected="+expected);
        if(tails>0){latestMask=mask;latestEntity=wearer.getId();renderedAt=ticks;}
    }
    private static int mask(LivingEntity wearer){int mask=0;for(int i=0;i<4;i++)if(ArmorTailGrowth.FOX.test(wearer.getItemBySlot(ArmorTailGrowth.SLOTS[i])))mask|=1<<i;return mask;}
    public static void tick(Minecraft mc){
        if(!Boolean.getBoolean("kitsunebi.verifyGeckoNetwork")||poseStarted)return;
        if(++ticks>4800)throw new AssertionError("Gecko network client timed out; seen masks="+Integer.toHexString(seen));
        if(mc.player==null||mc.level==null)return;
        if(Boolean.getBoolean("kitsunebi.verifySkinFit")){poseStarted=true;mc.setScreen(new GeckoPlayerPoseScreen(mc.player));return;}
        if(mc.player.getName().getString().equals("GeckoWearer"))return;
        var wearer=mc.level.players().stream().filter(p->p.getName().getString().equals("GeckoWearer")).findFirst().orElse(null);
        if(wearer==null)return;
        int mask=mask(wearer);
        if(mask==0||(latestMask==mask&&latestEntity==wearer.getId()&&ticks-renderedAt<20)){
            if((seen&(1<<mask))==0)System.out.println("PASS: remote player rendered mask="+mask+" tails="+ArmorTailGrowth.tails(Integer.bitCount(mask)));
            seen|=1<<mask;
        }
        if(seen==0xffff){
            try{net.minecraft.client.Screenshot.grab(mc.gameDirectory,"gecko-remote-player.png",mc.getMainRenderTarget(),message->{});Files.writeString(Path.of("gecko-network-verification.txt"),"PASS: two real NeoForge clients joined dedicated server; observer rendered remote GeckoWearer through GeckoLib armor layer for all 16 synchronized armor subsets, with 0/1/3/6/9 tails.");}catch(Exception e){throw new RuntimeException(e);}
            poseStarted=true;mc.setScreen(new GeckoPlayerPoseScreen(wearer));
        }
    }
}


