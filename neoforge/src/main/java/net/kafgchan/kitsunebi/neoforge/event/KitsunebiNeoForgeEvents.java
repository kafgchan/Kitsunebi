package net.kafgchan.kitsunebi.neoforge.event;

import net.kafgchan.kitsunebi.Constants;
import net.kafgchan.kitsunebi.item.armor.KitsunebiArmorHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

/**
 * NeoForge 側イベントリスナー
 * 狐火防具のダメージ軽減・無効化・ノックバック耐性・反撃の連携
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public class KitsunebiNeoForgeEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        float original = event.getOriginalDamage();
        float processed = KitsunebiArmorHandler.processDamage(event.getEntity(), event.getSource(), original);
        event.setNewDamage(processed);
    }

    @SubscribeEvent
    public static void onLivingKnockback(LivingKnockBackEvent event) {
        if (KitsunebiArmorHandler.shouldCancelKnockback(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.kafgchan.kitsunebi.item.armor.KitsunebiAirWalkManager.tickPlayer(serverPlayer);
        }
    }
}
