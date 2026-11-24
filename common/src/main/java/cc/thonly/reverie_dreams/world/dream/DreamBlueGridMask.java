package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Each blue plane avoids only planned non-air blocks at that exact X/Y/Z.
 */
public final class DreamBlueGridMask {
    private static final int SPACING = DreamArchipelagoLayout.CLUSTER_SPACING;

    private record Cell(long seed, int x, int z) {
    }

    private static final Map<Cell, Occupancy> MASKS = new LinkedHashMap<>(8, .75f, true);

    public static boolean isExcluded(long seed, BlockPos pos) {
        if (DreamGridLayout.layers().stream().noneMatch(layer -> !layer.red() && layer.y() == pos.getY()))
            return false;
        int cellX = (int) Math.floorDiv((long) pos.getX() + SPACING / 2, SPACING);
        int cellZ = (int) Math.floorDiv((long) pos.getZ() + SPACING / 2, SPACING);
        return mask(seed, cellX, cellZ).contains(pos);
    }

    private static synchronized Occupancy mask(long seed, int cellX, int cellZ) {
        Cell cell = new Cell(seed, cellX, cellZ);
        Occupancy cached = MASKS.get(cell);
        if (cached != null)
            return cached;
        Occupancy result = new Occupancy(cellX * SPACING - SPACING / 2, cellZ * SPACING - SPACING / 2);
        // Use authored block positions, including AIR gaps between foundations and roofs.
        // No projected column, scenery box, horizontal buffer or line-fragment cleanup.
        DreamArchipelagoLayout.clusterBlocks(seed, cellX, cellZ).forEach(result::add);
        for (var island : DreamTerrainLayout.islands(seed, cellX, cellZ)) {
            boolean intersectsLayer = DreamGridLayout.layers().stream()
                    .anyMatch(layer -> !layer.red() && layer.y() >= island.minY() && layer.y() <= island.maxY());
            if (!intersectsLayer)
                continue;
            for (int cx = Math.floorDiv(island.minX(), 16); cx <= Math.floorDiv(island.maxX(), 16); cx++) {
                for (int cz = Math.floorDiv(island.minZ(), 16); cz <= Math.floorDiv(island.maxZ(), 16); cz++)
                    DreamTerrainLayout.forEachBlockInChunk(seed, cx, cz, result::add);
            }
        }
        MASKS.put(cell, result);
        if (MASKS.size() > 8)
            MASKS.remove(MASKS.keySet().iterator().next());
        return result;
    }

    /**
     * Sparse blue-layer slices; shared by authored and streamed natural geometry.
     */
    static final class Occupancy {
        private final int minX, minZ;
        private final Map<Integer, BitSet> layers = new LinkedHashMap<>();

        Occupancy(int minX, int minZ) {
            this.minX = minX;
            this.minZ = minZ;
            for (var layer : DreamGridLayout.layers())
                if (!layer.red())
                    layers.put(layer.y(), new BitSet(SPACING * SPACING));
        }

        void add(BlockPos pos, BlockState state) {
            BitSet plane = layers.get(pos.getY());
            if (plane == null || state.isAir())
                return;
            int x = pos.getX() - minX, z = pos.getZ() - minZ;
            if (x < 0 || x >= SPACING || z < 0 || z >= SPACING)
                throw new IllegalStateException("Dream occupancy leaves its owning cluster");
            plane.set(z * SPACING + x);
        }

        boolean contains(BlockPos pos) {
            BitSet plane = layers.get(pos.getY());
            int x = pos.getX() - minX, z = pos.getZ() - minZ;
            return plane != null && x >= 0 && x < SPACING && z >= 0 && z < SPACING
                    && plane.get(z * SPACING + x);
        }
    }

    private DreamBlueGridMask() {
    }
}
