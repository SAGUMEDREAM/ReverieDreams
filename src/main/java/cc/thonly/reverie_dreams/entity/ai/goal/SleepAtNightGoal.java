package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepAtNightGoal extends Goal {
    private final NPCEntityImpl entity;
    private final double speed;
    private BlockPos bedPos;

    public SleepAtNightGoal(NPCEntityImpl entity, double speed) {
        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        if (!entity.getWorld().isNight() || entity.isSleeping()) {
            return false;
        }

        this.bedPos = findNearbyBed();
        return this.bedPos != null;
    }

    @Override
    public void start() {
        if (this.bedPos != null) {
            this.entity.getNavigation().startMovingTo(
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

        double distanceSq = this.entity.squaredDistanceTo(Vec3d.ofCenter(this.bedPos));
//        System.out.println("Distance to bed: " + Math.sqrt(distanceSq));
//        System.out.println("Current Pos: " + this.entity.getBlockPos() + ", Bed Pos: " + bedPos);
        if (distanceSq <= 2.25) {
//            System.out.println("Sleeping...");
            this.entity.sleep(this.bedPos);
            this.entity.getNavigation().stop();
            this.bedPos = null;
        }
    }

    @Override
    public boolean shouldContinue() {
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
        BlockPos entityPos = this.entity.getBlockPos();
        World world = this.entity.getWorld();
        List<BlockPos> list = new ArrayList<>();
        for (int i = -10; i < 10; i++) {
            for (int j = -5; j < 5; j++) {
                for (int k = -10; k < 10; k++) {
                    list.add(entityPos.add(i, j, k));
                }
            }
        }
        Map<Double, BlockPos> hashMap = new HashMap<>();
        for (BlockPos pos : list) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof BedBlock) {
                boolean a = blockState.get(BedBlock.OCCUPIED);
                boolean b = blockState.get(BedBlock.PART) == BedPart.HEAD;
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
