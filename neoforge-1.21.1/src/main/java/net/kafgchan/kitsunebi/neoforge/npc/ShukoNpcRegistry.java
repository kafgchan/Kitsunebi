package net.kafgchan.kitsunebi.neoforge.npc;

import net.kafgchan.kitsunebi.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Standalone NPC registration: no spawn placement, equipment, recipes or trading. */
public final class ShukoNpcRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<ShukoNpcEntity>> SHUKO = ENTITIES.register("shuko_npc",
            () -> EntityType.Builder.of(ShukoNpcEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.6F).eyeHeight(1.35F).clientTrackingRange(10).build("kitsunebi:shuko_npc"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ShukoNpcRegistry::attributes);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(SHUKO.get(), ShukoNpcEntity.createAttributes().build());
    }

    private ShukoNpcRegistry() {}
}
