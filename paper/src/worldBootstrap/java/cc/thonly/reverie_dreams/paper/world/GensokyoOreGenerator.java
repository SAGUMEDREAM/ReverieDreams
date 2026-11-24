package cc.thonly.reverie_dreams.paper.world;

import java.util.BitSet;
import java.util.List;

/** Source terrain-relative bands and target26.1.2 OreFeature ellipsoid algorithm, without engine imports. */
final class GensokyoOreGenerator {
    static final int MAX_DEPTH = 320;
    enum Shape { UNIFORM, TRIANGLE, DEEP_RAMP, ALTITUDE_TRIANGLE }
    enum Restriction { OVERWORLD, MOUNTAIN, BADLANDS }
    record Band(String name, String feature, int minCount, int maxCount, int rarity, int low, int high, Shape shape, Restriction restriction) {
        int count(GensokyoFeatureRandom random) {
            if (rarity > 1 && random.nextInt(rarity) != 0) return 0;
            return minCount + random.nextInt(maxCount - minCount + 1);
        }
        int y(GensokyoFeatureRandom random, int solidTop, int sea) {
            int width = high - low;
            int value = switch (shape) {
                case UNIFORM -> low + random.nextInt(width + 1);
                case TRIANGLE, ALTITUDE_TRIANGLE -> low + random.nextInt(width / 2 + 1) + random.nextInt(width - width / 2 + 1);
                case DEEP_RAMP -> low + Math.max(random.nextInt(width + 1), random.nextInt(width + 1));
            };
            return shape == Shape.ALTITUDE_TRIANGLE ? sea + value : solidTop - value;
        }
    }
    private static Band band(String name, String feature, int count, int low, int high, Shape shape) {
        return new Band(name, feature, count, count, 1, low, high, shape, Restriction.OVERWORLD);
    }
    static final List<Band> BANDS = List.of(
            band("coal_shallow", "minecraft:ore_coal", 24, 8, 128, Shape.UNIFORM),
            band("coal_buried", "minecraft:ore_coal_buried", 20, 48, 224, Shape.TRIANGLE),
            band("iron_middle", "minecraft:ore_iron", 10, 16, 160, Shape.TRIANGLE),
            band("iron_small", "minecraft:ore_iron_small", 10, 8, 256, Shape.UNIFORM),
            band("iron_highland", "minecraft:ore_iron", 90, 80, 384, Shape.ALTITUDE_TRIANGLE),
            band("copper", "minecraft:ore_copper_small", 16, 8, 144, Shape.TRIANGLE),
            band("gold", "minecraft:ore_gold_buried", 4, 112, 288, Shape.TRIANGLE),
            new Band("gold_deep", "minecraft:ore_gold_buried", 0, 1, 1, 280, 312, Shape.UNIFORM, Restriction.OVERWORLD),
            new Band("gold_badlands", "minecraft:ore_gold", 50, 50, 1, 24, 128, Shape.UNIFORM, Restriction.BADLANDS),
            band("redstone", "minecraft:ore_redstone", 4, 96, 224, Shape.UNIFORM),
            band("redstone_deep", "minecraft:ore_redstone", 8, 176, 312, Shape.DEEP_RAMP),
            band("diamond", "minecraft:ore_diamond_small", 3, 160, 312, Shape.DEEP_RAMP),
            band("diamond_medium", "minecraft:ore_diamond_medium", 2, 248, 312, Shape.UNIFORM),
            new Band("diamond_large", "minecraft:ore_diamond_large", 1, 1, 9, 160, 312, Shape.DEEP_RAMP, Restriction.OVERWORLD),
            band("diamond_buried", "minecraft:ore_diamond_buried", 2, 192, 312, Shape.DEEP_RAMP),
            band("lapis", "minecraft:ore_lapis", 2, 96, 256, Shape.TRIANGLE),
            band("lapis_buried", "minecraft:ore_lapis_buried", 4, 48, 288, Shape.UNIFORM),
            new Band("emerald_highland", "minecraft:ore_emerald", 100, 100, 1, -16, 480, Shape.ALTITUDE_TRIANGLE, Restriction.MOUNTAIN),
            band("silver", "reverie_dreams:overworld_silver_ore", 9, 8, 256, Shape.UNIFORM),
            band("orb", "reverie_dreams:overworld_orb_ore", 4, 192, 312, Shape.DEEP_RAMP));

