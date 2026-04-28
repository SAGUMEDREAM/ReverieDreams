package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepAtNightGoal extends Goal {
    private final BaseNPCLikeEntity entity;
    private final double speed;
    private BlockPos bedPos;

    public SleepAtNightGoal(BaseNPCLikeEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        if (!entity.level().isDarkOutside() || entity.isSleeping()) {
            return false;
        }

        this.bedPos = findNearbyBed();
        return this.bedPos != null;
    }

    @Override
    public void start() {
        if (this.bedPos != null) {
            this.entity.getNavigation().moveTo(
                    bedPos.getX() + 0.5,
                    bedPos.getY(),
                    bedPos.getZ() + 0.5,
                    this.speed
            );

        }
    }

    @Override
    public void tick() {
        if (this.bedPos == null) {
            return;
        }

        int bedWakeCd = this.entity.getBedWakeCd();
        if (bedWakeCd > 0) {
            this.entity.setBedWakeCd(bedWakeCd - 1);
            this.bedPos = null;
            return;
        }

        double distanceSq = this.entity.distanceToSqr(Vec3.atCenterOf(this.bedPos));
//        System.out.println("Distance to bed: " + Math.sqrt(distanceSq));
//        System.out.println("Current Pos: " + this.entity.getBlockPos() + ", Bed Pos: " + bedPos);
        if (distanceSq <= 2.25) {
//            System.out.println("Sleeping...");
            this.entity.startSleeping(this.bedPos);
            this.entity.getNavigation().stop();
            this.bedPos = null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.bedPos != null && !entity.isSleeping();
    }

    @Override
    public void stop() {
        this.bedPos = null;
    }

    private double dist(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private BlockPos findNearbyBed() {
        BlockPos entityPos = this.entity.blockPosition();
        Level world = this.entity.level();
        List<BlockPos> list = new ArrayList<>();
        for (int i = -10; i < 10; i++) {
            for (int j = -5; j < 5; j++) {
                for (int k = -10; k < 10; k++) {
                    list.add(entityPos.offset(i, j, k));
                }
            }
        }
        Map<Double, BlockPos> hashMap = new HashMap<>();
        for (BlockPos pos : list) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof BedBlock) {
                boolean a = blockState.getValue(BedBlock.OCCUPIED);
                boolean b = blockState.getValue(BedBlock.PART) == BedPart.HEAD;
                if (!a && b) {
                    hashMap.put(dist(pos, entityPos), pos);
                }
            }
        }
        if (hashMap.isEmpty()) {
            return null;
        }
        double idx = hashMap.keySet().stream().min(Double::compareTo).get();
        return hashMap.get(idx);
    }
}
