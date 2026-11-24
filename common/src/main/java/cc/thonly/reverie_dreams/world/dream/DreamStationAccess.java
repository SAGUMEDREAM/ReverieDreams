package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Prefers the origin station and accepts intact, loaded legacy landings in older saves.
 */
public final class DreamStationAccess {
    private static final int[][] CANDIDATES = {
            {0, 0}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, -1},
            {2, 0}, {0, 2}, {-2, 0}, {0, -2}, {64, 0}, {0, 64}
    };

    public static Optional<BlockPos> findArrival(ServerLevel level) {
        if (!level.dimension().equals(RDBuiltinLevels.DREAM_WORLD))
            return Optional.empty();
        for (int[] cell : CANDIDATES) {
            BlockPos landing = DreamArchipelagoLayout.stationLanding(cell[0], cell[1]);
            if (!level.getWorldBorder().isWithinBounds(landing))
                continue;
            if (cell[0] == 0 && cell[1] == 0) {
                level.getChunkAt(landing);
            } else if (!level.hasChunksAt(landing.offset(-1, -1, -1), landing.offset(1, 1, 1))) {
                // New terrain cannot produce another station. Only inspect legacy floors already in memory.
                continue;
            }
            if (isUsableLanding(level, landing))
                return Optional.of(landing);
        }
        return Optional.empty();
    }

    public static boolean isUsableLanding(ServerLevel level, BlockPos landing) {
        BlockPos expectedLanding = DreamArchipelagoLayout.stationLanding(0, 0);
        int shiftX = landing.getX() - expectedLanding.getX();
        int shiftZ = landing.getZ() - expectedLanding.getZ();
        if (landing.getY() != expectedLanding.getY()
                || Math.floorMod(shiftX, DreamArchipelagoLayout.CLUSTER_SPACING) != 0
                || Math.floorMod(shiftZ, DreamArchipelagoLayout.CLUSTER_SPACING) != 0)
            return false;
        var plan = DreamArchipelagoLayout.clusterBlocks();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos floor = landing.offset(x, -1, z);
                var expected = plan.get(floor.offset(-shiftX, 0, -shiftZ));
                var actual = level.getBlockState(floor);
                if (expected == null || !actual.equals(expected) || !actual.isFaceSturdy(level, floor, Direction.UP))
                    return false;
                for (int y = 1; y <= 2; y++) {
                    BlockPos headroom = floor.above(y);
                    var state = level.getBlockState(headroom);
                    if (!state.getCollisionShape(level, headroom).isEmpty() || !state.getFluidState().isEmpty())
                        return false;
                }
            }
        }
        return true;
    }

    private DreamStationAccess() {
    }
}
