package cc.thonly.reverie_dreams.entity.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Local sea-floor wandering: bounded loaded-block probes and at most two paths per attempt. */
final class AquaticBottomWanderGoal extends Goal {
    private final CuisineAquaticEntity animal;
    private Path nextPath;
    private int nextAttempt;

    AquaticBottomWanderGoal(CuisineAquaticEntity animal) {
        this.animal = animal;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override public boolean canUse() {
        if (this.animal.species().stationary() || !this.animal.isInWater() || this.animal.tickCount < this.nextAttempt) return false;
        this.nextAttempt = this.animal.tickCount + 80;
        this.nextPath = null;
        var world = this.animal.level();
        BlockPos origin = this.animal.blockPosition();
        // Vanilla's navigation region has an eight-block margin; require loaded chunks before it is built.
        if (!world.hasChunksAt(origin.offset(-16, 0, -16), origin.offset(16, 0, 16))) return false;
        List<BlockPos> candidates = new ArrayList<>(8);
        for (int candidate = 0; candidate < 8; candidate++) {
            BlockPos column = origin.offset(this.animal.getRandom().nextInt(9) - 4, 2, this.animal.getRandom().nextInt(9) - 4);
            for (int depth = 0; depth < 7; depth++) {
                BlockPos feet = column.below(depth);
                if (!world.hasChunkAt(feet) || world.isOutsideBuildHeight(feet)) break;
                if (!world.getFluidState(feet).is(FluidTags.WATER)) continue;
                if (!world.getBlockState(feet.below()).isFaceSturdy(world, feet.below(), Direction.UP)) continue;
                Vec3 position = Vec3.atBottomCenterOf(feet).add(0, .1, 0);
                if (world.noCollision(this.animal, this.animal.getBoundingBox().move(position.subtract(this.animal.position()))))
                    candidates.add(feet);
                break;
            }
        }
        for (int attempt = 0; attempt < Math.min(2, candidates.size()); attempt++) {
            Path path = this.animal.getNavigation().createPath(candidates.get(attempt), 0, 8);
            if (path != null && path.canReach()) { this.nextPath = path; return true; }
        }
        return false;
    }

    @Override public boolean canContinueToUse() { return this.animal.isInWater() && !this.animal.getNavigation().isDone(); }
    @Override public void start() { this.animal.getNavigation().moveTo(this.nextPath, .8); }
    @Override public void stop() { this.animal.getNavigation().stop(); this.nextPath = null; }
}
