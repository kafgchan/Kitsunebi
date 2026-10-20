package net.kafgchan.kitsunebi.forge.verification;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.kafgchan.kitsunebi.item.tool.*;
import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;

/** Actual harvest callbacks and recipe manager; isolated test server only. */
public final class EquipmentFeaturesVerifier {
    public static void verify(ServerLevel level) {
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("196803f1-5dd5-4248-a452-96f6c6461b64"),"EquipmentVerifier"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        BlockPos tree=new BlockPos(4500,250,4500);
        player.setPos(4500.5,252,4500.5);
        for(int y=0;y<5;y++)level.setBlock(tree.above(y),Blocks.OAK_LOG.defaultBlockState(),3);
        for(int x=-1;x<=1;x++)level.setBlock(tree.above(5).offset(x,0,0),Blocks.OAK_LEAVES.defaultBlockState(),3);
        ItemStack axe=new ItemStack(ModItems.KITSUNEBI_AXE.get());
        player.setItemSlot(EquipmentSlot.MAINHAND,axe);
        if(ToolStateHelper.getMode(axe)!=1)throw new AssertionError("Axe must default to tree felling");
        player.gameMode.destroyBlock(tree);
        for(int y=0;y<5;y++)if(!level.isEmptyBlock(tree.above(y)))throw new AssertionError("Tree felling lost destroyed origin");
        if(axe.getDamageValue()!=5)throw new AssertionError("Felling durability: "+axe.getDamageValue());
        BlockPos vein=tree.offset(16,0,0);
        for(int x=0;x<8;x++)level.setBlock(vein.offset(x,0,0),Blocks.IRON_ORE.defaultBlockState(),3);
        ItemStack pick=new ItemStack(ModItems.KITSUNEBI_PICKAXE.get());ToolStateHelper.setMode(pick,3);
        player.setItemSlot(EquipmentSlot.MAINHAND,pick);player.gameMode.destroyBlock(vein);
        for(int x=0;x<8;x++)if(!level.isEmptyBlock(vein.offset(x,0,0)))throw new AssertionError("Vein lost destroyed origin");
        if(pick.getDamageValue()!=8)throw new AssertionError("Vein durability");
        // Each axis must excavate forward, including vertical views, and leave blocks behind intact.
        for(Direction direction:Direction.values()) {
            player.setYRot(direction==Direction.EAST?-90:direction==Direction.WEST?90:direction==Direction.NORTH?180:0);
            player.setXRot(direction==Direction.UP?-90:direction==Direction.DOWN?90:0);
            if(MiningHelper.tunnelDirection(player)!=direction)throw new AssertionError("Tunnel direction: "+direction);
            BlockPos start=tree.offset(80+direction.ordinal()*12,0,0);
            for(int distance=-1;distance<3;distance++)level.setBlock(start.relative(direction,distance),Blocks.STONE.defaultBlockState(),3);
            ItemStack tunnelPick=new ItemStack(ModItems.KITSUNEBI_PICKAXE.get());
            ToolStateHelper.setActive(tunnelPick,false);
            player.setItemSlot(EquipmentSlot.MAINHAND,tunnelPick);
            MiningHelper.breakTunnel(level,player,start,tunnelPick,3);
            for(int distance=0;distance<3;distance++)if(!level.isEmptyBlock(start.relative(direction,distance)))throw new AssertionError("Tunnel did not advance: "+direction);
            if(!level.getBlockState(start.relative(direction,-1)).is(Blocks.STONE))throw new AssertionError("Tunnel dug backwards");
        }
        BlockPos farm=tree.offset(40,0,0);
        player.setPos(farm.getX()+.5,252,farm.getZ()+.5);player.setXRot(90);player.setYRot(0);
        // pick(..., 0, ...) uses the previous render pose; fake players do not tick it forward.
        player.xo=player.getX();player.yo=player.getY();player.zo=player.getZ();player.xRotO=90;player.yRotO=0;
        ItemStack hoe=new ItemStack(ModItems.KITSUNEBI_HOE.get());ToolStateHelper.setMode(hoe,2);
        player.setItemSlot(EquipmentSlot.MAINHAND,hoe);
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)level.setBlock(farm.offset(x,0,z),Blocks.DIRT.defaultBlockState(),3);
        var hit=new BlockHitResult(Vec3.atCenterOf(farm),Direction.UP,farm,false);
        ((KitsunebiHoeItem)hoe.getItem()).useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
        int tilled=0;
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(level.getBlockState(farm.offset(x,0,z)).is(Blocks.FARMLAND))tilled++;
        if(tilled!=25||hoe.getDamageValue()!=25)throw new AssertionError("5x5 till failed: "+tilled+" / "+hoe.getDamageValue());
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++) {
            level.setBlock(farm.offset(x,0,z),Blocks.FARMLAND.defaultBlockState(),3);
            level.setBlock(farm.offset(x,1,z),Blocks.WHEAT.defaultBlockState(),3);
        }
        ToolStateHelper.setMode(hoe,4);
        ((KitsunebiHoeItem)hoe.getItem()).executeSkill(hoe,player);
        int grown=0;
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(level.getBlockState(farm.offset(x,1,z)).getValue(CropBlock.AGE)>0)grown++;
        if(grown!=81)throw new AssertionError("Growth should affect 81 crops: "+grown);
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)level.setBlock(farm.offset(x,1,z),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),3);
        player.getInventory().setItem(1,new ItemStack(Items.WHEAT_SEEDS,64));
        player.getInventory().setItem(2,new ItemStack(Items.WHEAT_SEEDS,17));
        ToolStateHelper.setMode(hoe,3);
        ((KitsunebiHoeItem)hoe.getItem()).useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(farm.above()),Direction.UP,farm.above(),false)));
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(!level.getBlockState(farm.offset(x,1,z)).equals(Blocks.WHEAT.defaultBlockState()))throw new AssertionError("Harvest/replant failed");
        if(hoe.getDamageValue()!=25+81+81)throw new AssertionError("Farm durability: "+hoe.getDamageValue());
        for(String source:ModBlocks.COLORS) {
            var input=new SimpleContainer(15);var damaged=new ItemStack(ModItems.KITSUNEBI_SWORD.get());damaged.setDamageValue(4096);
            input.setItem(0,damaged);input.setItem(1,new ItemStack(ModItems.getItem(source).get()));
            var repair=AltarCraftingEngine.calculate(input,AltarCraftingEngine.Category.EQUIPMENT,9,1);
            if(!repair.canCraft()||repair.result().getDamageValue()!=0)throw new AssertionError("Repair rejected color: "+source);
            for(String target:ModBlocks.COLORS) {
                var crafting=new TransientCraftingContainer(new net.minecraft.world.inventory.AbstractContainerMenu(null,0){public boolean stillValid(net.minecraft.world.entity.player.Player p){return true;}public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player p,int i){return ItemStack.EMPTY;}},2,1);crafting.setItem(0,new ItemStack(ModItems.getItem(source).get()));crafting.setItem(1,new ItemStack(AltarCraftingEngine.getDyeForColor(target)));
                var recipe=level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,crafting,level).orElseThrow();
                if(!recipe.assemble(crafting,level.registryAccess()).is(ModItems.getItem(target).get()))throw new AssertionError("Wrong recolor: "+source+" -> "+target);
            }
        }
        System.out.println("PASS: default axe fells 5 logs, vein harvest 8 ores, hoe till25/grow81/harvest81/replant81, exact durability, all 7 repair colors and 49 shapeless recolor combinations.");
    }
    private EquipmentFeaturesVerifier(){}
}
