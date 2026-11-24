package cc.thonly.reverie_dreams.world.gensokyo;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class StructurePlacementGuard {
    private static final Logger LOGGER = LoggerFactory.getLogger(StructurePlacementGuard.class);
    public static final List<ResourceKey<Level>> ENABLED_DIMENSIONS = List.of(RDBuiltinLevels.getGensokyo());
    private static final int CHUNK_GUARD = 1;

    private StructurePlacementGuard() {
    }

    public static boolean isEnabled(ResourceKey<Level> dimension) {
        return dimension != null && ENABLED_DIMENSIONS.contains(dimension);
    }

    public static boolean isPlacementReady(ServerLevel level, BoundingBox bounds) {
        int minChunkX = (bounds.minX() >> 4) - CHUNK_GUARD;
        int minChunkZ = (bounds.minZ() >> 4) - CHUNK_GUARD;
        int maxChunkX = (bounds.maxX() >> 4) + CHUNK_GUARD;
        int maxChunkZ = (bounds.maxZ() >> 4) + CHUNK_GUARD;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                    LOGGER.warn(
                            "Blocked structure placement in {} at bounds {} because chunk ({}, {}) is not fully loaded",
                            level.dimension().identifier(),
                            bounds,
                            chunkX,
                            chunkZ
                    );
                    return false;
                }
            }
        }

        return true;
    }
}
