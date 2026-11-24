package cc.thonly.reverie_dreams.paper.world;

import java.util.ArrayList;
import java.util.List;

/** Exact source fillFromNoise/riverbed/border arithmetic, independent of block registries. */
public final class GensokyoTerrain {
    public record Run(int minY, int maxYExclusive, String state) {}
    private final GensokyoColumns cells;
    private final GensokyoMap map;

    public GensokyoTerrain(GensokyoColumns cells) { this.cells = cells; this.map = cells.map(); }
    public GensokyoMap map() { return map; }
    public int borderWidth() { return Math.max(256, Math.min(1024, Math.min(map.width(), map.depth()) / 16)); }
    public int outsideDistance(int x, int z) {
        int dx = Math.max(map.originX() - x, x - (map.originX() + map.width() - 1));
        int dz = Math.max(map.originZ() - z, z - (map.originZ() + map.depth() - 1));
        return Math.max(dx, dz);
    }

    public List<Run> column(int x, int z) {
        Column column = plan(x, z);
        var runs = new ArrayList<Run>(8);
        int from = map.minY();
        String previous = evaluate(column, from);
        for (int y = from + 1; y <= map.maxY() + 1; y++) {
            String current = y <= map.maxY() ? evaluate(column, y) : null;
            if (!previous.equals(current)) {
                if (!isAir(previous)) runs.add(new Run(from, y, previous));
                from = y; previous = current;
            }
        }
        return List.copyOf(runs);
    }

    public String stateAt(int x, int y, int z) {
        if (y < map.minY() || y > map.maxY()) return "minecraft:air";
        String result = evaluate(plan(x, z), y);
        // Source overrides are applied in source order after generation and again
        // after decoration. Later authored writes win, including explicit air.
        for (var block : map.underground().getOrDefault(GensokyoMap.chunkKey(x >> 4, z >> 4), List.of())) {
            if (block.x() == x && block.y() == y && block.z() == z) result = block.state();
        }
        return result;
    }

    private Column plan(int x, int z) {
        int sourceHeight = cells.heightAt(x, z);
        if (sourceHeight == GensokyoMap.NO_SURFACE) {
            int distance = outsideDistance(x, z);
            if (distance > borderWidth()) return new Column(map.maxY(), "minecraft:barrier", 0, null, distance, true, true);
            int top = borderHeight(x, z);
            return new Column(top, borderTopState(x, z, top), 0, null, distance, true, false);
        }
        String surface = cells.surfaceAt(x, z);
        boolean water = type(surface).equals("minecraft:water"), lava = type(surface).equals("minecraft:lava");
        int fluid = water ? waterDepth(x, z) : lava ? 96 : 0;
        String bed = water ? riverbed(x, z) : null;
        return new Column(Math.min(sourceHeight, map.maxY()), surface, fluid, bed, 0, false, false);
    }

    private record Column(int top, String surface, int fluidDepth, String riverbed,
                          int distance, boolean border, boolean barrier) {
        private String stateAt(int y) {
            if (barrier) return "minecraft:barrier";
            if (border) {
                if (distance == 1 && (y <= top - 4 || y > top)) return "minecraft:barrier";
                if (y > top) return "minecraft:air";
                if (y == top) return surface;
                if (y == 0) return "minecraft:bedrock";
                if (type(surface).equals("minecraft:grass_block") && y >= top - 3) return "minecraft:dirt";
                return "minecraft:stone";
            }
            throw new IllegalStateException("Inside column requires map sea/dirt metadata");
        }
    }

    // Kept separately from Column to preserve the exact source branch order:
    // fluid, authored surface, bedrock, two riverbed layers, dirt, then stone.
    private String insideState(Column c, int y) {
        if (isAir(c.surface)) return "minecraft:air";
        if (y > c.top) return y <= map.seaLevel() ? "minecraft:water" : "minecraft:air";
        String kind = type(c.surface);
        boolean water = kind.equals("minecraft:water"), lava = kind.equals("minecraft:lava");
        if ((water || lava) && y > c.top - c.fluidDepth) return water ? "minecraft:water" : "minecraft:lava";
        if (y == c.top) return c.surface;
        if (y == map.minY()) return "minecraft:bedrock";
        if (water && y >= c.top - c.fluidDepth - 1) return c.riverbed;
        if (y >= c.top - map.dirtDepth()) return "minecraft:dirt";
        return "minecraft:stone";
    }

    private String evaluate(Column c, int y) { return c.border ? c.stateAt(y) : insideState(c, y); }

    public int baseHeight(int x, int z, boolean worldSurface, boolean oceanFloor) {
        if (!map.contains(x, z)) {
            int distance = outsideDistance(x, z);
            return distance <= 1 || distance > borderWidth() ? map.maxY() : Math.min(map.maxY(), borderHeight(x, z) + 1);
        }
        int top = cells.heightAt(x, z);
        if (top == GensokyoMap.NO_SURFACE) return map.minY();
        if (worldSurface) return Math.max(top + 1, map.seaLevel() + 1);
        if (oceanFloor && type(cells.surfaceAt(x, z)).equals("minecraft:water"))
            return Math.max(map.minY() + 1, top - waterDepth(x, z) + 1);
        return top + 1;
    }

