package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class NPCFollowOwnerGoal extends Goal {
    private final TamableAnimal tameable;
    @Nullable
    private LivingEntity owner;
    private final double speed;
    private final PathNavigation navigation;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;

    public NPCFollowOwnerGoal(TamableAnimal tameable, double speed, float minDistance, float maxDistance) {
        this.tameable = tameable;
        this.speed = speed;
        this.navigation = tameable.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(tameable.getNavigation() instanceof GroundPathNavigation) && !(tameable.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity == null) {
            return false;
        }
        if (this.tameable.isOrderedToSit()) {
            return false;
        }
        if (this.tameable.getLastHurtMob() != null) {
            if (this.tameable.getLastHurtMob().isAlive()) {
                return false;
            }
            return false;
        }
        if (this.tameable.getTarget() != null) {
            if (this.tameable.getTarget().isAlive()) {
                return false;
            }
            return false;
        }
        if (this.tameable.unableToMoveToOwner()) {
            return false;
        }
        if (this.tameable.distanceToSqr(livingEntity) < (double) (this.minDistance * this.minDistance)) {
            return false;
        }
        if (this.tameable instanceof BaseNPCLikeEntity impl) {
            NPCState state = impl.getNpcState();
            if (state == NPCStates.NO_WALK || state == NPCStates.SEATED || state == NPCStates.WORKING) {
                return false;
            }
            if (state == NPCStates.FOLLOW) {
                return true;
            }
        }
        this.owner = livingEntity;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        }
        if (this.tameable.unableToMoveToOwner()) {
            return false;
        }
        return !(this.tameable.distanceToSqr(this.owner) <= (double) (this.maxDistance * this.maxDistance));
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.tameable.getPathfindingMalus(PathType.WATER);
        this.tameable.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tameable.setPathfindingMalus(PathType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick() {
        if (this.owner == null && this.tameable != null) {
            this.owner = this.tameable.getOwner();
        }
        if (this.tameable == null || this.owner == null) {
            return;
        }
        boolean bl = this.tameable.shouldTryTeleportToOwner();
        if (!bl) {
            this.tameable.getLookControl().setLookAt(this.owner, 10.0f, this.tameable.getMaxHeadXRot());
        }
        if (--this.updateCountdownTicks > 0) {
            return;
        }
        this.updateCountdownTicks = this.adjustedTickDelay(10);
        if (bl) {
            if (!this.tameable.isOrderedToSit()) {
                this.tameable.tryToTeleportToOwner();
            }
        } else {
            if (this.tameable.getLastHurtMob() == null) {
                this.navigation.moveTo(this.owner, this.speed);
            }
        }
    }
}

