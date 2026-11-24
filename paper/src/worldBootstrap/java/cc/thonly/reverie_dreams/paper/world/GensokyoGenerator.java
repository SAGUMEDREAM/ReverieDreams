package cc.thonly.reverie_dreams.paper.world;

import cc.thonly.reverie_dreams.paper.world.forest.GensokyoForestPopulator;
import cc.thonly.reverie_dreams.paper.world.forest.GensokyoForestType;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Terrain adapter only. No guessed ore veins, custom feature codecs or CE persistence are installed. */
public final class GensokyoGenerator extends ChunkGenerator {
    private final GensokyoMapCache cache;
    private final GensokyoTerrain terrain;
    private final GensokyoStateBindings<BlockData> bindings;
    private final Function<String, BlockData> nativeParser;
    private final GensokyoStateBindings<BlockData> featureBindings;
    private final boolean nativeDecoration;
    private final GensokyoFeaturePopulator featurePopulator;
    private final GensokyoForestPopulator forestPopulator;
    private final Map<Integer, Biome> biomes;
    private final List<String> issues;

    public GensokyoGenerator(GensokyoMapCache cache) {
        this(cache, false, null);
    }
    public GensokyoGenerator(GensokyoMapCache cache, boolean nativeDecoration, GensokyoOreFeatures oreFeatures) {
        this(cache, nativeDecoration, oreFeatures, oreFeatures != null, false);
    }
    public GensokyoGenerator(GensokyoMapCache cache, boolean nativeDecoration, GensokyoOreFeatures oreFeatures,
                             boolean terrainRelativeOres, boolean sourceOrePlacements) {
        this(cache, nativeDecoration, oreFeatures, terrainRelativeOres, sourceOrePlacements, false);
    }
    public GensokyoGenerator(GensokyoMapCache cache, boolean nativeDecoration, GensokyoOreFeatures oreFeatures,
                             boolean terrainRelativeOres, boolean sourceOrePlacements, boolean sourceForestTrees) {
        this(cache, Bukkit::createBlockData, id -> {
            NamespacedKey key = NamespacedKey.fromString(id);
            return key == null ? null : Registry.BIOME.get(key);
        }, nativeDecoration, oreFeatures, terrainRelativeOres, sourceOrePlacements, sourceForestTrees);
    }

