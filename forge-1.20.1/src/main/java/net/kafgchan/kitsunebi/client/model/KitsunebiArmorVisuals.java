package net.kafgchan.kitsunebi.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.kafgchan.kitsunebi.item.armor.KitsunebiArmorHandler;

/** Shared pose transfer; called after the loader has applied vanilla armor visibility. */
public final class KitsunebiArmorVisuals {
    private static final java.util.Map<LivingEntity,TailMotion> MOTION=new java.util.WeakHashMap<>();
    public static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("kitsunebi","textures/entity/kitsunebi_shrine_palette.png");
    public static KitsunebiArmorVisualModel<LivingEntity> create() {
        return new KitsunebiArmorVisualModel<>(KitsunebiArmorVisualModel.createLayer().bakeRoot());
    }
    public static void prepare(KitsunebiArmorVisualModel<LivingEntity> model,HumanoidModel<?> original,LivingEntity entity,EquipmentSlot slot) {
        model.attackTime=original.attackTime;model.riding=original.riding;model.young=original.young;
        model.head.copyFrom(original.head);model.hat.copyFrom(original.hat);model.body.copyFrom(original.body);
        model.rightArm.copyFrom(original.rightArm);model.leftArm.copyFrom(original.leftArm);
        model.rightLeg.copyFrom(original.rightLeg);model.leftLeg.copyFrom(original.leftLeg);
        float partial=net.minecraft.client.Minecraft.getInstance().getFrameTime();
        float age=entity.tickCount+partial;
        model.configure(slot,KitsunebiArmorHandler.getKitsunebiArmorCount(entity),age);
        if(slot==EquipmentSlot.LEGS) {
            TailMotion motion=MOTION.computeIfAbsent(entity,ignored->new TailMotion());
            var velocity=entity.getDeltaMovement();
            double radians=Math.toRadians(entity.yBodyRot);
            float sideways=(float)(velocity.x*Math.cos(radians)+velocity.z*Math.sin(radians));
            float speed=(float)Math.sqrt(velocity.x*velocity.x+velocity.z*velocity.z);
            float turn=net.minecraft.util.Mth.wrapDegrees(entity.yBodyRot-entity.yBodyRotO);
            motion.update(age,Math.min(.32F,speed*.8F),net.minecraft.util.Mth.clamp(-turn*.012F+sideways*.3F,-.4F,.4F));
            for(int i=0;i<9;i++) {
                var tail=model.body.getChild("tail_"+i);
                tail.xRot+=motion.lift;
                tail.yRot+=motion.yaw;
                var mid=tail.getChild("tail_"+i+"_mid");
                mid.xRot=.04F+motion.lift*.15F+(float)Math.sin(age*.06F-i*.4F)*.025F;
                var end=mid.getChild("tail_"+i+"_end");
                end.yRot=motion.yaw*.2F+(float)Math.sin(age*.06F-i*.4F-.7F)*.025F;
            }
        }
    }
    /** Damped springs, per entity; rendering multiple equipment slots never advances them twice. */
    static final class TailMotion {
        float lift,yaw,liftVelocity,yawVelocity;
        private float lastAge=Float.NaN;
        void update(float age,float targetLift,float targetYaw) {
            if(Float.isNaN(lastAge)||age<lastAge||age-lastAge>20) {
                lastAge=age;lift=0;yaw=0;liftVelocity=0;yawVelocity=0;return;
            }
            float seconds=(age-lastAge)/20F;lastAge=age;
            while(seconds>0) {
                float dt=Math.min(seconds,1F/120);seconds-=dt;
                liftVelocity+=(64*(targetLift-lift)-16*liftVelocity)*dt;
                yawVelocity+=(49*(targetYaw-yaw)-14*yawVelocity)*dt;
                lift+=liftVelocity*dt;yaw+=yawVelocity*dt;
            }
        }
    }
    private KitsunebiArmorVisuals(){}
}
