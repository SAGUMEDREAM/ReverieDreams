package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.variant.HairballAnimationState;
import cc.thonly.reverie_dreams.util.entity.IAnimationHelper;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.util.GeckoLibUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

@Getter
public class Hairball extends PathfinderMob implements GeoEntity {
    public static final EntityDataAccessor<Boolean> BLACK_COLOR = SynchedEntityData.defineId(Hairball.class, EntityDataSerializers.BOOLEAN);
    public static final DataTicket<Boolean> BLACK_COLOR_TICKET = DataTicket.create("black_color", Boolean.class);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final byte ATTACK_ANIMATION_EVENT = 4;
    private final HairballAnimationState animationState = new HairballAnimationState();

    public Hairball(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.getEntityData().set(BLACK_COLOR, ReverieDreams.RD.nextBoolean());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 12.0f));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLACK_COLOR, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        HairballAnimationState.Clip[] previous = {null};
        int[] revision = {-1};
        controllers.add(new AnimationController<Hairball>("movement", 1, state -> {
            if (this.isDeadOrDying()) return PlayState.STOP;
            var next = this.animationState.clip();
            if (next != previous[0] || (!next.looping() && revision[0] != this.animationState.revision())) {
                state.controller().reset();
            }
            previous[0] = next;
            revision[0] = this.animationState.revision();
            state.controller().setAnimationSpeed(next == HairballAnimationState.Clip.WALK
                    ? HairballAnimationState.walkSpeed(this.getDeltaMovement().horizontalDistance()) : 1);
            var animation = RawAnimation.begin();
            return state.setAndContinue(next.looping() ? animation.thenLoop(next.animation())
                    : animation.thenPlayAndHold(next.animation()));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.animationState.update(this.tickCount, this.onGround(), this.isInWater() || this.isInLava(),
                this.getDeltaMovement().y, this.getDeltaMovement().horizontalDistanceSqr(),
                this.hurtTime, this.isDeadOrDying());
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.animationState.beginAttack(this.tickCount);
        level.broadcastEntityEvent(this, ATTACK_ANIMATION_EVENT);
        return super.doHurtTarget(level, target);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == ATTACK_ANIMATION_EVENT) {
            this.animationState.beginAttack(this.tickCount);
            return;
        }
        super.handleEntityEvent(event);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 2.5)
                .add(Attributes.FOLLOW_RANGE, 15);
    }

    public static boolean checkSpawnRules(
            EntityType<Hairball> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (!Mob.checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return true;
        }
        return level.getEntitiesOfClass(
                Hairball.class,
                new AABB(pos).inflate(8),
                Hairball::isAlive
        ).size() < 2;
    }

    public boolean isBlackColor() {
        return this.getEntityData().get(BLACK_COLOR);
    }

    public void setBlackColor(Boolean blackColor) {
        this.getEntityData().set(BLACK_COLOR, blackColor);
        IAnimationHelper.setSyncData(this, BLACK_COLOR_TICKET, blackColor);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBlackColor(input.getBooleanOr("BlackColor", input.getBooleanOr("Black", false)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("BlackColor", this.getEntityData().get(BLACK_COLOR));
    }
}