    GensokyoGenerator(GensokyoMapCache cache, Function<String, BlockData> nativeParser, Function<String, Biome> biomeResolver) {
        this(cache, nativeParser, biomeResolver, false, null);
    }
    GensokyoGenerator(GensokyoMapCache cache, Function<String, BlockData> nativeParser, Function<String, Biome> biomeResolver,
                      boolean nativeDecoration, GensokyoOreFeatures oreFeatures) {
        this(cache, nativeParser, biomeResolver, nativeDecoration, oreFeatures, oreFeatures != null, false);
    }
    GensokyoGenerator(GensokyoMapCache cache, Function<String, BlockData> nativeParser, Function<String, Biome> biomeResolver,
                      boolean nativeDecoration, GensokyoOreFeatures oreFeatures, boolean terrainRelativeOres, boolean sourceOrePlacements) {
        this(cache, nativeParser, biomeResolver, nativeDecoration, oreFeatures, terrainRelativeOres, sourceOrePlacements, false);
    }
    GensokyoGenerator(GensokyoMapCache cache, Function<String, BlockData> nativeParser, Function<String, Biome> biomeResolver,
                      boolean nativeDecoration, GensokyoOreFeatures oreFeatures, boolean terrainRelativeOres,
                      boolean sourceOrePlacements, boolean sourceForestTrees) {
        this.cache = cache;
        this.terrain = new GensokyoTerrain(cache);
        this.nativeParser = nativeParser;
        if ((terrainRelativeOres || sourceOrePlacements) != (oreFeatures != null)) throw new IllegalArgumentException("Enabled ore stages require an explicit catalog");
        this.nativeDecoration = nativeDecoration;
        var available = new HashMap<String, BlockData>();
        var problems = new LinkedHashSet<String>();
        var requested = new LinkedHashSet<>(List.of("minecraft:air", "minecraft:barrier", "minecraft:bedrock", "minecraft:stone",
                "minecraft:dirt", "minecraft:grass_block", "minecraft:snow_block", "minecraft:water", "minecraft:lava",
                "minecraft:gravel", "minecraft:sand", "minecraft:clay"));
        var counts = cache.manifest().getAsJsonArray("surface_counts");
        var surfaceCounts = new ArrayList<Long>(counts.size());
        for (var count : counts) surfaceCounts.add(count.getAsLong());
        Set<String> customSurfaces = GensokyoStateBindings.customSurfaces(cache.map().surfacePalette(), surfaceCounts);
        for (int i = 0; i < counts.size(); i++) if (counts.get(i).getAsLong() > 0) requested.add(cache.map().surfacePalette().get(i));
        cache.map().underground().values().forEach(list -> list.forEach(block -> requested.add(block.state())));
        for (String source : requested) {
            // Source logical IDs are never parsed as a guessed native mapping.
            // Custom surfaces enter only through the verified post-load API.
            if (!source.startsWith("minecraft:")) continue;
            try { available.put(source, nativeParser.apply(source)); }
            catch (IllegalArgumentException error) { /* Reported dynamically by the state table. */ }
        }
        this.bindings = new GensokyoStateBindings<>(requested, customSurfaces, available);
        Set<String> featureRequested = oreFeatures == null ? Set.of() : oreFeatures.states();
        var featureNative = new HashMap<String, BlockData>();
        for (String state : featureRequested) if (state.startsWith("minecraft:")) {
            try { featureNative.put(state, nativeParser.apply(state)); }
            catch (IllegalArgumentException error) { /* Reported by the feature table. */ }
        }
        Set<String> customFeatures = featureRequested.stream().filter(state -> !state.startsWith("minecraft:")).collect(Collectors.toUnmodifiableSet());
        this.featureBindings = new GensokyoStateBindings<>(featureRequested, customFeatures, featureNative);
        this.featurePopulator = nativeDecoration || oreFeatures != null || sourceForestTrees
                ? new GensokyoFeaturePopulator(cache, oreFeatures, featureBindings::snapshot, bindings::snapshot, terrainRelativeOres, sourceOrePlacements) : null;
        var biomeValues = new LinkedHashMap<Integer, Biome>();
        var biomeCounts = cache.manifest().getAsJsonArray("biome_counts");
        for (int i = 0; i < biomeCounts.size(); i++) {
            if (biomeCounts.get(i).getAsLong() == 0) continue;
            String id = cache.map().biomePalette().get(i);
            Biome biome = biomeResolver.apply(id);
            if (biome == null) problems.add("Unresolved source biome: " + id);
            else biomeValues.put(i, biome);
        }
        this.biomes = Map.copyOf(biomeValues);
        var activeForests = new LinkedHashSet<GensokyoForestType>();
        for (int i = 0; i < biomeCounts.size(); i++) if (biomeCounts.get(i).getAsLong() > 0) {
            String id = cache.map().biomePalette().get(i);
            for (var type : GensokyoForestType.values()) if (id.equals("reverie_dreams:" + type.biomePath())) activeForests.add(type);
        }
        // Current active source types use native oak/birch/cherry. A future active
        // maple requires explicit CE property bindings; never parse its logical IR as vanilla.
        var samplers = new ConcurrentHashMap<Long, GensokyoBiomeSampling>();
        this.forestPopulator = activeForests.isEmpty() ? null : new GensokyoForestPopulator(sourceForestTrees,
                Set.copyOf(activeForests), (seed, x, y, z) -> {
                    var quart = samplers.computeIfAbsent(seed, GensokyoBiomeSampling::new).quart(x, y, z);
                    return cache.map().biomePalette().get(terrain.biomeIndex(quart.x() << 2, quart.z() << 2));
                }, state -> {
                    if (!state.startsWith("minecraft:")) throw new IllegalStateException("Unbound source forest state " + state);
                    return nativeParser.apply(state);
                });
        if (cache.map().anchorCount() != 0) problems.add("Authored structure anchors are not implemented: " + cache.map().anchorCount());
        this.issues = List.copyOf(problems);
    }

    public List<String> unresolved() {
        var unresolved = new ArrayList<>(issues);
        bindings.unresolved().forEach(source -> unresolved.add("Unresolved source blockstate: " + source));
        featureBindings.unresolved().forEach(source -> unresolved.add("Unresolved feature blockstate: " + source));
        return List.copyOf(unresolved);
    }
    public Set<String> requestedCustomStates() { return bindings.requestedCustomStates(); }
    public Set<String> requestedFeatureStates() { return featureBindings.requestedCustomStates(); }
    void installVerifiedBindings(Map<String, String> sourceToNativeEncoded) {
        bindings.installVerifiedBindings(sourceToNativeEncoded, nativeParser);
    }
    void installInitialBindings(Function<Set<String>, Map<String, String>> resolver) {
        var union = new LinkedHashSet<>(requestedCustomStates());
        union.addAll(requestedFeatureStates());
        Map<String, String> resolved = Map.copyOf(resolver.apply(Set.copyOf(union)));
        if (!union.containsAll(resolved.keySet())) throw new IllegalArgumentException("Resolver returned a state outside the source/feature requests");
        var surface = new HashMap<String, String>();
        var feature = new HashMap<String, String>();
        resolved.forEach((source, nativeState) -> {
            if (requestedCustomStates().contains(source)) surface.put(source, nativeState);
            if (requestedFeatureStates().contains(source)) feature.put(source, nativeState);
        });
        installVerifiedBindings(surface);
        installVerifiedFeatureBindings(feature);
    }
    void installVerifiedFeatureBindings(Map<String, String> sourceToNativeEncoded) { featureBindings.installVerifiedBindings(sourceToNativeEncoded, nativeParser); }
    public GensokyoTerrain terrain() { return terrain; }
    public boolean sourceForestTreesEnabled() { return forestPopulator != null && forestPopulator.enabled(); }
    public long sourceForestTreesGenerated() { return forestPopulator == null ? 0 : forestPopulator.generatedTrees(); }
    public GensokyoForestPopulator.Sample sourceForestSample() { return forestPopulator == null ? null : forestPopulator.lastGeneratedSample(); }
    public String sourceForestStatus() { return forestPopulator == null ? "No source custom forest occurs in this map" : forestPopulator.limitation(); }

