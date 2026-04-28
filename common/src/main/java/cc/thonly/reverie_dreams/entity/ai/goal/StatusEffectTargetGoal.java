package cc.thonly.reverie_dreams.entity.ai.goal;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class StatusEffectTargetGoal<T extends LivingEntity> extends TargetGoal {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
    protected final Class<T> targetClass;
    protected final int reciprocalChance;
    @Nullable
    protected LivingEntity targetEntity;
    protected TargetingConditions targetPredicate;

    @Nullable
    private final Holder<MobEffect> requiredEffect;

    public StatusEffectTargetGoal(Mob mob, Class<T> targetClass, boolean checkVisibility, @Nullable Holder<MobEffect> requiredEffect) {
        this(mob, targetClass, DEFAULT_RECIPROCAL_CHANCE, checkVisibility, false, null, requiredEffect);
    }

    public StatusEffectTargetGoal(Mob mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable TargetingConditions.Selector targetPredicate, @Nullable Holder<MobEffect> requiredEffect) {
        super(mob, checkVisibility, checkCanNavigate);
        this.targetClass = targetClass;
        this.reciprocalChance = NearestAttackableTargetGoal.reducedTickDelay(reciprocalChance);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.requiredEffect = requiredEffect;

        this.targetPredicate = TargetingConditions.forCombat()
                .range(this.getFollowDistance())
                .selector(targetPredicate);
    }

    @Override
    public boolean canUse() {
        if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
        }
        this.findClosestTarget();
        return this.targetEntity != null;
    }

    protected AABB getSearchBox(double distance) {
        return this.mob.getBoundingBox().inflate(distance, distance, distance);
    }

    protected void findClosestTarget() {
        ServerLevel serverWorld = NearestAttackableTargetGoal.getServerLevel(this.mob);

        if (this.targetClass == Player.class || this.targetClass == ServerPlayer.class) {
            this.targetEntity = serverWorld.getNearestPlayer(
                    this.getAndUpdateTargetPredicate().selector(
                            (entity, world) -> requiredEffect == null || entity.hasEffect(requiredEffect)
                    ),
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
            );
        } else {
            this.targetEntity = serverWorld.getNearestEntity(
                    this.mob.level().getEntitiesOfClass(
                            this.targetClass,
                            this.getSearchBox(this.getFollowDistance()),
                            entity -> {
                                if (entity.getClass() == this.mob.getClass()) return false;
                                return requiredEffect == null || entity.hasEffect(requiredEffect);
                            }
                    ),
                    this.getAndUpdateTargetPredicate(),
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
            );
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetEntity);
        super.start();
    }

    public void setTargetEntity(@Nullable LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    private TargetingConditions getAndUpdateTargetPredicate() {
        return this.targetPredicate.range(this.getFollowDistance());
    }
}
