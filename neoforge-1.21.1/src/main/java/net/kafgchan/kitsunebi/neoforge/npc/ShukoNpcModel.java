package net.kafgchan.kitsunebi.neoforge.npc;

import net.kafgchan.kitsunebi.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public final class ShukoNpcModel extends GeoModel<ShukoNpcEntity> {
    @Override public ResourceLocation getModelResource(ShukoNpcEntity entity) { return id("geo/npc/shuko.geo.json"); }
    @Override public ResourceLocation getTextureResource(ShukoNpcEntity entity) {
        return id("textures/entity/npc/shuko/" + entity.getExpressionName() + ".png");
    }
    @Override public ResourceLocation getAnimationResource(ShukoNpcEntity entity) { return id("animations/npc/shuko.animation.json"); }

    @Override public void setCustomAnimations(ShukoNpcEntity entity, long instanceId, AnimationState<ShukoNpcEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        var look = state.getData(DataTickets.ENTITY_MODEL_DATA);
        getBone("head").ifPresent(head -> {
            head.setRotX(head.getRotX() + Mth.clamp(look.headPitch(), -35, 35) * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + Mth.clamp(look.netHeadYaw(), -55, 55) * Mth.DEG_TO_RAD);
        });
        // Models are shared: restore every tail root on every entity render, including removals.
        for (int index = 1; index <= 9; index++) {
            final int tail = index;
            getBone(String.format(java.util.Locale.ROOT, "tail_%02d", index))
                    .ifPresent(bone -> {
                        boolean hidden = !isTailVisible(entity.getTailCount(), tail);
                        bone.setHidden(hidden);
                        bone.setChildrenHidden(hidden);
                    });
        }
    }

    public static boolean isTailVisible(int count, int index) {
        return switch (count) {
            case 9 -> true;
            case 6 -> index >= 2 && index <= 8 && index != 5;
            case 3 -> index >= 4 && index <= 6;
            case 1 -> index == 5;
            default -> false;
        };
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path); }
}
