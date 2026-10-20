package net.kafgchan.kitsunebi.item.armor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kafgchan.kitsunebi.config.KitsunebiConfig;
import net.kafgchan.kitsunebi.item.tool.ToolStateHelper;
import net.kafgchan.kitsunebi.network.KitsunebiAirWalkStatePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.AABB;

/** Server-authoritative sneak+jump toggle. No real blocks or creative flight are granted. */
public final class KitsunebiAirWalkManager {
    public static final double VERTICAL_SPEED=.18;
    public static final double DESCENT_SPEED=.30;
    public static double verticalVelocity(int axis){return axis<0?-DESCENT_SPEED:axis>0?VERTICAL_SPEED:0;}
    private static final Map<UUID,Session> ACTIVE=new HashMap<>();
    private static final class Session {
        final ServerPlayer owner;
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        double height;
        int vertical;
        long lastInput;
        boolean leftSupport;
        Session(ServerPlayer player) {
            owner=player;dimension=player.level().dimension();height=player.getY();
            lastInput=player.level().getGameTime();
            leftSupport=!supported(player);
        }
    }
    public static boolean isActive(ServerPlayer player){return ACTIVE.containsKey(player.getUUID());}
    public static void setVerticalInput(ServerPlayer player,int vertical) {
        Session session=ACTIVE.get(player.getUUID());
        if(session==null||!eligible(player)||vertical< -1||vertical>1)return;
        // Both buttons are reserved for cancellation, never simultaneous ascent/descent.
        if(vertical<0&&!player.isShiftKeyDown()||vertical>0&&player.isShiftKeyDown())return;
        session.height=player.getY();session.vertical=vertical;
        session.lastInput=player.level().getGameTime();
    }
    public static boolean skillEnabled(net.minecraft.world.entity.player.Player player) {
        return KitsunebiArmorHandler.hasTabi(player)&&ToolStateHelper.isActive(player.getItemBySlot(EquipmentSlot.FEET));
    }
    private static boolean eligible(ServerPlayer player) {
        return player.isAlive()&&!player.isSpectator()&&!player.getAbilities().flying&&!player.isFallFlying()
                &&!player.isPassenger()&&!player.isInLava()&&skillEnabled(player);
    }
    public static void onPlayerJumpInput(ServerPlayer player) {
        if(!player.isShiftKeyDown()||!eligible(player))return;
        if(isActive(player)){clearPlayer(player.getUUID());return;}
        // Another flight mechanic already owns gravity; never overwrite it.
        if(player.isNoGravity())return;
        ACTIVE.put(player.getUUID(),new Session(player));
        player.setNoGravity(true);
        var motion=player.getDeltaMovement();player.setDeltaMovement(motion.x,0,motion.z);
        player.fallDistance=0;player.hasImpulse=true;
        net.kafgchan.kitsunebi.network.KitsunebiNetwork.sendToPlayer(player, new KitsunebiAirWalkStatePacket(true));
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.displayClientMessage(Component.translatable("message.kitsunebi.air_walk_on"),true);
    }
    public static void tickPlayer(ServerPlayer player) {
        Session session=ACTIVE.get(player.getUUID());if(session==null)return;
        if(session.owner!=player||session.dimension!=player.level().dimension()||!eligible(player)) {
            clearPlayer(player.getUUID());return;
        }
        boolean support=supported(player);
        if(session.leftSupport&&support){clearPlayer(player.getUUID());return;}
        if(!support)session.leftSupport=true;
        if(player.level().getGameTime()-session.lastInput>20) {
            session.vertical=0;session.height=player.getY();
        }
        player.setNoGravity(true);
        var motion=player.getDeltaMovement();player.setDeltaMovement(motion.x,verticalVelocity(session.vertical),motion.z);
        player.fallDistance=0;
        if(session.vertical!=0)session.height=player.getY();
        // The owner simulates the same zero-gravity movement; correct only genuine height drift.
        if(Math.abs(player.getY()-session.height)>.125)player.connection.teleport(player.getX(),session.height,player.getZ(),player.getYRot(),player.getXRot());
        if(KitsunebiConfig.get().enableParticles&&player.tickCount%3==0) {
            player.serverLevel().sendParticles(ParticleTypes.END_ROD,player.getX(),player.getY(),player.getZ(),1,.2,0,.2,.01);
            player.serverLevel().sendParticles(ParticleTypes.FLAME,player.getX(),player.getY(),player.getZ(),1,.2,0,.2,.01);
        }
    }
    private static boolean supported(ServerPlayer player) {
        var box=player.getBoundingBox();
        return !player.level().noCollision(player,new AABB(box.minX+.001,box.minY-.06,box.minZ+.001,box.maxX-.001,box.minY,box.maxZ-.001));
    }
    public static void clearPlayer(UUID uuid) {
        Session session=ACTIVE.remove(uuid);if(session==null)return;
        ServerPlayer player=session.owner;
        player.setNoGravity(false);player.fallDistance=0;
        var motion=player.getDeltaMovement();player.setDeltaMovement(motion.x,0,motion.z);
        net.kafgchan.kitsunebi.network.KitsunebiNetwork.sendToPlayer(player, new KitsunebiAirWalkStatePacket(false));
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.displayClientMessage(Component.translatable("message.kitsunebi.air_walk_off"),true);
    }
    private KitsunebiAirWalkManager(){}
}
