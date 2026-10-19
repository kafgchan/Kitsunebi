package net.kafgchan.kitsunebi.fabric.mixin;

import net.kafgchan.kitsunebi.item.armor.KitsunebiArmorHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric における LivingEntity の被ダメージ軽減・無効化およびノックバック抑止 Mixin
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float kitsunebi$modifyDamage(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        return KitsunebiArmorHandler.processDamage(self, source, amount);
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void kitsunebi$preventKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (KitsunebiArmorHandler.shouldCancelKnockback(self)) {
            ci.cancel();
        }
    }
}
