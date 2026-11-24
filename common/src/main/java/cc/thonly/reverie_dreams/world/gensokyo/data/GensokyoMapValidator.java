package cc.thonly.reverie_dreams.world.gensokyo.data;

import com.google.gson.JsonObject;

/**
 * 运行时地图数据的快速校验。加载时 fail fast，避免生成到一半才发现数组长度不对。
 */
public final class GensokyoMapValidator {
    public static final int MAX_MAP_SIZE = 32768;
    public static final int DIMENSION_HEIGHT = 1536;
    // Practical memory guard: 12800 x 10240 (131,072,000) fits.
    private static final int MAX_CELLS = 134_217_728;

    private GensokyoMapValidator() {
    }

    /**
     * Validates allocation bounds and metadata before a single full-size layer is allocated.
     */
    public static int validateHeader(JsonObject root) {
        return validateMetadata(root.get("format_version").getAsInt(), root.get("map_version").getAsInt(),
                root.get("width").getAsInt(), root.get("depth").getAsInt(), root.get("min_y").getAsInt(),
                root.get("world_height").getAsInt(), root.get("sea_level").getAsInt(),
                root.has("dirt_depth") ? root.get("dirt_depth").getAsInt() : 3,
                root.has("biome_transition") ? root.get("biome_transition").getAsInt() : 0);
    }

    private static int validateMetadata(int formatVersion, int mapVersion, int width, int depth,
                                        int minY, int worldHeight, int seaLevel, int dirtDepth, int biomeTransition) {
        if (formatVersion != 1)
            throw new IllegalStateException("Unsupported Gensokyo map format_version: " + formatVersion);
        if (mapVersion < 1)
            throw new IllegalStateException("Gensokyo map_version must be >= 1");
        if (width <= 0 || depth <= 0)
            throw new IllegalStateException("Gensokyo map width/depth must be positive");
        if (width > MAX_MAP_SIZE || depth > MAX_MAP_SIZE) {
            throw new IllegalStateException("Gensokyo map is larger than " + MAX_MAP_SIZE + "x" + MAX_MAP_SIZE
                    + ": " + width + "x" + depth);
        }
        long cells = (long) width * depth;
        if (cells > MAX_CELLS)
            throw new IllegalStateException("Gensokyo map is too large: " + width + "x" + depth);
        if (worldHeight != DIMENSION_HEIGHT) {
            throw new IllegalStateException("world_height must be " + DIMENSION_HEIGHT + ", got " + worldHeight);
        }
        long topY = (long) minY + worldHeight;
        if (topY > Integer.MAX_VALUE || seaLevel < minY || seaLevel >= topY) {
            throw new IllegalStateException("sea_level or dimension height is outside valid bounds");
        }
        if (dirtDepth < 0 || dirtDepth > 32)
            throw new IllegalStateException("dirt_depth must be in 0..32");
        if (biomeTransition < 0 || biomeTransition > 64)
            throw new IllegalStateException("biome_transition must be in 0..64");
        return (int) cells;
    }

    public static void validate(GensokyoMapData map) {
        int expected = validateMetadata(map.formatVersion(), map.mapVersion(), map.width(), map.depth(),
                map.minY(), map.worldHeight(), map.seaLevel(), map.dirtDepth(), map.biomeTransition());
        if (map.id() == null)
            throw new IllegalStateException("Gensokyo map id is missing");
        if (map.heights().length != expected) {
            throw new IllegalStateException("heightmap length mismatch: expected " + expected + ", got " + map.heights().length);
        }
        if (map.biomes().length != expected) {
            throw new IllegalStateException("biome layer length mismatch: expected " + expected + ", got " + map.biomes().length);
        }
        if (map.surfaces().length != expected) {
            throw new IllegalStateException("surface layer length mismatch: expected " + expected + ", got " + map.surfaces().length);
        }

        if (map.biomePalette().isEmpty()) {
            throw new IllegalStateException("biome_palette is empty");
        }
        if (map.surfacePalette().isEmpty()) {
            throw new IllegalStateException("surface_palette is empty");
        }

        int maxY = map.minY() + map.worldHeight() - 1;
        for (int i = 0; i < expected; i++) {
            int height = map.heights()[i];
            if (height != GensokyoMapData.NO_SURFACE && (height < map.minY() || height > maxY)) {
                throw new IllegalStateException("heightmap[" + i + "]=" + height + " is outside [" + map.minY() + ", " + maxY + "]");
            }

            int biome = map.biomes()[i] & 0xFF;
            if (biome >= map.biomePalette().size()) {
                throw new IllegalStateException("biome[" + i + "]=" + biome + " is outside palette size " + map.biomePalette().size());
            }

            int surface = map.surfaces()[i] & 0xFFFF;
            if (surface >= map.surfacePalette().size()) {
                throw new IllegalStateException("surface[" + i + "]=" + surface + " is outside palette size " + map.surfacePalette().size());
            }
        }

        if (!map.contains(map.spawn().pos().getX(), map.spawn().pos().getZ())) {
            throw new IllegalStateException("spawn is outside map bounds: " + map.spawn().pos());
        }
        if (map.spawn().pos().getY() < map.minY() || map.spawn().pos().getY() > maxY + 1) {
            throw new IllegalStateException("spawn Y is outside dimension height: " + map.spawn().pos());
        }

        for (GensokyoMapData.Anchor anchor : map.anchors()) {
            if (anchor.id() == null) {
                throw new IllegalStateException("anchor id is missing");
            }
            if (!map.contains(anchor.pos().getX(), anchor.pos().getZ())) {
                throw new IllegalStateException("anchor " + anchor.id() + " is outside map bounds: " + anchor.pos());
            }
            if (anchor.pos().getY() < map.minY() || anchor.pos().getY() > maxY + 1) {
                throw new IllegalStateException("anchor " + anchor.id() + " Y is outside dimension height: " + anchor.pos());
            }
            if (!(anchor.scale() >= 0.25F && anchor.scale() <= 4.0F)) {
                throw new IllegalStateException("anchor " + anchor.id() + " scale must be in 0.25..4.0: " + anchor.scale());
            }
        }

        for (var entry : map.underground().values()) {
            for (GensokyoMapData.UndergroundBlock block : entry) {
                if (!map.contains(block.x(), block.z())) {
                    throw new IllegalStateException("underground block is outside map bounds: " + block);
                }
                if (block.y() < map.minY() || block.y() > maxY) {
                    throw new IllegalStateException("underground block Y is outside dimension height: " + block);
                }
            }
        }
    }
}
