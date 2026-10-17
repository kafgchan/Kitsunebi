package net.kafgchan.kitsunebi.neoforge.verification;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.kafgchan.kitsunebi.block.phantom.*;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

final class StackingVerifier {
    static void verify(net.minecraft.server.level.ServerLevel level) {
        BlockPos pos=new BlockPos(-32,250,0);
        Player player=new Player(level,pos,0,new GameProfile(UUID.randomUUID(),"StackVerifier")) {
            @Override public boolean isSpectator(){return false;}
            @Override public boolean isCreative(){return false;}
        };
        var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        for (Block block : java.util.List.of(ModBlocks.PHANTOM_SLAB.get(), ModBlocks.PHANTOM_VERTICAL_SLAB.get(),
                ModBlocks.PHANTOM_QUARTER_SLAB.get(), ModBlocks.PHANTOM_VERTICAL_QUARTER.get())) {
            boolean vertical=block instanceof PhantomVerticalSlabBlock || block instanceof PhantomVerticalQuarterBlock;
            int layers=block instanceof PhantomSlabBlock || block instanceof PhantomVerticalSlabBlock ? 2 : 4;
            for(Direction facing:Direction.Plane.HORIZONTAL) {
                var initial=block.defaultBlockState();
                if(initial.hasProperty(PhantomVerticalSlabBlock.FACING)) initial=initial.setValue(PhantomVerticalSlabBlock.FACING,facing);
                level.setBlock(pos,initial,3);
                player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(block.asItem(),8));
                for(int i=1;i<layers;i++) {
                    double fraction=(double)i/layers;
                    Vec3 hit=new Vec3(pos.getX()+0.5,pos.getY()+fraction,pos.getZ()+0.5);
                    Direction face=Direction.UP;
                    if(vertical) {
                        double x=0.5,z=fraction;
                        int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
                        for(int t=0;t<turns;t++){double old=x;x=1-z;z=old;}
                        hit=new Vec3(pos.getX()+x,pos.getY()+0.5,pos.getZ()+z);
                        face=facing.getOpposite();
                    }
                    var context=new net.minecraft.world.item.context.UseOnContext(player,hand,new net.minecraft.world.phys.BlockHitResult(hit,face,pos,false));
                    var outcome=player.getItemInHand(hand).useOn(context);
                    if(!outcome.consumesAction()) throw new AssertionError("Stack rejected: "+block+" "+facing+" "+i);
                    var shape=level.getBlockState(pos).getCollisionShape(level,pos);
                    double volume=shape.toAabbs().stream().mapToDouble(b->b.getXsize()*b.getYsize()*b.getZsize()).sum();
                    if(Math.abs(volume-(double)(i+1)/layers)>1e-8) throw new AssertionError("Stack volume: "+block+" "+volume);
                }
                if(player.getItemInHand(hand).getCount()!=8-(layers-1)) throw new AssertionError("Stack consumption");
            }
        }
        player.discard();
        System.out.println("PASS: BlockItem.useOn stacking, half/quarter, four directions, volume and consumption.");
    }
}
