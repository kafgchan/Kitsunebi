package net.kafgchan.kitsunebi.client;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager;

/** Owner-only motion after server approval; contains no client-only Minecraft classes. */
public final class KitsunebiAirWalkClientState {
    private static Player owner;
    private static int vertical,lastSent=99,lastTick;
    private static boolean activationHeld;
    public static void apply(Player player,boolean active) {
        if(owner!=null&&owner!=player)owner.setNoGravity(false);
        if(!active) {if(owner!=null)owner.setNoGravity(false);owner=null;vertical=0;lastSent=99;return;}
        lastSent=99;vertical=0;activationHeld=true;
        owner=player;player.setNoGravity(true);
        var motion=player.getDeltaMovement();player.setDeltaMovement(motion.x,0,motion.z);
    }
    public static void tick(Player player) {tick(player,true);}
    public static void tick(Player player,boolean allowInput) {tick(player,allowInput,false,false);}
    public static void tick(Player player,boolean allowInput,boolean jump,boolean sneak) {tick(player,allowInput,jump,sneak,player!=null&&player.isSprinting());}
    // The shared Fabric API exposes mayfly as a field; preserve mod-granted flight abilities.
    @SuppressWarnings({"deprecation", "unused"})
    public static void tick(Player player,boolean allowInput,boolean jump,boolean sneak,boolean sprintKey) {
        if(owner==null)return;
        if(owner!=player||!player.isAlive()||player.getAbilities().flying||player.isFallFlying()||player.isPassenger()||!KitsunebiAirWalkManager.skillEnabled(player)) {
            apply(owner,false);return;
        }
        player.setNoGravity(true);player.fallDistance=0;
        Vec3 input=allowInput?new Vec3(player.xxa,0,player.zza):Vec3.ZERO;
        if(input.lengthSqr()>1)input=input.normalize();
        // Vanilla's sprint attribute supplies the same 30% increase as on the ground.
        // Also allow starting sprint in midair, without depending on ground-only double taps.
        boolean sprint=allowInput&&(sprintKey||player.isSprinting())&&!player.isShiftKeyDown()
                &&player.zza>.8F&&!player.isUsingItem()&&!player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                &&(player.getFoodData().getFoodLevel()>6||player.getAbilities().mayfly);
        player.setSprinting(sprint);
        double speed=player.getAttributeValue(Attributes.MOVEMENT_SPEED)*2.15;
        Vec3 movement=input.yRot((float)Math.toRadians(-player.getYRot())).scale(speed);
        // Releasing jump while still holding the activation sneak must not immediately descend/land.
        if(activationHeld&&allowInput&&!jump&&!sneak)activationHeld=false;
        vertical=allowInput&&!activationHeld?verticalAxis(jump,sneak):0;
        player.setDeltaMovement(movement.x,KitsunebiAirWalkManager.verticalVelocity(vertical),movement.z);
    }
    public static int verticalAxis(boolean jump,boolean sneak){return jump==sneak?0:jump?1:-1;}
    /** Send changes immediately and refresh held controls periodically; absent input times out on server. */
    public static Integer pollVerticalInput(Player player) {
        if(owner==null||owner!=player)return null;
        if(vertical==lastSent&&player.tickCount-lastTick<10)return null;
        lastSent=vertical;lastTick=player.tickCount;return vertical;
    }
    private KitsunebiAirWalkClientState(){}
}
