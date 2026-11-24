package cc.thonly.reverie_dreams.paper.world;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.ArrayList;

/** Runs after Paper's native biome decorations, then restores authored underground data. */
final class GensokyoFeaturePopulator extends BlockPopulator {
    private final GensokyoMapCache cache;
    private final GensokyoOreFeatures catalog;
    private final GensokyoOreGenerator ores;
    private final GensokyoTerrain terrain;
    private final boolean terrainRelativeOres, sourceOrePlacements;
    private final Supplier<Map<String, BlockData>> featureStates, terrainStates;

    GensokyoFeaturePopulator(GensokyoMapCache cache, GensokyoOreFeatures catalog,
                            Supplier<Map<String, BlockData>> featureStates, Supplier<Map<String, BlockData>> terrainStates,
                            boolean terrainRelativeOres, boolean sourceOrePlacements) {
        this.cache = cache; this.catalog = catalog;
        this.ores = catalog == null ? null : new GensokyoOreGenerator(cache, catalog);
        this.terrain = new GensokyoTerrain(cache);
        this.terrainRelativeOres = terrainRelativeOres; this.sourceOrePlacements = sourceOrePlacements;
        this.featureStates = featureStates; this.terrainStates = terrainStates;
    }

    @Override public void populate(WorldInfo info, Random ignored, int chunkX, int chunkZ, LimitedRegion region) {
        if (info.getMinHeight() != 0 || info.getMaxHeight() != 1536) throw new IllegalStateException("Feature stage requires original1536 height");
        Map<String, BlockData> targets = featureStates.get(), terrainSnapshot = terrainStates.get();
        // A neighboring decoration stage may write across its owner boundary.
        // Reassert authored priority over this stage's complete local write region,
        // without scheduling a runtime repair task or scanning the whole map.
        var overrides = new ArrayList<GensokyoMap.Override>();
        for (int cx = chunkX - 1; cx <= chunkX + 1; cx++) for (int cz = chunkZ - 1; cz <= chunkZ + 1; cz++)
            for (var block : cache.map().underground().getOrDefault(GensokyoMap.chunkKey(cx, cz), List.of()))
                if (region.isInRegion(block.x(), block.y(), block.z())) overrides.add(block);
        for (var block : overrides) if (!terrainSnapshot.containsKey(block.state())) throw new IllegalStateException("Unbound authored underground state " + block.state());
        if (ores != null) {
            for (String state : catalog.states()) if (!targets.containsKey(state)) throw new IllegalStateException("Unbound feature state " + state);
            int horizontal = catalog.features().values().stream().mapToInt(f -> GensokyoOreGenerator.horizontalEnvelope(f.size())).max().orElse(0) + 1;
            int minX = (chunkX << 4) - horizontal, minZ = (chunkZ << 4) - horizontal;
            int maxX = (chunkX << 4) + 15 + horizontal, maxZ = (chunkZ << 4) + 15 + horizontal;
            if (!region.isInRegion(minX, 0, minZ) || !region.isInRegion(maxX, 1535, maxZ))
                throw new IllegalStateException("LimitedRegion cannot contain complete ore envelopes; refusing clipped veins");
            Map<String, Set<String>> tags = new HashMap<>();
            for (String key : catalog.tags()) {
                Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.fromString(key), Material.class);
                if (tag == null) throw new IllegalStateException("Missing native ore replacement tag " + key);
                tags.put(key, tag.getValues().stream().map(material -> material.getKey().toString()).collect(Collectors.toUnmodifiableSet()));
            }
            var biomeSampling = new GensokyoBiomeSampling(info.getSeed());
            var accessor = new GensokyoOreGenerator.Region() {
                public String state(int x, int y, int z) { return region.getBlockData(x, y, z).getAsString(); }
                public void set(int x, int y, int z, String state) { region.setBlockData(x, y, z, targets.get(state)); }
                public boolean matches(GensokyoOreFeatures.Target target, String current) {
                    String key = kind(current);
                    return target.predicate().equals("minecraft:block_match") ? target.match().equals(key) : tags.get(target.match()).contains(key);
                }
                public boolean air(int x, int y, int z) {
                    if (y < 0 || y >= 1536) return true;
                    String state = kind(state(x, y, z));
                    return state.equals("minecraft:air") || state.equals("minecraft:cave_air") || state.equals("minecraft:void_air");
                }
                public boolean canWrite(int x, int y, int z) { return y >= 0 && y < 1536 && region.isInRegion(x, y, z); }
                public int oceanFloor(int x, int z) { return region.getHighestBlockYAt(x, z, HeightMap.OCEAN_FLOOR_WG); }
                public String biome(int x, int y, int z) {
                    var quart = biomeSampling.quart(x, y, z);
                    return cache.map().biomePalette().get(terrain.biomeIndex(quart.x() << 2, quart.z() << 2));
                }
            };
            if (sourceOrePlacements) ores.populateSourcePlacements(info.getSeed(), chunkX, chunkZ, accessor);
            if (terrainRelativeOres) ores.populate(info.getSeed(), chunkX, chunkZ, accessor);
        }
        for (var block : overrides) region.setBlockData(block.x(), block.y(), block.z(), terrainSnapshot.get(block.state()));
    }
    private static String kind(String state) { int bracket = state.indexOf('['); return bracket < 0 ? state : state.substring(0, bracket); }
}
