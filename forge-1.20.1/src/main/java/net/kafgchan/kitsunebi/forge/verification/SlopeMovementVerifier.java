package net.kafgchan.kitsunebi.forge.verification;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.kafgchan.kitsunebi.block.phantom.*;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.server.ServerStartedEvent;

/** Opt-in runtime test: enter from flat ground, traverse, and compare stairs. */
public final class SlopeMovementVerifier {
    public static void verify(ServerStartedEvent event) {
        String result;
        try {
            var level=event.getServer().overworld();

            AltarCompatVerifier.verify(level);
            AirWalkVerifier.verify(level);
            MiningRegressionVerifier.verify(level);
            EquipmentFeaturesVerifier.verify(level);
            StackingVerifier.verify(level);
            AltarMenuVerifier.verify(level);
            var slopes=java.util.List.of(ModBlocks.PHANTOM_SLOPE_HIGH.get(),ModBlocks.PHANTOM_SLOPE_MID.get(),ModBlocks.PHANTOM_SLOPE_LOW.get());
            var stairs=java.util.List.of(ModBlocks.PHANTOM_STAIRS_HIGH.get(),ModBlocks.PHANTOM_STAIRS_MID.get(),ModBlocks.PHANTOM_STAIRS_LOW.get());
            int fixture=0,runs=0;
            for(int size=0;size<3;size++) for(Direction facing:Direction.Plane.HORIZONTAL) {
                for(double speed:new double[]{0.1,0.215,0.28}) {
                    int[] ticks=new int[2];
                    for(int kind=0;kind<2;kind++) {
                        Block block=(kind==0?slopes:stairs).get(size);
                        BlockPos pos=new BlockPos(fixture++*8,250,0);
                        int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
                        for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) {
                            level.setBlock(pos.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
                            for(int y=0;y<4;y++) level.setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
                        }
                        BlockState state=block.defaultBlockState().setValue(PhantomSlopeBlock.FACING,facing);
                        level.setBlock(pos,state,3);
                        // Full-height or half-height landing on the high end.
                        for(int z=-2;z<=-1;z++) {
                            double x=0.5,zz=z+0.5;
                            for(int t=0;t<turns;t++){double old=x;x=1-zz;zz=old;}
                            var landing=size==0?Blocks.STONE.defaultBlockState():Blocks.STONE_SLAB.defaultBlockState();
                            level.setBlock(pos.offset((int)Math.floor(x),0,(int)Math.floor(zz)),landing,3);
                        }
                        Player player=new Player(level,pos,0,new GameProfile(UUID.randomUUID(),"SlopeVerifier")) {
                            @Override public boolean isSpectator(){return false;}
                            @Override public boolean isCreative(){return false;}
                        };
                        double x=0.5,z=1.6,dx=0,dz=-speed;
                        for(int t=0;t<turns;t++){double old=x;x=1-z;z=old;old=dx;dx=-dz;dz=old;}
                        player.setPos(pos.getX()+x,pos.getY(),pos.getZ()+z);
                        player.setOnGround(true);
                        player.setYRot(facing.toYRot());
                        player.setSpeed((float)(speed*(1-0.546)));
                        player.setDeltaMovement(dx*0.546,-0.08,dz*0.546);
                        double distance=0;
                        while(distance<2.1 && ticks[kind]<120) {
                            double bx=player.getX(),bz=player.getZ();
                            player.travel(new Vec3(0,0,1));
                            double travelled=Math.hypot(player.getX()-bx,player.getZ()-bz);
                            distance+=travelled;
                            ticks[kind]++;
                        }
                        if(ticks[kind]>=120) throw new AssertionError("Stuck entering "+block+" "+facing);
                        double expected=pos.getY()+(size==0?1:0.5);
                        if(Math.abs(player.getY()-expected)>1e-5) throw new AssertionError("Did not ascend "+block+": "+player.getY()+" != "+expected);
                        player.discard();
                        runs++;
                    }
                    System.out.println("Traversal "+size+" "+facing+" speed="+speed+" slope="+ticks[0]+" stairs="+ticks[1]);
                    if(ticks[0]>ticks[1]+1) throw new AssertionError("Ramp is slower than stairs: "+ticks[0]+" vs "+ticks[1]);
                }
            }
            result="PASS: "+runs+" real Player.travel flat-ground entry/ascent runs, all sizes/directions at slow/walk/sprint inputs; traversal within one tick of stairs.";
            System.out.println(result);
        } catch(Throwable error) {
            result="FAIL: "+error;
            error.printStackTrace();
        }
        try {Files.writeString(Path.of("slope-verification.txt"),result);}catch(Exception error){throw new RuntimeException(error);}
        event.getServer().halt(false);
    }
}
