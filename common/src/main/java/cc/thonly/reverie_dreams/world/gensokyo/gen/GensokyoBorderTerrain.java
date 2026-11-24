package cc.thonly.reverie_dreams.world.gensokyo.gen;

import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Deterministic scenery outside the authored rectangle. Kept in sync with border-terrain.js by parity vectors.
 */
public final class GensokyoBorderTerrain {
    public interface MapView {
        int width();

        int depth();

        int originX();

        int originZ();

        int minY();

        int worldHeight();

        int seaLevel();

        int heightAt(int x, int z);

        String surfaceAt(int x, int z);

        String biomeAt(int x, int z);
    }

    public record Settings(int version, int minWidth, int maxWidth, int widthDivisor, int contextDepth,
                           double surfaceBlendFraction, double waterReachFraction, double mountainOffset,
                           double broadRelief, double ridgeRelief) {
        public static Settings load() {
            try (var stream = GensokyoBorderTerrain.class.getResourceAsStream("/data/reverie_dreams/gensokyo/border-settings.json")) {
                var json = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(stream), StandardCharsets.UTF_8)).getAsJsonObject();
                var value = new Settings(json.get("version").getAsInt(), json.get("min_width").getAsInt(), json.get("max_width").getAsInt(),
                        json.get("width_divisor").getAsInt(), json.get("context_depth").getAsInt(), json.get("surface_blend_fraction").getAsDouble(),
                        json.get("water_reach_fraction").getAsDouble(), json.get("mountain_offset").getAsDouble(),
                        json.get("broad_relief").getAsDouble(), json.get("ridge_relief").getAsDouble());
                if (value.version != 2 || value.minWidth < 32 || value.maxWidth < value.minWidth || value.widthDivisor < 1
                        || value.contextDepth < 1 || value.surfaceBlendFraction <= 0 || value.surfaceBlendFraction > 1
                        || value.waterReachFraction <= 0 || value.waterReachFraction > 1)
                    throw new IllegalArgumentException("Invalid border settings");
                return value;
            } catch (Exception error) {
                throw new IllegalStateException("Cannot read Gensokyo border settings", error);
            }
        }
    }

    public record Column(int groundY, int waterY, String surface, String biome) {
        public boolean water() {
            return waterY > groundY;
        }

        public int visibleY() {
            return water() ? waterY : groundY;
        }
    }

    public static final Settings SETTINGS = Settings.load();
    /**
     * Direct-mapped cache size; border columns are sampled by several concurrent worldgen stages.
     */
    private static final int CACHE_BITS = 14;
    private final MapView map;
    private final Settings settings;
    private final int width;
    private final AtomicReferenceArray<CachedColumn> columnCache = new AtomicReferenceArray<>(1 << CACHE_BITS);

    public GensokyoBorderTerrain(MapView map) {
        this(map, SETTINGS);
    }

    public GensokyoBorderTerrain(MapView map, Settings settings) {
        this.map = map;
        this.settings = settings;
        this.width = (int) clamp(Math.min(map.width(), map.depth()) / settings.widthDivisor, settings.minWidth, settings.maxWidth);
    }

    public int width() {
        return width;
    }

    public double distance(int x, int z) {
        double dx = Math.max(0, Math.max(map.originX() - (double) x, x - (map.originX() + (double) map.width() - 1)));
        double dz = Math.max(0, Math.max(map.originZ() - (double) z, z - (map.originZ() + (double) map.depth() - 1)));
        return Math.hypot(dx, dz);
    }

    private int edgeX(int x) {
        return (int) clamp(x, map.originX(), map.originX() + map.width() - 1);
    }

    private int edgeZ(int z) {
        return (int) clamp(z, map.originZ(), map.originZ() + map.depth() - 1);
    }

    private int validHeight(int x, int z) {
        int h = map.heightAt(edgeX(x), edgeZ(z));
        return h == Short.MIN_VALUE ? map.seaLevel() : h;
    }

    private record CachedColumn(long key, Column column) {
    }

    public Column sample(int x, int z) {
        long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
        int slot = (int) ((key * 0x9E3779B97F4A7C15L) >>> (64 - CACHE_BITS));
        CachedColumn cached = this.columnCache.get(slot);
        if (cached != null && cached.key() == key) {
            return cached.column();
        }
        Column column = this.sampleUncached(x, z);
        this.columnCache.set(slot, new CachedColumn(key, column));
        return column;
    }

    private Column sampleUncached(int x, int z) {
        int ex = edgeX(x), ez = edgeZ(z);
        double distance = distance(x, z), d = Math.max(0, distance - 1);
        int edge = validHeight(ex, ez);
        String edgeSurface = map.surfaceAt(ex, ez), edgeBiome = map.biomeAt(ex, ez);
        boolean edgeWater = edgeSurface.equals("minecraft:water");
        boolean cold = cold(edgeBiome) || edgeSurface.equals("minecraft:snow_block") || edgeSurface.equals("minecraft:snow");
        boolean alongX = x == ex || (z != ez && Math.abs(x - ex) < Math.abs(z - ez));
        int ax = alongX ? 1 : 0, az = alongX ? 0 : 1;
        double smoothEdge = (validHeight(ex - ax * 24, ez - az * 24) + validHeight(ex - ax * 12, ez - az * 12) * 2.0
                + edge * 4.0 + validHeight(ex + ax * 12, ez + az * 12) * 2.0 + validHeight(ex + ax * 24, ez + az * 24)) / 10.0;
        double nx = x - ex, nz = z - ez, length = Math.hypot(nx, nz);
        if (nx != 0 && nz != 0) {
            double sx = (validHeight(ex - 24, ez) + validHeight(ex - 12, ez) * 2.0 + edge * 4.0 + validHeight(ex + 12, ez) * 2.0 + validHeight(ex + 24, ez)) / 10.0;
            double sz = (validHeight(ex, ez - 24) + validHeight(ex, ez - 12) * 2.0 + edge * 4.0 + validHeight(ex, ez + 12) * 2.0 + validHeight(ex, ez + 24)) / 10.0;
            smoothEdge = (sx * Math.abs(nz) + sz * Math.abs(nx)) / (Math.abs(nx) + Math.abs(nz));
        }
        int ix = edgeX(ex - (int) Math.round(length == 0 ? 0 : nx / length * settings.contextDepth));
        int iz = edgeZ(ez - (int) Math.round(length == 0 ? 0 : nz / length * settings.contextDepth));
        double gradient = edgeWater ? 0 : clamp((edge - validHeight(ix, iz)) / (double) settings.contextDepth, -.65, .65);
        double base = lerp(edge, smoothEdge, smooth(d / 96)) + gradient * d * Math.exp(-d / 80);
        double warpX = x + fbm(x / 340.0, z / 340.0) * 70;
        double warpZ = z + fbm(x / 340.0 + 71, z / 340.0 - 29) * 70;
        double broad = fbm(warpX / 420, warpZ / 420);
        double ridge = 1 - Math.abs(fbm(warpX / 145 + 31, warpZ / 145 - 11));
        ridge *= ridge;
        double localWidth = width * (1 + fbm(x / 650.0 + 93, z / 650.0) * .85);
        double exponent = clamp(1 + fbm(warpX / 260 + 19, warpZ / 260 - 59) * .8, .65, 1.35);
        double progress = Math.pow(smooth(d / localWidth), exponent);
        double target = clamp(map.seaLevel() + settings.mountainOffset + broad * settings.broadRelief + ridge * settings.ridgeRelief,
                map.minY() + 8, map.minY() + map.worldHeight() - 13);
        double foothills = fbm(warpX / 65, warpZ / 65) * 20 * smooth(d / 100) * (1 - progress * .5);
        double terrain = lerp(base, target, progress) + foothills;
        int waterY = Integer.MIN_VALUE;
        if (edgeWater) {
            int shore = 32;
            for (int step = 1; step <= 32; step++) {
                if (!map.surfaceAt(edgeX(ex - ax * step), edgeZ(ez - az * step)).equals("minecraft:water")
                        || !map.surfaceAt(edgeX(ex + ax * step), edgeZ(ez + az * step)).equals("minecraft:water")) {
                    shore = step;
                    break;
                }
            }
            double reach = clamp(width * settings.waterReachFraction, 48, 192) * (.45 + .55 * smooth(shore / 24.0))
                    * (1 + noise(x / 80.0 + 17, z / 80.0) * .18);
            if (d < reach) {
                int depth = 3 + (int) Math.round((GensokyoRiverbed.depthAt(x, z) - 3) * smooth(d / 24));
                terrain = lerp(edge - depth, terrain, smooth((d / reach - .55) / .45));
                if (Math.round(terrain) < edge)
                    waterY = edge;
            }
        }
        int ground = (int) clamp(Math.round(terrain), map.minY() + 1, map.minY() + map.worldHeight() - 13);
        String surface;
        if (waterY > ground) {
            surface = switch (GensokyoRiverbed.materialAt(x, z)) {
                case 1 -> "minecraft:sand";
                case 2 -> "minecraft:clay";
                case 3 -> "minecraft:stone";
                default -> "minecraft:gravel";
            };
        } else {
            double carry = 1 - smooth(d / clamp(width * settings.surfaceBlendFraction, 48, 192));
            double patch = (noise(x / 13.0, z / 13.0) + 1) * .5;
            String inherited = substrate(edgeSurface);
            double snowLine = map.seaLevel() + (cold ? 20 : 300) + broad * 25;
            if (ground >= snowLine)
                surface = "minecraft:snow_block";
            else if (distance <= 1 || patch < carry)
                surface = inherited;
            else if (ground >= map.seaLevel() + 115 + broad * 45 || noise(x / 24.0 + 51, z / 24.0) > .55)
                surface = "minecraft:stone";
            else
                surface = "minecraft:grass_block";
        }
        String biome = biome(edgeBiome, edgeWater, cold, waterY > ground, ground, d, x, z);
        return new Column(ground, waterY, surface, biome);
    }

    private String biome(String edge, boolean edgeWater, boolean cold, boolean water, int height, double d, int x, int z) {
        if (water)
            return cold ? "minecraft:frozen_river" : "minecraft:river";
        double jitter = noise(x / 50.0 + 143, z / 50.0 - 71) * .07;
        double progress = d / width + jitter;
        if (progress < .22 && !edgeWater && terrestrial(edge))
            return edge;
        if (height > map.seaLevel() + 300)
            return "minecraft:frozen_peaks";
        if (height > map.seaLevel() + 285 || progress > .76)
            return cold ? "minecraft:frozen_peaks" : "minecraft:stony_peaks";
        if (height > map.seaLevel() + 150 || progress > .45)
            return cold ? "minecraft:snowy_slopes" : "minecraft:meadow";
        return cold ? "minecraft:grove" : "minecraft:windswept_forest";
    }

    private static boolean terrestrial(String biome) {
        return !(biome.contains("ocean") || biome.contains("river") || biome.contains("swamp") || biome.contains("nether")
                || biome.contains("end_") || biome.endsWith(":the_end") || biome.endsWith(":the_void"));
    }

    public static boolean cold(String biome) {
        return biome.contains("snow") || biome.contains("frozen") || biome.contains("ice_spikes") || biome.endsWith(":grove");
    }

    public static String substrate(String surface) {
        return switch (surface) {
            case "minecraft:stone", "minecraft:andesite", "minecraft:granite", "minecraft:diorite", "minecraft:gravel",
                 "minecraft:sand", "minecraft:red_sand", "minecraft:podzol", "minecraft:mycelium",
                 "minecraft:coarse_dirt",
                 "minecraft:rooted_dirt", "minecraft:snow_block", "minecraft:basalt", "minecraft:calcite",
                 "minecraft:moss_block" -> surface;
            case "minecraft:snow" -> "minecraft:snow_block";
            case "minecraft:lava" -> "minecraft:basalt";
            default -> "minecraft:grass_block";
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double smooth(double t) {
        t = clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static double hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / 8388607.5 - 1;
    }

    private static double noise(double x, double z) {
        int x0 = (int) Math.floor(x), z0 = (int) Math.floor(z);
        return lerp(lerp(hash(x0, z0), hash(x0 + 1, z0), smooth(x - x0)),
                lerp(hash(x0, z0 + 1), hash(x0 + 1, z0 + 1), smooth(x - x0)), smooth(z - z0));
    }

    private static double fbm(double x, double z) {
        double sum = 0, amplitude = 1, frequency = 1, total = 0;
        for (int i = 0; i < 4; i++) {
            sum += noise(x * frequency, z * frequency) * amplitude;
            total += amplitude;
            amplitude *= .5;
            frequency *= 2.03;
        }
        return sum / total;
    }
}
