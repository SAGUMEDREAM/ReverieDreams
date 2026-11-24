package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Additional, terrain-relative mining strata. Existing deep biome features remain
 * intact. These are explicit block-depth bands, never a scaled world-height range.
 * Actual veins use Minecraft's OreFeature and the registry's configured targets,
 * sizes and air-exposure rejection (including resource-pack overrides).
 */
public final class GensokyoOreGeneration {
    public static final int MAX_DEPTH = 320;
    private static final int SURFACE_COVER = 4;
    private static final Identifier MAPLE_FOREST = Identifier.parse(ReverieDreams.MOD_ID + ":maple_forest");

    enum Shape {UNIFORM, TRIANGLE, DEEP_RAMP, ALTITUDE_TRIANGLE}

    enum Restriction {OVERWORLD, MOUNTAIN, BADLANDS}

    record Band(String name, ResourceKey<ConfiguredFeature<?, ?>> feature,
                int minCount, int maxCount, int rarity, int low, int high,
                Shape shape, Restriction restriction) {
        int sampleY(RandomSource random, int solidTop, int seaLevel) {
            int width = this.high - this.low;
            int value = switch (this.shape) {
                case UNIFORM -> this.low + random.nextInt(width + 1);
                case TRIANGLE, ALTITUDE_TRIANGLE -> this.low
                        + random.nextInt(width / 2 + 1) + random.nextInt(width - width / 2 + 1);
                // Normalized increasing density, without sampling below a virtual floor.
                case DEEP_RAMP -> this.low + Math.max(random.nextInt(width + 1), random.nextInt(width + 1));
            };
            return this.shape == Shape.ALTITUDE_TRIANGLE ? seaLevel + value : solidTop - value;
        }

        int sampleCount(RandomSource random) {
            if (this.rarity > 1 && random.nextInt(this.rarity) != 0)
                return 0;
            return this.minCount + random.nextInt(this.maxCount - this.minCount + 1);
        }
    }

    // Counts are per source chunk, not per slice of the 1536-block dimension.
    // Coal/silver supplement their existing sparse upper-world placements;
    // diamonds use fewer attempts because every new origin has a valid stratum.
    static final List<Band> BANDS = List.of(
            band("coal_shallow", "minecraft:ore_coal", 24, 8, 128, Shape.UNIFORM),
            band("coal_buried", "minecraft:ore_coal_buried", 20, 48, 224, Shape.TRIANGLE),
            band("iron_middle", "minecraft:ore_iron", 10, 16, 160, Shape.TRIANGLE),
            band("iron_small", "minecraft:ore_iron_small", 10, 8, 256, Shape.UNIFORM),
            band("iron_highland", "minecraft:ore_iron", 90, 80, 384, Shape.ALTITUDE_TRIANGLE),
            band("copper", "minecraft:ore_copper_small", 16, 8, 144, Shape.TRIANGLE),
            band("gold", "minecraft:ore_gold_buried", 4, 112, 288, Shape.TRIANGLE),
            new Band("gold_deep", key("minecraft:ore_gold_buried"), 0, 1, 1, 280, 312, Shape.UNIFORM, Restriction.OVERWORLD),
            new Band("gold_badlands", key("minecraft:ore_gold"), 50, 50, 1, 24, 128, Shape.UNIFORM, Restriction.BADLANDS),
            band("redstone", "minecraft:ore_redstone", 4, 96, 224, Shape.UNIFORM),
            band("redstone_deep", "minecraft:ore_redstone", 8, 176, 312, Shape.DEEP_RAMP),
            band("diamond", "minecraft:ore_diamond_small", 3, 160, 312, Shape.DEEP_RAMP),
            band("diamond_medium", "minecraft:ore_diamond_medium", 2, 248, 312, Shape.UNIFORM),
            new Band("diamond_large", key("minecraft:ore_diamond_large"), 1, 1, 9, 160, 312, Shape.DEEP_RAMP, Restriction.OVERWORLD),
            band("diamond_buried", "minecraft:ore_diamond_buried", 2, 192, 312, Shape.DEEP_RAMP),
            band("lapis", "minecraft:ore_lapis", 2, 96, 256, Shape.TRIANGLE),
            band("lapis_buried", "minecraft:ore_lapis_buried", 4, 48, 288, Shape.UNIFORM),
            new Band("emerald_highland", key("minecraft:ore_emerald"), 100, 100, 1, -16, 480, Shape.ALTITUDE_TRIANGLE, Restriction.MOUNTAIN),
            band("silver", ReverieDreams.MOD_ID + ":overworld_silver_ore", 9, 8, 256, Shape.UNIFORM),
            band("orb", ReverieDreams.MOD_ID + ":overworld_orb_ore", 4, 192, 312, Shape.DEEP_RAMP)
    );

