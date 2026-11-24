package cc.thonly.reverie_dreams.world.gensokyo.road;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;

/**
 * A feature reservation, not a player-build restriction or a per-tick cleanup.
 */
public final class GensokyoRoadProtection {
    private GensokyoRoadProtection() {
    }

    public static GensokyoRoadNetwork network(WorldGenLevel level) {
        ServerLevel server = level instanceof WorldGenRegion region ? region.getLevel()
                : level instanceof ServerLevel actual ? actual : null;
        return server != null && server.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator
                ? generator.mapData().roads() : GensokyoRoadNetwork.EMPTY;
    }

    public static boolean reserved(WorldGenLevel level, BlockPos pos) {
        return network(level).contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean vanillaTreeIntersects(WorldGenLevel level, BlockPos origin) {
        var roads = network(level);
        if (roads.size() == 0)
            return false;
        // Vanilla trees cannot precompute their randomly selected complete
        // voxel shape. Reject a modest bounding envelope before any writes.
        for (int x = origin.getX() - 8; x <= origin.getX() + 8; x++)
            for (int z = origin.getZ() - 8; z <= origin.getZ() + 8; z++) {
                int y = roads.pavementY(x, z);
                if (y != Short.MIN_VALUE && y >= origin.getY() - 40 && y <= origin.getY() + 32)
                    return true;
            }
        return false;
    }
}
