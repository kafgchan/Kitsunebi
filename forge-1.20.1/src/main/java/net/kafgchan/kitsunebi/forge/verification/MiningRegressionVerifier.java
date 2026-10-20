package net.kafgchan.kitsunebi.forge.verification;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.kafgchan.kitsunebi.item.tool.ToolStateHelper;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;

/** Runs only inside the isolated verification server, never a user's world. */
public final class MiningRegressionVerifier {
    public static void verify(ServerLevel level) {
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("57dedc78-4136-4169-90f2-d586754609ec"),"MiningVerifier"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        int fixture=0;
        for(Item item:new Item[]{ModItems.KITSUNEBI_SHOVEL.get(),ModItems.KITSUNEBI_PICKAXE.get()}) {
            for(int mode:new int[]{1,2})for(boolean active:new boolean[]{true,false}) {
                BlockPos center=new BlockPos(3000+fixture++*16,250,3000);
                player.setPos(center.getX()+0.5,252,center.getZ()+0.5);
                player.setXRot(90);
                var block=item==ModItems.KITSUNEBI_SHOVEL.get()?Blocks.DIRT:Blocks.STONE;
                for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)level.setBlock(center.offset(x,0,z),block.defaultBlockState(),3);
                ItemStack tool=new ItemStack(item);
                ToolStateHelper.setMode(tool,mode);ToolStateHelper.setActive(tool,active);
                player.setItemSlot(EquipmentSlot.MAINHAND,tool);
                if(!player.gameMode.destroyBlock(center))throw new AssertionError("Primary harvest failed");
                int radius=active?mode:0,broken=0;
                for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++) {
                    boolean shouldBreak=Math.abs(x)<=radius&&Math.abs(z)<=radius;
                    boolean air=level.getBlockState(center.offset(x,0,z)).isAir();
                    if(air!=shouldBreak)throw new AssertionError("Harvest escaped selected area: "+item+" mode="+mode+" active="+active+" offset="+x+","+z);
                    if(air)broken++;
                }
                int expected=(2*radius+1)*(2*radius+1);
                if(broken!=expected||tool.getDamageValue()!=expected)throw new AssertionError("Wrong block/durability count: "+broken+" / "+tool.getDamageValue()+" expected "+expected);
            }
        }
        System.out.println("PASS: shovel/pickaxe 3x3 and 5x5, skill off, exact bounds and one durability per block; no recursive expansion.");
    }
    private MiningRegressionVerifier(){}
}