    private GensokyoOreGeneration() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String id) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.parse(id));
    }

    private static Band band(String name, String feature, int count, int low, int high, Shape shape) {
        return new Band(name, key(feature), count, count, 1, low, high, shape, Restriction.OVERWORLD);
    }

    /**
     * Below this level the supplemental pass must not change any blocks.
     */
    public static int miningFloor(GensokyoMapData map) {
        return Math.max(map.minY() + 1, map.seaLevel() - MAX_DEPTH);
    }

    static long decorationSeed(WorldgenRandom random, long worldSeed, ChunkPos chunk) {
        return random.setDecorationSeed(worldSeed, chunk.getMinBlockX(), chunk.getMinBlockZ());
    }

    static void seedBand(WorldgenRandom random, long decorationSeed, Band band) {
        // Stable identifiers make adding/reordering one band leave other bands unchanged.
        random.setFeatureSeed(decorationSeed, band.name.hashCode() ^ 0x47534F52, 6);
    }

    public static void place(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunk, GensokyoMapData map) {
        var features = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(0));
        long decorationSeed = decorationSeed(random, level.getSeed(), chunk);
        // Terrain is static during this pass, so column tops and biome verdicts repeat
        // across the hundreds of fitsVein probes. Both caches live for one chunk only.
        Long2IntOpenHashMap solidTops = new Long2IntOpenHashMap();
        solidTops.defaultReturnValue(Integer.MIN_VALUE);
        Long2ObjectOpenHashMap<TerrainRange> terrainRanges = new Long2ObjectOpenHashMap<>();
        Map<Band, Map<Holder<Biome>, Boolean>> biomeChecks = new HashMap<>();
        for (Band band : BANDS) {
            ConfiguredFeature<?, ?> feature = features.getOrThrow(band.feature).value();
            if (feature.feature() != Feature.ORE || !(feature.config() instanceof OreConfiguration config))
                continue;
            seedBand(random, decorationSeed, band);
            int count = band.sampleCount(random);
            for (int attempt = 0; attempt < count; attempt++) {
                int x = chunk.getMinBlockX() + random.nextInt(16);
                int z = chunk.getMinBlockZ() + random.nextInt(16);
                int top = cachedSolidTop(map, solidTops, x, z);
                if (top == GensokyoMapData.NO_SURFACE)
                    continue;
                int y = band.sampleY(random, top, map.seaLevel());
                BlockPos origin = new BlockPos(x, y, z);
                boolean allowed = biomeChecks.computeIfAbsent(band, ignored -> new HashMap<>())
                        .computeIfAbsent(level.getBiome(origin), biome -> allowsBiome(band, biome));
                if (!allowed)
                    continue;
                if (!fitsVein(map, x, y, z, config.size, solidTops, terrainRanges))
                    continue;
                // Reuse the real vanilla feature, rather than painting random ore pixels.
                feature.place(level, generator, random, origin);
            }
        }
    }

    private static int cachedSolidTop(GensokyoMapData map, Long2IntOpenHashMap cache, int x, int z) {
        long key = BlockPos.asLong(x, 0, z);
        int cached = cache.get(key);
        if (cached != Integer.MIN_VALUE)
            return cached;
        int top = solidTop(map, x, z);
        cache.put(key, top);
        return top;
    }

    static boolean allowsBiome(Band band, Holder<Biome> biome) {
        boolean gensokyoForest = false;
        for (GensokyoForestType type : GensokyoForestType.values()) {
            if (biome.unwrapKey().map(key -> key.equals(GensokyoForestBootstrap.biome(type))).orElse(false)) {
                gensokyoForest = true;
                break;
            }
        }
        if (!biome.is(BiomeTags.IS_OVERWORLD)
                && !biome.unwrapKey().map(k -> k.identifier().equals(MAPLE_FOREST)).orElse(false)
                && !gensokyoForest)
            return false;
        return switch (band.restriction) {
            case OVERWORLD -> true;
            case MOUNTAIN -> biome.value().getGenerationSettings().features().stream()
                    .flatMap(set -> set.stream()).anyMatch(f -> f.unwrapKey()
                            .map(k -> k.identifier().equals(Identifier.withDefaultNamespace("ore_emerald"))).orElse(false));
            case BADLANDS -> biome.is(BiomeTags.IS_BADLANDS);
        };
    }

    static int solidTop(GensokyoMapData map, int x, int z) {
        if (!map.contains(x, z))
            return GensokyoMapData.NO_SURFACE;
        int top = map.heightAt(x, z);
        var state = map.surfaceStateAt(x, z);
        if (top == GensokyoMapData.NO_SURFACE || state.isAir())
            return GensokyoMapData.NO_SURFACE;
        if (state.is(Blocks.WATER))
            return top - GensokyoRiverbed.depthAt(x, z,
                    (sx, sz) -> map.surfaceStateAt(sx, sz).is(Blocks.WATER),
                    (bankX, bankZ) -> map.contains(bankX, bankZ) && map.heightAt(bankX, bankZ) <= top);
        if (state.is(Blocks.LAVA))
            return top - GensokyoChunkGenerator.LAVA_DEPTH;
        return top;
    }

    /**
     * Conservative bounds around OreFeature's complete ellipsoid envelope.
     */
    static boolean fitsVein(GensokyoMapData map, int x, int y, int z, int size) {
        Long2IntOpenHashMap solidTops = new Long2IntOpenHashMap();
        solidTops.defaultReturnValue(Integer.MIN_VALUE);
        return fitsVein(map, x, y, z, size, solidTops, new Long2ObjectOpenHashMap<>());
    }

    private static boolean fitsVein(GensokyoMapData map, int x, int y, int z, int size, Long2IntOpenHashMap solidTops,
                                    Long2ObjectOpenHashMap<TerrainRange> terrainRanges) {
        int radius = Mth.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        int horizontal = Mth.ceil(size / 8.0F) + radius + 1;
        int vertical = radius + 3;
        int low = y - vertical;
        int high = y + vertical;
        if (low < miningFloor(map) || high >= map.minY() + map.worldHeight())
            return false;
        int center = cachedSolidTop(map, solidTops, x, z);
        if (center == GensokyoMapData.NO_SURFACE || low < center - MAX_DEPTH || high > center - SURFACE_COVER)
            return false;
        long key = BlockPos.asLong(x, horizontal, z);
        TerrainRange range = terrainRanges.get(key);
        if (range == null) {
            range = terrainRange(map, solidTops, x, z, horizontal);
            terrainRanges.put(key, range);
        }
        if (range.lowest() == GensokyoMapData.NO_SURFACE
                || low < range.highest() - MAX_DEPTH || high > range.lowest() - SURFACE_COVER)
            return false;
        // Protect explicit editor blocks even when a vein crosses a chunk edge.
        for (int cx = (x - horizontal) >> 4; cx <= (x + horizontal) >> 4; cx++) {
            for (int cz = (z - horizontal) >> 4; cz <= (z + horizontal) >> 4; cz++) {
                for (var block : map.undergroundAt(new ChunkPos(cx, cz))) {
                    if (block.x() >= x - horizontal && block.x() <= x + horizontal
                            && block.z() >= z - horizontal && block.z() <= z + horizontal
                            && block.y() >= low && block.y() <= high)
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * These two limits are equivalent to applying both depth constraints to every column.
     */
    private static TerrainRange terrainRange(GensokyoMapData map, Long2IntOpenHashMap solidTops, int x, int z, int radius) {
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int sx = x - radius; sx <= x + radius; sx++) {
            for (int sz = z - radius; sz <= z + radius; sz++) {
                int top = cachedSolidTop(map, solidTops, sx, sz);
                if (top == GensokyoMapData.NO_SURFACE)
                    return TerrainRange.MISSING;
                lowest = Math.min(lowest, top);
                highest = Math.max(highest, top);
            }
        }
        return new TerrainRange(lowest, highest);
    }

    private record TerrainRange(int lowest, int highest) {
        private static final TerrainRange MISSING = new TerrainRange(GensokyoMapData.NO_SURFACE, GensokyoMapData.NO_SURFACE);
    }
}
