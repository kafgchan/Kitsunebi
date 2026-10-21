package net.kafgchan.kitsunebi.neoforge.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Shuko's appearance is independent of armor ownership and equipped slot counts. */
public final class ShukoNpcEntity extends PathfinderMob implements GeoEntity {
    public static final String[] EXPRESSIONS = {"neutral", "smile", "happy", "serious", "surprised", "worried"};
    private static final EntityDataAccessor<Integer> TAIL_COUNT = SynchedEntityData.defineId(ShukoNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPRESSION = SynchedEntityData.defineId(ShukoNpcEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.shuko.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.shuko.walk");
    private static final RawAnimation TALK = RawAnimation.begin().thenPlay("animation.shuko.talk");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShukoNpcEntity(EntityType<? extends ShukoNpcEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.FOLLOW_RANGE, 12);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        var wander = new WaterAvoidingRandomStrollGoal(this, 0.65);
        wander.setInterval(100);
        goalSelector.addGoal(5, wander);
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 7));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAIL_COUNT, 9);
        builder.define(EXPRESSION, 0);
    }

    public int getTailCount() { return entityData.get(TAIL_COUNT); }
    public void setTailCount(int count) {
        int clamped = Mth.clamp(count, 0, 9);
        entityData.set(TAIL_COUNT, clamped >= 9 ? 9 : clamped >= 6 ? 6 : clamped >= 3 ? 3 : clamped >= 1 ? 1 : 0);
    }
    public int getExpression() { return entityData.get(EXPRESSION); }
    public void setExpression(int expression) { entityData.set(EXPRESSION, Mth.clamp(expression, 0, EXPRESSIONS.length - 1)); }
    public String getExpressionName() { return EXPRESSIONS[getExpression()]; }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TailCount", getTailCount());
        tag.putInt("Expression", getExpression());
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTailCount(tag.contains("TailCount") ? tag.getInt("TailCount") : 9);
        setExpression(tag.getInt("Expression"));
    }

    public void playTalkAnimation() { if (!level().isClientSide) triggerAnim("movement", "talk"); }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            getNavigation().stop();
            getLookControl().setLookAt(player, 30, 30);
            playTalkAnimation();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)).triggerableAnim("talk", TALK));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
