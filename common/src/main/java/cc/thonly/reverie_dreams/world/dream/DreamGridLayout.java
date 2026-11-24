package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical, walkable grid planes above and below the island/building activity band.
 */
public final class DreamGridLayout {
    public static final int HORIZONTAL_SPACING = 16;
    public static final int ACTIVITY_MIN_Y = 80;
    public static final int ACTIVITY_MAX_Y = 184;

    public record Layer(int y, boolean red) {
    }

    private static final List<Layer> LAYERS = List.of(
            new Layer(32, true), new Layer(64, false),
            new Layer(192, true), new Layer(224, false));

    public static List<Layer> layers() {
        return LAYERS;
    }

    public static boolean isRedLayer(int y) {
        return LAYERS.stream().anyMatch(layer -> layer.y == y && layer.red);
    }

    public static boolean isGridPosition(BlockPos pos) {
        return (Math.floorMod(pos.getX(), HORIZONTAL_SPACING) == 0
                || Math.floorMod(pos.getZ(), HORIZONTAL_SPACING) == 0)
                && LAYERS.stream().anyMatch(layer -> layer.y == pos.getY());
    }

    /**
     * Own-chunk blocks only, clipped to the world's absolute [minY, maxY) interval.
     * Unmasked geometric baseline. Production uses the seeded overload for blue avoidance.
     */
    public static List<BlockPos> blocksInChunk(int chunkX, int chunkZ, int minY, int maxY) {
        List<BlockPos> result = new ArrayList<>(LAYERS.size() * 31);
        int startX = chunkX * 16, startZ = chunkZ * 16;
        for (Layer layer : LAYERS) {
            if (layer.y < minY || layer.y >= maxY)
                continue;
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    if (Math.floorMod(x, HORIZONTAL_SPACING) == 0
                            || Math.floorMod(z, HORIZONTAL_SPACING) == 0)
                        result.add(new BlockPos(x, layer.y, z));
                }
            }
        }
        return List.copyOf(result);
    }

    /**
     * Red routes stay continuous; blue blocks yield only to geometry at the same X/Y/Z.
     */
    public static List<BlockPos> blocksInChunk(long seed, int chunkX, int chunkZ, int minY, int maxY) {
        return blocksInChunk(chunkX, chunkZ, minY, maxY).stream()
                .filter(pos -> isRedLayer(pos.getY()) || !DreamBlueGridMask.isExcluded(seed, pos))
                .toList();
    }

    public static boolean isGridPosition(long seed, BlockPos pos) {
        return isGridPosition(pos) && (isRedLayer(pos.getY())
                || !DreamBlueGridMask.isExcluded(seed, pos));
    }

    private DreamGridLayout() {
    }
}