    interface Region {
        String state(int x, int y, int z);
        void set(int x, int y, int z, String state);
        boolean matches(GensokyoOreFeatures.Target target, String current);
        boolean air(int x, int y, int z);
        boolean canWrite(int x, int y, int z);
        int oceanFloor(int x, int z);
        String biome(int x, int y, int z);
    }
    private final GensokyoColumns columns;
    private final GensokyoMap map;
    private final GensokyoOreFeatures catalog;
    GensokyoOreGenerator(GensokyoColumns columns, GensokyoOreFeatures catalog) {
        this.columns = columns; this.map = columns.map(); this.catalog = catalog;
    }
    int miningFloor() { return Math.max(map.minY() + 1, map.seaLevel() - MAX_DEPTH); }
    int solidTop(int x, int z) {
        if (!map.contains(x, z)) return GensokyoMap.NO_SURFACE;
        int top = columns.heightAt(x, z);
        String state = kind(columns.surfaceAt(x, z));
        if (top == GensokyoMap.NO_SURFACE || airKind(state)) return GensokyoMap.NO_SURFACE;
        if (state.equals("minecraft:water")) return top - GensokyoRiverbed.depthAt(x, z, (sx, sz) -> kind(columns.surfaceAt(sx, sz)).equals("minecraft:water"));
        if (state.equals("minecraft:lava")) return top - 96;
        return top;
    }
    static int horizontalEnvelope(int size) {
        return (int) Math.ceil(size / 8F) + radius(size) + 1;
    }
    private static int radius(int size) { return (int) Math.ceil((size / 16F * 2F + 1F) / 2F); }
    boolean fitsVein(int x, int y, int z, int size) {
        int horizontal = horizontalEnvelope(size), vertical = radius(size) + 3, low = y - vertical, high = y + vertical;
        if (low < miningFloor() || high >= map.minY() + map.height()) return false;
        for (int sx = x - horizontal; sx <= x + horizontal; sx++) for (int sz = z - horizontal; sz <= z + horizontal; sz++) {
            int top = solidTop(sx, sz);
            if (top == GensokyoMap.NO_SURFACE || low < top - MAX_DEPTH || high > top - 4) return false;
        }
        for (int cx = (x - horizontal) >> 4; cx <= (x + horizontal) >> 4; cx++) for (int cz = (z - horizontal) >> 4; cz <= (z + horizontal) >> 4; cz++)
            for (var block : map.underground().getOrDefault(GensokyoMap.chunkKey(cx, cz), List.of()))
                if (block.x() >= x - horizontal && block.x() <= x + horizontal && block.z() >= z - horizontal && block.z() <= z + horizontal && block.y() >= low && block.y() <= high) return false;
        return true;
    }
    boolean allows(Band band, String biome) {
        if (!biome.equals("reverie_dreams:maple_forest") && !catalog.biomeRules().get("overworld").contains(biome)) return false;
        return switch (band.restriction()) {
            case OVERWORLD -> true;
            case MOUNTAIN -> catalog.biomeRules().get("mountain").contains(biome);
            case BADLANDS -> catalog.biomeRules().get("badlands").contains(biome);
        };
    }
    void populate(long worldSeed, int chunkX, int chunkZ, Region region) {
        var random = new GensokyoFeatureRandom(0);
        long seed = random.decorationSeed(worldSeed, chunkX << 4, chunkZ << 4);
        for (Band band : BANDS) {
            GensokyoOreFeatures.Feature feature = catalog.features().get(band.feature());
            random.bandSeed(seed, band.name());
            int count = band.count(random);
            for (int attempt = 0; attempt < count; attempt++) {
                int x = (chunkX << 4) + random.nextInt(16), z = (chunkZ << 4) + random.nextInt(16), top = solidTop(x, z);
                if (top == GensokyoMap.NO_SURFACE) continue;
                int y = band.y(random, top, map.seaLevel());
                if (!fitsVein(x, y, z, feature.size()) || !allows(band, region.biome(x, y, z))) continue;
                placeVein(random, feature, x, y, z, region);
            }
        }
    }

    /** Mod-injected base placements; these intentionally retain their full-height/low-world anchors, not the supplemental320-depth floor. */
    void populateSourcePlacements(long worldSeed, int chunkX, int chunkZ, Region region) {
        var random = new GensokyoFeatureRandom(0);
        long seed = random.decorationSeed(worldSeed, chunkX << 4, chunkZ << 4);
        for (boolean silver : new boolean[]{true, false}) {
            String name = silver ? "overworld_silver_ore_placed" : "overworld_orb_ore_placed";
            var feature = catalog.features().get(silver ? "reverie_dreams:overworld_silver_ore" : "reverie_dreams:overworld_orb_ore");
            random.bandSeed(seed, name);
            int count = silver ? 9 : 7;
            for (int attempt = 0; attempt < count; attempt++) {
                int x = (chunkX << 4) + random.nextInt(16), z = (chunkZ << 4) + random.nextInt(16);
                int y = silver ? map.minY() + 10 + random.nextInt(map.height() - 20)
                        : map.minY() - 80 + random.nextInt(81) + random.nextInt(81);
                String biome = region.biome(x, y, z);
                if (!catalog.biomeRules().get("overworld").contains(biome)) continue;
                placeVein(random, feature, x, y, z, region);
            }
        }
    }

