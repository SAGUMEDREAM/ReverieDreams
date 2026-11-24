package cc.thonly.reverie_dreams.world.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class SettlementBed {
    enum Status {
        AVAILABLE,
        OCCUPIED,
        INVALID,
        UNLOADED
    }

    private record BedParts(BlockPos head, BlockPos foot, Direction facing, boolean occupied) {
    }

    private record Inspection(Status status, @Nullable BedParts parts) {
    }

    private SettlementBed() {
    }

    static Status status(Mob mob, ServerLevel level, BlockPos head) {
        if (mob.level() != level) {
            return Status.INVALID;
        }

        Inspection inspection = inspect(level, head);
        if (inspection.status() != Status.AVAILABLE) {
            return inspection.status();
        }

        BedParts parts = inspection.parts();
        if (isSleepingAt(mob, parts.head())) {
            return Status.AVAILABLE;
        }
        if (parts.occupied()) {
            return Status.OCCUPIED;
        }
        return hasOtherSleeper(level, mob, parts) ? Status.OCCUPIED : Status.AVAILABLE;
    }

    static boolean trySleep(Mob mob, ServerLevel level, BlockPos head) {
        if (!isServerThread(level) || mob.level() != level || !mob.isAlive()
                || status(mob, level, head) != Status.AVAILABLE) {
            return false;
        }
        if (isSleepingAt(mob, head)) {
            return true;
        }
        if (!sleepingNeighborsLoaded(level, head)) {
            return false;
        }

        mob.startSleeping(head.immutable());
        return true;
    }

    static void wake(Mob mob, ServerLevel level) {
        if (!isServerThread(level) || mob.level() != level || !mob.isSleeping()) {
            return;
        }

        BlockPos sleepingPos = mob.getSleepingPos().orElse(null);
        if (sleepingPos == null) {
            return;
        }

        if (vanillaWakeAreaLoaded(level, sleepingPos, mob)) {
            mob.stopSleeping();
            return;
        }

        clearHeadOccupiedIfLoaded(level, mob, sleepingPos);
        wakeWithoutWorldSearch(mob);
    }

    static List<BlockPos> approaches(ServerLevel level, BlockPos head) {
        Inspection inspection = inspect(level, head);
        if (inspection.status() != Status.AVAILABLE) {
            return List.of();
        }

        BedParts parts = inspection.parts();
        Direction side = parts.facing().getClockWise();
        List<BlockPos> candidates = List.of(
                parts.foot().relative(parts.facing().getOpposite()),
                parts.foot().relative(side),
                parts.head().relative(side.getOpposite()),
                parts.head().relative(parts.facing())
        );
        List<BlockPos> loaded = new ArrayList<>(candidates.size());
        for (BlockPos candidate : candidates) {
            if (loadedChunk(level, candidate) != null) {
                loaded.add(candidate.immutable());
            }
        }
        return List.copyOf(loaded);
    }

    private static Inspection inspect(ServerLevel level, BlockPos head) {
        BlockPos immutableHead = head.immutable();
        LevelChunk headChunk = loadedChunk(level, immutableHead);
        if (headChunk == null) {
            return new Inspection(Status.UNLOADED, null);
        }

        BlockState headState = headChunk.getBlockState(immutableHead);
        if (!(headState.getBlock() instanceof BedBlock)
                || headState.getValue(BedBlock.PART) != BedPart.HEAD) {
            return new Inspection(Status.INVALID, null);
        }

        Direction facing = headState.getValue(BedBlock.FACING);
        BlockPos foot = immutableHead.relative(facing.getOpposite());
        LevelChunk footChunk = loadedChunk(level, foot);
        if (footChunk == null) {
            return new Inspection(Status.UNLOADED, null);
        }

        BlockState footState = footChunk.getBlockState(foot);
        if (footState.getBlock() != headState.getBlock()
                || footState.getValue(BedBlock.PART) != BedPart.FOOT
                || footState.getValue(BedBlock.FACING) != facing) {
            return new Inspection(Status.INVALID, null);
        }

        return new Inspection(Status.AVAILABLE,
                new BedParts(immutableHead, foot, facing, headState.getValue(BedBlock.OCCUPIED)));
    }

    private static boolean hasOtherSleeper(ServerLevel level, Mob mob, BedParts parts) {
        int minX = Math.min(parts.head().getX(), parts.foot().getX());
        int maxX = Math.max(parts.head().getX(), parts.foot().getX());
        int minZ = Math.min(parts.head().getZ(), parts.foot().getZ());
        int maxZ = Math.max(parts.head().getZ(), parts.foot().getZ());
        AABB bedArea = new AABB(minX - 1.0, parts.head().getY() - 1.0, minZ - 1.0,
                maxX + 2.0, parts.head().getY() + 2.0, maxZ + 2.0);

        return !level.getEntitiesOfClass(LivingEntity.class, bedArea, entity -> entity != mob
                && entity.isAlive()
                && entity.isSleeping()
                && entity.getSleepingPos().filter(pos -> pos.equals(parts.head()) || pos.equals(parts.foot())).isPresent()
        ).isEmpty();
    }

    private static boolean isSleepingAt(Mob mob, BlockPos head) {
        return mob.isAlive() && mob.isSleeping()
                && mob.getSleepingPos().filter(pos -> pos.equals(head)).isPresent();
    }

    private static void clearHeadOccupiedIfLoaded(ServerLevel level, Mob mob, BlockPos sleepingPos) {
        LevelChunk sleepingChunk = loadedChunk(level, sleepingPos);
        if (sleepingChunk == null) {
            return;
        }

        BlockState sleepingState = sleepingChunk.getBlockState(sleepingPos);
        if (!(sleepingState.getBlock() instanceof BedBlock)) {
            return;
        }
        Direction facing = sleepingState.getValue(BedBlock.FACING);
        BlockPos head = sleepingState.getValue(BedBlock.PART) == BedPart.HEAD
                ? sleepingPos : sleepingPos.relative(facing);
        LevelChunk headChunk = loadedChunk(level, head);
        if (headChunk == null) {
            return;
        }

        BlockState headState = headChunk.getBlockState(head);
        BlockPos foot = head.relative(facing.getOpposite());
        if (headState.getBlock() != sleepingState.getBlock()
                || headState.getValue(BedBlock.PART) != BedPart.HEAD
                || headState.getValue(BedBlock.FACING) != facing
                || !headState.getValue(BedBlock.OCCUPIED)
                || hasOtherSleeper(level, mob, new BedParts(head, foot, facing, true))) {
            return;
        }

        level.setBlock(head, headState.setValue(BedBlock.OCCUPIED, false), Block.UPDATE_CLIENTS);
    }

    private static void wakeWithoutWorldSearch(Mob mob) {
        Vec3 position = mob.position();
        boolean wasNoPhysics = mob.noPhysics;
        mob.noPhysics = true;
        try {
            mob.setPose(Pose.STANDING);
            mob.setPos(position.x, position.y, position.z);
            mob.clearSleepingPos();
        } finally {
            mob.noPhysics = wasNoPhysics;
        }
    }

    private static boolean vanillaWakeAreaLoaded(ServerLevel level, BlockPos sleepingPos, Mob mob) {
        // Vanilla stopSleeping checks bed exits up to two blocks away and their collision boxes.
        double width = Math.max(mob.getType().getDimensions().width(),
                mob.getDimensions(Pose.STANDING).width());
        int padding = 3 + (int) Math.ceil(width);
        int minChunkX = (sleepingPos.getX() - padding) >> 4;
        int maxChunkX = (sleepingPos.getX() + padding) >> 4;
        int minChunkZ = (sleepingPos.getZ() - padding) >> 4;
        int maxChunkZ = (sleepingPos.getZ() + padding) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean sleepingNeighborsLoaded(ServerLevel level, BlockPos head) {
        for (Direction direction : Direction.values()) {
            if (loadedChunk(level, head.relative(direction)) == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean isServerThread(ServerLevel level) {
        return !level.isClientSide() && level.getServer().isSameThread();
    }

    @Nullable
    private static LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
