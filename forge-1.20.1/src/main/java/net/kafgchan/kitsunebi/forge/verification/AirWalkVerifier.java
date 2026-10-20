package net.kafgchan.kitsunebi.forge.verification;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager;
import net.kafgchan.kitsunebi.item.tool.ToolStateHelper;
import net.kafgchan.kitsunebi.client.KitsunebiAirWalkClientState;
import net.kafgchan.kitsunebi.network.KitsunebiNetworkHandler;
import net.kafgchan.kitsunebi.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;

/** Real Player.travel, server eligibility and the shared owner simulation, isolated server only. */
public final class AirWalkVerifier {
    private static void check(boolean ok,String error){if(!ok)throw new AssertionError(error);}
    public static void verify(ServerLevel level) {
        var chord=new net.kafgchan.kitsunebi.client.AirWalkToggleInput();
        check(!chord.update(false,true,true)&&chord.update(true,true,true),"Sneak then jump chord failed");
        for(int i=0;i<30;i++)check(!chord.update(true,true,true),"Held chord toggled repeatedly");
        for(int i=0;i<10;i++) {
            check(!chord.update(false,true,true),"Partial release toggled");
            check(!chord.update(true,true,true),"Repeated jumps while sneaking toggled again");
        }
        check(!chord.update(true,false,true)&&!chord.update(true,true,true),"Releasing sneak alone rearmed the chord");
        chord.update(false,false,true);
        check(!chord.update(true,false,true)&&chord.update(true,true,true),"Reverse key order failed");
        chord.suspend(true,true);check(!chord.update(true,true,true),"GUI-held chord toggled");
        chord.update(false,false,true);check(!chord.update(true,true,false)&&!chord.update(true,true,true),"Enabling skill with held keys toggled");
        chord.update(false,false,true);check(chord.update(true,true,true),"Complete release did not rearm");
        level.setBlock(new BlockPos(4820,249,4820),Blocks.AIR.defaultBlockState(),3);
        var profile=new GameProfile(UUID.fromString("1c3362c9-85fe-4901-bc7f-24996914a443"),"AirWalkVerifier");
        var p=FakePlayerFactory.get(level,profile);p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(4800.5,250,4800.5);p.setOnGround(false);p.setShiftKeyDown(true);
        KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(!KitsunebiAirWalkManager.isActive(p),"Missing tabi accepted");
        var feet=new ItemStack(ModItems.KITSUNEBI_TABI.get());p.setItemSlot(EquipmentSlot.FEET,feet);
        p.setShiftKeyDown(false);KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(!KitsunebiAirWalkManager.isActive(p),"Normal jump toggled air walk");
        p.setShiftKeyDown(true);ToolStateHelper.setActive(feet,false);KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(!KitsunebiAirWalkManager.isActive(p),"Disabled skill accepted sneak jump");
        ToolStateHelper.setActive(feet,true);KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(KitsunebiAirWalkManager.isActive(p)&&p.isNoGravity(),"Sneak jump did not activate");
        KitsunebiAirWalkClientState.apply(p,true);
        KitsunebiAirWalkClientState.tick(p,true,true,true,false);
        check(p.getDeltaMovement().y==0,"Activation chord caused vertical motion");
        KitsunebiAirWalkClientState.tick(p,true,false,true,false);
        check(p.getDeltaMovement().y==0,"Activation sneak immediately descended and landed");
        p.setShiftKeyDown(false);p.zza=1;p.xxa=0;p.setYRot(0);
        double height=p.getY(),z=p.getZ();
        for(int i=0;i<80;i++) {
            KitsunebiAirWalkClientState.tick(p);p.travel(Vec3.ZERO);KitsunebiAirWalkManager.tickPlayer(p);
            check(Math.abs(p.getY()-height)<.001,"Altitude drift during travel");
        }
        check(p.getZ()-z>5,"Air walk has no horizontal movement");
        KitsunebiAirWalkClientState.tick(p,true,false,false,false);
        double walking=Math.hypot(p.getDeltaMovement().x,p.getDeltaMovement().z);
        KitsunebiAirWalkClientState.tick(p,true,false,false,true);
        double running=Math.hypot(p.getDeltaMovement().x,p.getDeltaMovement().z);
        check(p.isSprinting()&&Math.abs(running/walking-1.3)<.00001,"Air sprint must match vanilla's 30% increase, without double multiplication");
        p.setSprinting(false);KitsunebiAirWalkClientState.tick(p,true,false,false,false);
        check(Math.abs(Math.hypot(p.getDeltaMovement().x,p.getDeltaMovement().z)-walking)<.00001,"Walking speed did not return");
        p.zza=0;
        check(KitsunebiAirWalkClientState.verticalAxis(true,true)==0,"Sneak+jump must not ascend or descend while canceling");
        for(int i=0;i<20;i++) {
            KitsunebiAirWalkManager.setVerticalInput(p,1);
            KitsunebiAirWalkClientState.tick(p,true,true,false);p.travel(Vec3.ZERO);KitsunebiAirWalkManager.tickPlayer(p);
        }
        check(Math.abs(p.getY()-(height+20*KitsunebiAirWalkManager.VERTICAL_SPEED))<.001,"Jump must ascend without altitude correction");
        KitsunebiAirWalkManager.setVerticalInput(p,127);KitsunebiAirWalkManager.tickPlayer(p);
        check(Math.abs(p.getDeltaMovement().y-KitsunebiAirWalkManager.VERTICAL_SPEED)<.001,"Invalid vertical control changed speed");
        KitsunebiAirWalkManager.setVerticalInput(p,0);double raised=p.getY();
        for(int i=0;i<10;i++) {
            KitsunebiAirWalkClientState.tick(p,true,false,false);p.travel(Vec3.ZERO);KitsunebiAirWalkManager.tickPlayer(p);
        }
        check(Math.abs(p.getY()-raised)<.001,"Releasing jump must hover at the new altitude");
        KitsunebiAirWalkManager.setVerticalInput(p,-1);KitsunebiAirWalkManager.tickPlayer(p);
        check(p.getDeltaMovement().y==0,"Descent requires sneak on server");
        p.setShiftKeyDown(true);
        for(int i=0;i<12;i++) {
            KitsunebiAirWalkManager.setVerticalInput(p,-1);
            KitsunebiAirWalkClientState.tick(p,true,false,true);p.travel(Vec3.ZERO);KitsunebiAirWalkManager.tickPlayer(p);
        }
        check(Math.abs(p.getY()-height)<.001,"Sneak must descend");
        p.setShiftKeyDown(false);KitsunebiAirWalkManager.setVerticalInput(p,0);
        z=p.getZ();
        for(int i=0;i<20;i++) {
            KitsunebiAirWalkClientState.tick(p,false);p.travel(Vec3.ZERO);KitsunebiAirWalkManager.tickPlayer(p);
        }
        check(Math.abs(p.getZ()-z)<.001&&Math.abs(p.getY()-height)<.001,"GUI/idle must stop without falling");
        KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(KitsunebiAirWalkManager.isActive(p),"Unsneaked jump disabled active mode");
        p.setShiftKeyDown(true);KitsunebiAirWalkManager.onPlayerJumpInput(p);KitsunebiAirWalkClientState.apply(p,false);
        check(!p.isNoGravity()&&!KitsunebiAirWalkManager.isActive(p),"Toggle did not restore gravity");
        for(int i=0;i<8;i++)p.travel(Vec3.ZERO);
        check(p.getY()<height-.1,"Player did not fall after cancellation");
        ItemStack heldTool=new ItemStack(ModItems.KITSUNEBI_PICKAXE.get());
        p.setItemSlot(EquipmentSlot.MAINHAND,heldTool);
        boolean feetBefore=ToolStateHelper.isActive(feet),toolBefore=ToolStateHelper.isActive(heldTool);
        KitsunebiNetworkHandler.handleSkillAction(p,3);
        check(ToolStateHelper.isActive(feet)==feetBefore&&ToolStateHelper.isActive(heldTool)!=toolBefore,"Tool toggle affected feet");
        KitsunebiNetworkHandler.handleSkillAction(p,4);
        check(ToolStateHelper.isActive(feet)!=feetBefore&&ToolStateHelper.isActive(heldTool)!=toolBefore,"Feet toggle affected tool");
        KitsunebiNetworkHandler.handleSkillAction(p,4);
        p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        KitsunebiAirWalkManager.onPlayerJumpInput(p);KitsunebiNetworkHandler.handleSkillAction(p,4);
        check(!KitsunebiAirWalkManager.isActive(p)&&!p.isNoGravity()&&!ToolStateHelper.isActive(feet),"G must disable skill and active footing even with an empty hand");
        KitsunebiAirWalkManager.onPlayerJumpInput(p);check(!KitsunebiAirWalkManager.isActive(p),"Sneak gate ignored G off");
        KitsunebiNetworkHandler.handleSkillAction(p,4);KitsunebiAirWalkManager.onPlayerJumpInput(p);
        p.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);KitsunebiAirWalkManager.tickPlayer(p);
        check(!p.isNoGravity()&&!KitsunebiAirWalkManager.isActive(p),"Unequipping left gravity disabled");
        p.setItemSlot(EquipmentSlot.FEET,feet);p.setPos(4820.5,250,4820.5);KitsunebiAirWalkManager.onPlayerJumpInput(p);
        level.setBlock(new BlockPos(4820,249,4820),Blocks.STONE.defaultBlockState(),3);KitsunebiAirWalkManager.tickPlayer(p);
        check(!KitsunebiAirWalkManager.isActive(p)&&!p.isNoGravity(),"Landing on real terrain did not cancel");
        p.setPos(4830.5,250,4830.5);p.setNoGravity(true);KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(!KitsunebiAirWalkManager.isActive(p)&&p.isNoGravity(),"Overwrote another gravity owner");p.setNoGravity(false);
        KitsunebiAirWalkManager.onPlayerJumpInput(p);KitsunebiAirWalkManager.clearPlayer(p.getUUID());
        check(!p.isNoGravity(),"Logout cleanup left noGravity enabled");
        p.getAbilities().flying=true;KitsunebiAirWalkManager.onPlayerJumpInput(p);
        check(!KitsunebiAirWalkManager.isActive(p),"Creative flight conflict");p.getAbilities().flying=false;
        KitsunebiAirWalkManager.onPlayerJumpInput(p);p.setHealth(0);KitsunebiAirWalkManager.tickPlayer(p);
        check(!p.isNoGravity()&&!KitsunebiAirWalkManager.isActive(p),"Death cleanup failed");
        KitsunebiAirWalkClientState.tick(null);
        System.out.println("PASS: sneak gate, G on/off, horizontal travel, jump ascent, sneak descent, input validation, hover after release, GUI stop, cancellation/fall, unequip, landing, logout/death and flight conflicts.");
    }
    private AirWalkVerifier(){}
}