    public int biomeIndex(int x, int z) {
        int qx = Math.floorDiv(x, 4), qz = Math.floorDiv(z, 4);
        int sx = qx * 4, sz = qz * 4;
        int index = cells.biomeAt(sx, sz), transition = map.biomeTransition();
        if (transition > 0) {
            int px = sx + Math.floorMod(hashBiome(qx, qz, 0), transition * 2 + 1) - transition;
            int pz = sz + Math.floorMod(hashBiome(qx, qz, 1), transition * 2 + 1) - transition;
            if (map.contains(px, pz)) index = cells.biomeAt(px, pz);
        }
        return Math.floorMod(index, map.biomePalette().size());
    }

    private static int hashBiome(int x, int z, int seed) {
        int h = x * 374761393 + z * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }
    private int waterDepth(int x, int z) { return GensokyoRiverbed.depthAt(x, z, (sx, sz) -> type(cells.surfaceAt(sx, sz)).equals("minecraft:water")); }
    private static String riverbed(int x, int z) { return switch (GensokyoRiverbed.materialAt(x, z)) {
        case 1 -> "minecraft:sand"; case 2 -> "minecraft:clay"; case 3 -> "minecraft:stone"; default -> "minecraft:gravel"; }; }

    private int edgeX(int x) { return Math.max(map.originX(), Math.min(map.originX() + map.width() - 1, x)); }
    private int edgeZ(int z) { return Math.max(map.originZ(), Math.min(map.originZ() + map.depth() - 1, z)); }
    private int edgeHeight(int x, int z) { int y = cells.heightAt(edgeX(x), edgeZ(z)); return y == GensokyoMap.NO_SURFACE ? map.seaLevel() : y; }
    public int borderHeight(int x, int z) {
        int distance = outsideDistance(x, z), edge = edgeHeight(x, z), width = borderWidth();
        if (distance <= 1) return Math.min(map.maxY(), edge);
        float t = Math.min(1.0F, (distance - 1.0F) / (width - 1.0F));
        t = t * t * (3.0F - 2.0F * t);
        int target = Math.min(map.maxY() - 12, Math.max(edge + 180, map.seaLevel() + 380));
        double broad = fbm(x * 0.0035D, z * 0.0035D);
        double ridge = 1.0D - Math.abs(fbm(x * 0.0075D + 31.0D, z * 0.0075D - 11.0D));
        double detail = fbm(x * 0.019D, z * 0.019D);
        double relief = broad * 120.0D + ridge * 95.0D + detail * 25.0D;
        double blend = t * (0.45D + 0.55D * t);
        int result = (int) Math.round(edge + (target - edge) * t * 0.62D + relief * blend);
        return Math.max(map.minY() + 1, Math.min(map.maxY(), result));
    }
    private String borderTopState(int x, int z, int height) {
        int distance = outsideDistance(x, z);
        String edge = cells.surfaceAt(edgeX(x), edgeZ(z));
        if (isAir(edge) || type(edge).equals("minecraft:water") || type(edge).equals("minecraft:lava")) edge = null;
        if (distance <= 1) return edge == null ? "minecraft:grass_block" : edge;
        double edgeBlend = Math.max(0.0D, 1.0D - (distance - 1.0D) / 24.0D);
        if (edge != null && edgeBlend > 0.0D && valueNoise(x * 0.22D, z * 0.22D) < edgeBlend * 0.72D) return edge;
        int slope = Math.abs(borderHeight(x + 1, z) - height) + Math.abs(borderHeight(x, z + 1) - height);
        double rock = fbm(x * 0.014D, z * 0.014D), patch = valueNoise(x * 0.075D, z * 0.075D);
        int snowLine = map.seaLevel() + 320 + (int) Math.round(rock * 30.0D);
        int stoneLine = map.seaLevel() + 100 + (int) Math.round(rock * 45.0D);
        if (height >= snowLine && slope <= 1) return "minecraft:snow_block";
        if (slope >= 2 || height >= stoneLine || patch > 0.62D) return "minecraft:stone";
        return "minecraft:grass_block";
    }
    private static double valueNoise(double x, double z) {
        int x0 = (int) Math.floor(x), z0 = (int) Math.floor(z);
        double fx = x - x0, fz = z - z0;
        fx = fx * fx * (3.0D - 2.0D * fx); fz = fz * fz * (3.0D - 2.0D * fz);
        double n00 = hash2(x0, z0), n10 = hash2(x0 + 1, z0), n01 = hash2(x0, z0 + 1), n11 = hash2(x0 + 1, z0 + 1);
        double nx0 = n00 + (n10 - n00) * fx, nx1 = n01 + (n11 - n01) * fx;
        return nx0 + (nx1 - nx0) * fz;
    }
    private static double fbm(double x, double z) {
        double sum = 0.0D, amplitude = 1.0D, frequency = 1.0D, total = 0.0D;
        for (int i = 0; i < 4; i++) { sum += valueNoise(x * frequency, z * frequency) * amplitude; total += amplitude; frequency *= 2.03D; amplitude *= 0.5D; }
        return sum / total;
    }
    private static double hash2(int x, int z) { int h = x * 374761393 + z * 668265263; h = (h ^ (h >>> 13)) * 1274126177; h ^= h >>> 16; return (h & 0xFFFFFF) / 8388607.5D - 1.0D; }
    private static String type(String state) { int bracket = state.indexOf('['); return bracket < 0 ? state : state.substring(0, bracket); }
    private static boolean isAir(String state) { String kind = type(state); return kind.equals("minecraft:air") || kind.equals("minecraft:cave_air") || kind.equals("minecraft:void_air"); }
}
