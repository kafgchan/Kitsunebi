package net.kafgchan.kitsunebi.neoforge.verification;

import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two real clients, localhost only, isolated world. Never enabled in normal play. */
@EventBusSubscriber(modid="kitsunebi")
public final class GeckoNetworkServerVerifier {
    private static int ticks,readyTicks,previous=-1;
    @SubscribeEvent public static void left(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){if(Boolean.getBoolean("kitsunebi.verifyGeckoServer")&&event.getEntity().getServer()!=null)event.getEntity().getServer().halt(false);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        if(!Boolean.getBoolean("kitsunebi.verifyGeckoServer"))return;
        var server=event.getServer();
        if(++ticks>6000)throw new IllegalStateException("Gecko network verification timed out");
        var wearer=server.getPlayerList().getPlayerByName("GeckoWearer");
        var observer=server.getPlayerList().getPlayerByName("GeckoObserver");
        if(wearer==null||observer==null){if(readyTicks>0)server.halt(false);return;}
        if(readyTicks==0){
            var level=server.overworld();
            for(int x=-4;x<=4;x++)for(int z=-4;z<=10;z++)level.setBlockAndUpdate(new BlockPos(x,63,z),Blocks.STONE.defaultBlockState());
            wearer.setGameMode(GameType.SURVIVAL);observer.setGameMode(GameType.SURVIVAL);
            wearer.teleportTo(level,.5,64,.5,0,0);observer.teleportTo(level,.5,64,7.5,180,0);
        }
        int step=++readyTicks/50;
        if(step>17){server.halt(false);return;}
        int mask=step<16?step:0;
        if(mask==previous)return;previous=mask;
        Item[] armor={ModItems.KITSUNEBI_HEADDRESS.get(),ModItems.KITSUNEBI_HAORI.get(),ModItems.KITSUNEBI_HAKAMA.get(),ModItems.KITSUNEBI_TABI.get()};
        EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
        for(int i=0;i<4;i++)wearer.setItemSlot(slots[i],(mask&(1<<i))==0?ItemStack.EMPTY:new ItemStack(armor[i]));
        System.out.println("GECKO_NETWORK equipment mask="+mask);
    }
}