    static boolean placeVein(GensokyoFeatureRandom random, GensokyoOreFeatures.Feature config, int x, int y, int z, Region region) {
        float angle = random.nextFloat() * (float) Math.PI, span = config.size() / 8F;
        int radius = radius(config.size());
        double x0 = x + Math.sin(angle) * span, x1 = x - Math.sin(angle) * span;
        double z0 = z + Math.cos(angle) * span, z1 = z - Math.cos(angle) * span;
        double y0 = y + random.nextInt(3) - 2, y1 = y + random.nextInt(3) - 2;
        int minX = x - (int) Math.ceil(span) - radius, minY = y - 2 - radius, minZ = z - (int) Math.ceil(span) - radius;
        int width = 2 * ((int) Math.ceil(span) + radius), height = 2 * (2 + radius);
        boolean belowFloor = false;
        for (int sx = minX; sx <= minX + width && !belowFloor; sx++) for (int sz = minZ; sz <= minZ + width; sz++)
            if (minY <= region.oceanFloor(sx, sz)) { belowFloor = true; break; }
        if (!belowFloor) return false;
        double[] spheres = new double[config.size() * 4];
        for (int i = 0; i < config.size(); i++) {
            float progress = (float) i / config.size();
            double scale = random.nextDouble() * config.size() / 16D;
            spheres[i * 4] = lerp(progress, x0, x1); spheres[i * 4 + 1] = lerp(progress, y0, y1); spheres[i * 4 + 2] = lerp(progress, z0, z1);
            spheres[i * 4 + 3] = ((sin((float) Math.PI * progress) + 1F) * scale + 1D) / 2D;
        }
        for (int i = 0; i < config.size() - 1; i++) if (spheres[i * 4 + 3] > 0)
            for (int j = i + 1; j < config.size(); j++) if (spheres[j * 4 + 3] > 0) {
                double dx = spheres[i * 4] - spheres[j * 4], dy = spheres[i * 4 + 1] - spheres[j * 4 + 1], dz = spheres[i * 4 + 2] - spheres[j * 4 + 2];
                double difference = spheres[i * 4 + 3] - spheres[j * 4 + 3];
                if (difference * difference > dx * dx + dy * dy + dz * dz) spheres[(difference > 0 ? j : i) * 4 + 3] = -1D;
            }
        var visited = new BitSet(width * height * width);
        boolean placed = false;
        for (int i = 0; i < config.size(); i++) {
            double r = spheres[i * 4 + 3]; if (r < 0) continue;
            double cx = spheres[i * 4], cy = spheres[i * 4 + 1], cz = spheres[i * 4 + 2];
            int fromX = Math.max((int) Math.floor(cx - r), minX), fromY = Math.max((int) Math.floor(cy - r), minY), fromZ = Math.max((int) Math.floor(cz - r), minZ);
            int toX = Math.max((int) Math.floor(cx + r), fromX), toY = Math.max((int) Math.floor(cy + r), fromY), toZ = Math.max((int) Math.floor(cz + r), fromZ);
            for (int sx = fromX; sx <= toX; sx++) {
                double dx = (sx + .5D - cx) / r; if (dx * dx >= 1) continue;
                for (int sy = fromY; sy <= toY; sy++) {
                    double dy = (sy + .5D - cy) / r; if (dx * dx + dy * dy >= 1) continue;
                    for (int sz = fromZ; sz <= toZ; sz++) {
                        double dz = (sz + .5D - cz) / r;
                        if (dx * dx + dy * dy + dz * dz >= 1 || !region.canWrite(sx, sy, sz)) continue;
                        int bit = sx - minX + (sy - minY) * width + (sz - minZ) * width * height;
                        if (visited.get(bit)) continue;
                        visited.set(bit);
                        String current = region.state(sx, sy, sz);
                        for (var target : config.targets()) {
                            if (!region.matches(target, current)) continue;
                            boolean skipAir = config.discardChance() <= 0 || config.discardChance() < 1 && random.nextFloat() >= config.discardChance();
                            if (!skipAir && adjacentAir(region, sx, sy, sz)) continue;
                            region.set(sx, sy, sz, target.state()); placed = true; break;
                        }
                    }
                }
            }
        }
        return placed;
    }
    private static boolean adjacentAir(Region region, int x, int y, int z) {
        return region.air(x, y - 1, z) || region.air(x, y + 1, z) || region.air(x, y, z - 1) || region.air(x, y, z + 1) || region.air(x - 1, y, z) || region.air(x + 1, y, z);
    }
    private static final float[] SIN = new float[65536];
    static { for (int i = 0; i < SIN.length; i++) SIN[i] = (float) Math.sin(i * Math.PI * 2D / 65536D); }
    private static float sin(double value) { return SIN[(int) ((long) (value * 10430.378350470453D) & 65535L)]; }
    private static double lerp(double delta, double from, double to) { return from + delta * (to - from); }
    private static String kind(String state) { int bracket = state.indexOf('['); return bracket < 0 ? state : state.substring(0, bracket); }
    private static boolean airKind(String kind) { return kind.equals("minecraft:air") || kind.equals("minecraft:cave_air") || kind.equals("minecraft:void_air"); }
}
