package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 按固定地图的 biome 层返回群系；坐标输入是 4×4 的 quart 坐标。
 */
public class GensokyoBiomeSource extends BiomeSource {
    public static final MapCodec<GensokyoBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("map").forGetter(GensokyoBiomeSource::mapId),
            Biome.CODEC.listOf().fieldOf("biomes").forGetter(GensokyoBiomeSource::biomes),
            RegistryOps.<Biome, GensokyoBiomeSource>retrieveGetter(Registries.BIOME)
    ).apply(instance, GensokyoBiomeSource::fromSavedPalette));

    private static GensokyoBiomeSource fromSavedPalette(Identifier id, List<Holder<Biome>> saved, HolderGetter<Biome> lookup) {
        if (!id.equals(GensokyoChunkGenerator.DEFAULT_MAP))
            return new GensokyoBiomeSource(id, saved);
        var palette = GensokyoMapLoader.get(id).biomePalette();
        return new GensokyoBiomeSource(id, upgradeLegacyJungle(id, saved, palette,
                lookup.getOrThrow(RDBuiltinBiomes.GENSOKYO_JUNGLE)));
    }

    /**
     * Old level.dat lists retain their slot numbers; only an authored jungle replacement is upgraded.
     */
    static List<Holder<Biome>> upgradeLegacyJungle(Identifier id, List<Holder<Biome>> saved,
                                                   List<Identifier> current, Holder<Biome> jungle) {
        if (!id.equals(GensokyoChunkGenerator.DEFAULT_MAP))
            return saved;
        var result = new ArrayList<>(saved);
        for (int i = 0; i < Math.min(saved.size(), current.size()); i++) {
            Holder<Biome> old = saved.get(i);
            if (current.get(i).equals(RDBuiltinBiomes.GENSOKYO_JUNGLE.identifier())
                    && (old.is(Biomes.JUNGLE) || old.is(Biomes.SPARSE_JUNGLE) || old.is(Biomes.BAMBOO_JUNGLE))) {
                result.set(i, jungle);
            }
        }
        return List.copyOf(result);
    }

    private final Identifier mapId;
    private final List<Holder<Biome>> biomes;
    private final ConcurrentHashMap<String, Integer> borderBiomeIndexes = new ConcurrentHashMap<>();
    private volatile GensokyoMapData mapData;
    private volatile GensokyoBorderTerrain borderTerrain;

    public GensokyoBiomeSource(Identifier mapId, List<Holder<Biome>> biomes) {
        if (biomes.isEmpty()) {
            throw new IllegalArgumentException("Gensokyo biome source needs at least one biome");
        }
        this.mapId = mapId;
        this.biomes = List.copyOf(biomes);
    }

    public Identifier mapId() {
        return this.mapId;
    }

    public List<Holder<Biome>> biomes() {
        return this.biomes;
    }

    /**
     * 群系填充按方块列遍历 96 个 section，但本群系源与 y 无关；缓存同一 quart 列的结果。
     */
    static BiomeResolver cachedResolver(GensokyoBiomeSource source) {
        Long2ObjectOpenHashMap<Holder<Biome>> columns = new Long2ObjectOpenHashMap<>(64);
        return (x, y, z, sampler) -> {
            long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
            Holder<Biome> cached = columns.get(key);
            if (cached == null) {
                cached = source.getNoiseBiome(x, y, z, sampler);
                columns.put(key, cached);
            }
            return cached;
        };
    }

    private GensokyoMapData map() {
        GensokyoMapData data = this.mapData;
        if (data == null) {
            data = GensokyoMapLoader.get(this.mapId);
            this.mapData = data;
        }
        return data;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.biomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        GensokyoMapData map = this.map();
        if (!map.contains(x * 4, z * 4)) {
            var border = this.borderTerrain;
            if (border == null)
                this.borderTerrain = border = new GensokyoBorderTerrain(GensokyoBorderMapView.of(map));
            var column = border.sample(x * 4, z * 4);
            int index = this.borderBiomeIndexes.computeIfAbsent(column.biome(),
                    id -> map.biomePalette().indexOf(Identifier.parse(id)));
            if (index < 0) {
                int edgeX = Math.clamp(x * 4, map.originX(), map.originX() + map.width() - 1);
                int edgeZ = Math.clamp(z * 4, map.originZ(), map.originZ() + map.depth() - 1);
                index = map.biomeIndexAt(edgeX, edgeZ);
            }
            return this.biomes.get(Math.floorMod(index, this.biomes.size()));
        }
        int index = map.biomeIndexAt(x * 4, z * 4);
        int transition = map.biomeTransition();
        if (transition > 0) {
            int dx = transitionOffset(x, z, 0, transition);
            int dz = transitionOffset(x, z, 1, transition);
            int sampleX = x * 4 + dx;
            int sampleZ = z * 4 + dz;
            if (map.contains(sampleX, sampleZ)) {
                index = map.biomeIndexAt(sampleX, sampleZ);
            }
        }
        return this.biomes.get(Math.floorMod(index, this.biomes.size()));
    }

    /**
     * 固定地图维度忽略原版 6400 格搜索半径，直接在地图范围内按距离由近到远查找，
     * 保证 /locate biome 的结果不会落在计划地图之外。
     */
    @Override
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos origin, int radius, int horizontalRes, int verticalRes,
                                                            Predicate<Holder<Biome>> allowed, Climate.Sampler sampler,
                                                            LevelReader level) {
        Set<Holder<Biome>> matches = this.possibleBiomes().stream()
                .filter(allowed)
                .collect(Collectors.toUnmodifiableSet());
        if (matches.isEmpty()) {
            return null;
        }
        GensokyoMapData map = this.map();
        int minX = map.originX();
        int minZ = map.originZ();
        int maxX = minX + map.width() - 1;
        int maxZ = minZ + map.depth() - 1;
        int step = Math.max(1, horizontalRes);
        int centerX = Math.clamp(origin.getX(), minX, maxX);
        int centerZ = Math.clamp(origin.getZ(), minZ, maxZ);
        int maxRing = Math.max(Math.max(centerX - minX, maxX - centerX),
                Math.max(centerZ - minZ, maxZ - centerZ)) / step + 1;

        Pair<BlockPos, Holder<Biome>> hit = this.locateBiomeAt(centerX, centerZ, matches, sampler, map);
        if (hit != null) {
            return hit;
        }
        for (int ring = 1; ring <= maxRing; ring++) {
            int offset = ring * step;
            for (int k = -offset; k <= offset; k += step) {
                hit = this.locateBiomeAt(centerX + k, centerZ - offset, matches, sampler, map);
                if (hit == null) {
                    hit = this.locateBiomeAt(centerX + k, centerZ + offset, matches, sampler, map);
                }
                if (hit != null) {
                    return hit;
                }
            }
            for (int k = -offset + step; k <= offset - step; k += step) {
                hit = this.locateBiomeAt(centerX - offset, centerZ + k, matches, sampler, map);
                if (hit == null) {
                    hit = this.locateBiomeAt(centerX + offset, centerZ + k, matches, sampler, map);
                }
                if (hit != null) {
                    return hit;
                }
            }
        }
        return null;
    }

    private Pair<BlockPos, Holder<Biome>> locateBiomeAt(int x, int z, Set<Holder<Biome>> matches,
                                                        Climate.Sampler sampler, GensokyoMapData map) {
        if (!map.contains(x, z)) {
            return null;
        }
        Holder<Biome> biome = this.getNoiseBiome(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z), sampler);
        if (!matches.contains(biome)) {
            return null;
        }
        int surfaceY = map.heightAt(x, z);
        int y = surfaceY == GensokyoMapData.NO_SURFACE ? map.seaLevel() : surfaceY + 1;
        int maxY = map.minY() + map.worldHeight() - 1;
        return Pair.of(new BlockPos(x, Math.clamp(y, map.minY(), maxY), z), biome);
    }

    private static int transitionOffset(int x, int z, int seed, int width) {
        int span = width * 2 + 1;
        return Math.floorMod(hash32(x, z, seed), span) - width;
    }

    private static int hash32(int x, int z, int seed) {
        int h = x * 374761393 + z * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }
}