    @Override public void generateNoise(WorldInfo info, Random random, int chunkX, int chunkZ, ChunkData data) {
        Map<String, BlockData> snapshot = bindings.snapshot();
        if (!featureBindings.unresolved().isEmpty()) throw new IllegalStateException("Feature outputs are not verified; refusing terrain before any write: " + featureBindings.unresolved());
        requireHeight(info);
        if (data.getMinHeight() != 0 || data.getMaxHeight() != 1536)
            throw new IllegalStateException("ChunkData height mismatch; refusing to clip/rescale source terrain");
        if (cache.map().anchorCount() != 0) throw new IllegalStateException("Unimplemented authored anchors; refusing terrain generation");
        var columns = new ArrayList<List<GensokyoTerrain.Run>>(256);
        // Validate every state in this chunk before its first write. Unresolved
        // paddy is an explicit failure, never air/farmland silently substituted.
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            List<GensokyoTerrain.Run> runs = terrain.column((chunkX << 4) + x, (chunkZ << 4) + z);
            for (var run : runs) requireState(snapshot, run.state());
            columns.add(runs);
        }
        var overrides = cache.map().underground().getOrDefault(GensokyoMap.chunkKey(chunkX, chunkZ), List.of());
        for (var block : overrides) requireState(snapshot, block.state());
        int index = 0;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            for (var run : columns.get(index++)) data.setRegion(x, run.minY(), z, x + 1, run.maxYExclusive(), z + 1, requireState(snapshot, run.state()));
        }
        for (var block : overrides) data.setBlock(block.x() - (chunkX << 4), block.y(), block.z() - (chunkZ << 4), requireState(snapshot, block.state()));
    }

    @Override public BiomeProvider getDefaultBiomeProvider(WorldInfo info) {
        requireHeight(info);
        return new BiomeProvider() {
            @Override public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                Biome biome = biomes.get(terrain.biomeIndex(x, z));
                if (biome == null) throw new IllegalStateException("Unresolved biome at source coordinates " + x + "," + z);
                return biome;
            }
            @Override public List<Biome> getBiomes(WorldInfo worldInfo) { return biomes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).distinct().toList(); }
        };
    }
    @Override public int getBaseHeight(WorldInfo info, Random random, int x, int z, HeightMap heightMap) {
        requireHeight(info);
        String type = heightMap.name();
        return terrain.baseHeight(x, z, type.equals("WORLD_SURFACE") || type.equals("WORLD_SURFACE_WG"),
                type.equals("OCEAN_FLOOR") || type.equals("OCEAN_FLOOR_WG"));
    }
    @Override public Location getFixedSpawnLocation(World world, Random random) {
        requireHeight(world);
        var spawn = cache.map().spawn();
        return new Location(world, spawn.x() + 0.5, spawn.y(), spawn.z() + 0.5, spawn.yaw(), spawn.pitch());
    }
    @Override public boolean isParallelCapable() { return true; }
    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateBedrock() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return nativeDecoration; }
    @Override public List<BlockPopulator> getDefaultPopulators(World world) {
        var stages = new ArrayList<BlockPopulator>();
        if (forestPopulator != null && forestPopulator.enabled()) stages.add(forestPopulator);
        if (featurePopulator != null) stages.add(featurePopulator);
        return List.copyOf(stages);
    }
    @Override public boolean shouldGenerateMobs() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }

    private static BlockData requireState(Map<String, BlockData> snapshot, String state) {
        BlockData value = snapshot.get(state);
        if (value == null) throw new IllegalStateException("Unresolved source state " + state + "; refusing chunk before any write. No CE/startup fallback is assumed.");
        return value;
    }
    private void requireHeight(WorldInfo info) {
        if (info.getMinHeight() != cache.map().minY() || info.getMaxHeight() != cache.map().minY() + cache.map().height())
            throw new IllegalStateException("Datapack dimension mismatch: expected [0,1536), got [" + info.getMinHeight() + "," + info.getMaxHeight() + "). No clipping/rescaling is permitted.");
    }
}
