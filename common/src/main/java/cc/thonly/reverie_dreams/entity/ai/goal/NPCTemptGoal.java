package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class NPCTemptGoal extends Goal {
    private static final TargetingConditions TEMPTING_ENTITY_PREDICATE = TargetingConditions.forNonCombat().ignoreLineOfSight();
    private final TargetingConditions predicate;
    protected final BaseNPCLikeEntity mob;
    private final double speed;
    private double lastPlayerX;
    private double lastPlayerY;
    private double lastPlayerZ;
    private double lastPlayerPitch;
    private double lastPlayerYaw;
    @Nullable
    protected Player closestPlayer;
    private int cooldown;
    private boolean active;
    private final Predicate<ItemStack> foodPredicate;
    private final boolean canBeScared;

    public NPCTemptGoal(BaseNPCLikeEntity mob, double speed, Predicate<ItemStack> foodPredicate, boolean canBeScared) {
        this.mob = mob;
        this.speed = speed;
        this.foodPredicate = foodPredicate;
        this.canBeScared = canBeScared;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.predicate = TEMPTING_ENTITY_PREDICATE.copy().selector((entityx, world) -> this.isTemptedBy(entityx));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        this.closestPlayer = NPCTemptGoal.getServerLevel(this.mob).getNearestPlayer(this.predicate.range(this.mob.getAttributeValue(Attributes.TEMPT_RANGE)), this.mob);
        TamableAnimal entitySelf = this.mob;
        if (!entitySelf.isTame()) {
            return this.closestPlayer != null;
        } else if (!(entitySelf.getOwner() == this.closestPlayer)) {
            return false;
        }
        return this.closestPlayer != null;
    }

    private boolean isTemptedBy(LivingEntity entity) {
        return this.foodPredicate.test(entity.getMainHandItem()) || this.foodPredicate.test(entity.getOffhandItem());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.closestPlayer == null) {
            return false;
        }
        if (this.canBeScared()) {
            if (this.mob.distanceToSqr(this.closestPlayer) < 36.0) {
                if (this.closestPlayer.distanceToSqr(this.lastPlayerX, this.lastPlayerY, this.lastPlayerZ) > 0.010000000000000002) {
                    return false;
                }
                if (Math.abs((double) this.closestPlayer.getXRot() - this.lastPlayerPitch) > 5.0 || Math.abs((double) this.closestPlayer.getYRot() - this.lastPlayerYaw) > 5.0) {
                    return false;
                }
            } else {
                this.lastPlayerX = this.closestPlayer.getX();
                this.lastPlayerY = this.closestPlayer.getY();
                this.lastPlayerZ = this.closestPlayer.getZ();
            }
            this.lastPlayerPitch = this.closestPlayer.getXRot();
            this.lastPlayerYaw = this.closestPlayer.getYRot();
        }
        return this.canUse();
    }

    protected boolean canBeScared() {
        return this.canBeScared;
    }

    @Override
    public void start() {
        if (this.closestPlayer != null) {
            this.lastPlayerX = this.closestPlayer.getX();
            this.lastPlayerY = this.closestPlayer.getY();
            this.lastPlayerZ = this.closestPlayer.getZ();
        }
        this.active = true;
    }

    @Override
    public void stop() {
        this.closestPlayer = null;
        this.mob.getNavigation().stop();
        this.cooldown = TemptGoal.reducedTickDelay(100);
        this.active = false;
    }

    @Override
    public void tick() {
        if (this.closestPlayer != null) {
            this.mob.getLookControl().setLookAt(this.closestPlayer, this.mob.getMaxHeadYRot() + 20, this.mob.getMaxHeadXRot());
            if (this.mob.distanceToSqr(this.closestPlayer) < 6.25) {
                this.mob.getNavigation().stop();
            } else {
                this.mob.getNavigation().moveTo(this.closestPlayer, this.speed);
            }
        }
    }

}

