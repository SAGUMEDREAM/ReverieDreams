package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoBorderGenerationTest {
    @BeforeAll static void bootstrap() throws Exception { GensokyoOreGenerationTest.bootstrap(); }
    static GensokyoChunkGenerator generator(GensokyoMapData map) throws Exception {
        var generator = new GensokyoChunkGenerator(new FixedBiomeSource(GensokyoOreGenerationTest.plains), map.id());
        var field = GensokyoChunkGenerator.class.getDeclaredField("mapData");
        field.setAccessible(true); field.set(generator, map);
        return generator;
    }
    @Test void createStateDisablesRandomStructureSetsForAuthoredMap() throws Exception {
        var map = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        var generator = generator(map);
        var state = generator.createState(null, randomState(), 42L);
        assertTrue(state.possibleStructureSets().isEmpty());
    }
    @Test void actualChunkFillingHeightmapsAndBaseQueriesAgreeAcrossAllSidesAndWater() throws Exception {
        var accessor = LevelHeightAccessor.create(0, 1536);
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        for (int kind : new int[]{0, 1}) {
            var map = GensokyoOreGenerationTest.map(1050, kind, Map.of());
            var before = map.heights().clone();
            var generator = generator(map);
            for (var position : List.of(new ChunkPos(-1, 4), new ChunkPos(8, 4), new ChunkPos(4, -1), new ChunkPos(4, 8), new ChunkPos(-1, -1))) {
                var sections = new LevelChunkSection[96];
                for (int i = 0; i < sections.length; i++) sections[i] = new LevelChunkSection(
                        new PalettedContainer<>(Blocks.AIR.defaultBlockState(), factory.blockStatesStrategy()), factory.createForBiomes());
                var chunk = new ProtoChunk(position, UpgradeData.EMPTY, sections, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), accessor, factory, null);
                generator.fillFromNoise(null, null, null, chunk).join();
                for (int lx = 0; lx < 16; lx += 3) for (int lz = 0; lz < 16; lz += 3) {
                    int x = position.getMinBlockX() + lx, z = position.getMinBlockZ() + lz;
                    var c = generator.borderTerrain().sample(x, z);
                    var base = generator.getBaseColumn(x, z, accessor, null);
                    for (var type : List.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG)) {
                        assertEquals(chunk.getOrCreateHeightmapUnprimed(type).getFirstAvailable(lx, lz), generator.getBaseHeight(x, z, type, accessor, null));
                    }
                    for (int y : new int[]{0, c.groundY() - 1, c.groundY(), c.visibleY(), c.visibleY() + 1, 1535}) {
                        var state = chunk.getBlockState(new BlockPos(x, y, z));
                        assertEquals(state, base.getBlock(y));
                        assertFalse(state.is(Blocks.BARRIER));
                    }
                    assertTrue(base.getBlock(c.visibleY() + 1).isAir());
                    if (c.water()) for (int y = c.groundY() + 1; y <= c.waterY(); y++) assertTrue(base.getBlock(y).is(Blocks.WATER));
                }
            }
            assertArrayEquals(before, map.heights());
        }
    }
    @Test void authoredVoidRemainsEmptyInsteadOfBeingFilledAsIfItWereOutside() throws Exception {
        var map = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        map.heights()[map.index(64, 64)] = Short.MIN_VALUE;
        var generator = generator(map);
        var accessor = LevelHeightAccessor.create(0, 1536);
        assertTrue(generator.getBaseColumn(64, 64, accessor, null).getBlock(1024).isAir());
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        var sections = new LevelChunkSection[96];
        for (int i = 0; i < sections.length; i++) sections[i] = new LevelChunkSection(
                new PalettedContainer<>(Blocks.AIR.defaultBlockState(), factory.blockStatesStrategy()), factory.createForBiomes());
        var chunk = new ProtoChunk(new ChunkPos(4, 4), UpgradeData.EMPTY, sections, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), accessor, factory, null);
        generator.fillFromNoise(null, null, null, chunk).join();
        assertTrue(chunk.getBlockState(new BlockPos(64, 1024, 64)).isAir());
        assertTrue(chunk.getBlockState(new BlockPos(65, 1050, 64)).is(Blocks.GRASS_BLOCK));
    }
    @Test void interiorHeightmapsMatchFullColumnBlockScan() throws Exception {
        var override = new GensokyoMapData.UndergroundBlock(80, 1100, 80, Blocks.STONE.defaultBlockState());
        var map = GensokyoOreGenerationTest.map(1084, 0, Map.of(new ChunkPos(5, 5).pack(), List.of(override)));
        map.surfaces()[map.index(50, 50)] = 1;
        map.surfaces()[map.index(60, 60)] = 2;
        map.heights()[map.index(70, 70)] = 1010;
        map.heights()[map.index(90, 90)] = GensokyoMapData.NO_SURFACE;
        var generator = generator(map);
        var accessor = LevelHeightAccessor.create(0, 1536);
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        var chunks = new HashMap<ChunkPos, ProtoChunk>();
        for (var position : List.of(new ChunkPos(3, 3), new ChunkPos(4, 4), new ChunkPos(5, 5))) {
            var sections = new LevelChunkSection[96];
            for (int i = 0; i < sections.length; i++) sections[i] = new LevelChunkSection(
                    new PalettedContainer<>(Blocks.AIR.defaultBlockState(), factory.blockStatesStrategy()), factory.createForBiomes());
            var chunk = new ProtoChunk(position, UpgradeData.EMPTY, sections, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), accessor, factory, null);
            generator.fillFromNoise(null, null, null, chunk).join();
            chunks.put(position, chunk);
            var pos = new BlockPos.MutableBlockPos();
            int minY = chunk.getMinY(), maxY = minY + chunk.getHeight() - 1;
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int expectedSurface = minY, expectedFloor = minY;
                    boolean surfaceFound = false, floorFound = false;
                    for (int y = maxY; y >= minY && !(surfaceFound && floorFound); y--) {
                        var state = chunk.getBlockState(pos.set(position.getMinBlockX() + lx, y, position.getMinBlockZ() + lz));
                        if (!surfaceFound && !state.isAir()) {
                            expectedSurface = y + 1;
                            surfaceFound = true;
                        }
                        if (!floorFound && Heightmap.Types.OCEAN_FLOOR_WG.isOpaque().test(state)) {
                            expectedFloor = y + 1;
                            floorFound = true;
                        }
                    }
                    assertEquals(expectedSurface, chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG).getFirstAvailable(lx, lz),
                            "world surface at " + position.getMinBlockX() + lx + "," + position.getMinBlockZ() + lz);
                    assertEquals(expectedFloor, chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG).getFirstAvailable(lx, lz),
                            "ocean floor at " + position.getMinBlockX() + lx + "," + position.getMinBlockZ() + lz);
                }
            }
        }
        var waterLava = chunks.get(new ChunkPos(3, 3));
        assertEquals(1085, height(waterLava, Heightmap.Types.WORLD_SURFACE_WG, 50, 50));
        assertEquals(1084, height(waterLava, Heightmap.Types.OCEAN_FLOOR_WG, 50, 50));
        assertEquals(1085, height(waterLava, Heightmap.Types.WORLD_SURFACE_WG, 60, 60));
        assertEquals(989, height(waterLava, Heightmap.Types.OCEAN_FLOOR_WG, 60, 60));
        var belowSea = chunks.get(new ChunkPos(4, 4));
        assertEquals(1025, height(belowSea, Heightmap.Types.WORLD_SURFACE_WG, 70, 70));
        assertEquals(1011, height(belowSea, Heightmap.Types.OCEAN_FLOOR_WG, 70, 70));
        var overridden = chunks.get(new ChunkPos(5, 5));
        assertEquals(1101, height(overridden, Heightmap.Types.WORLD_SURFACE_WG, 80, 80));
        assertEquals(1101, height(overridden, Heightmap.Types.OCEAN_FLOOR_WG, 80, 80));
        assertEquals(0, height(overridden, Heightmap.Types.WORLD_SURFACE_WG, 90, 90));
        assertEquals(0, height(overridden, Heightmap.Types.OCEAN_FLOOR_WG, 90, 90));
    }

    private static int height(ChunkAccess chunk, Heightmap.Types type, int worldX, int worldZ) {
        return chunk.getOrCreateHeightmapUnprimed(type).getFirstAvailable(worldX & 15, worldZ & 15);
    }

    @Test void stoneSectionsKeepBiomesLightingAndEditorOverrides() throws Exception {
        var position = new ChunkPos(3, 3);
        var override = new GensokyoMapData.UndergroundBlock(48, 32, 48, Blocks.AIR.defaultBlockState());
        var map = GensokyoOreGenerationTest.map(1084, 0, Map.of(position.pack(), List.of(override)));
        map.surfaces()[map.index(50, 50)] = 1;
        map.surfaces()[map.index(60, 60)] = 2;
        var generator = generator(map);
        var accessor = LevelHeightAccessor.create(0, 1536);
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        var fast = chunk(position, accessor, factory);
        var fallback = chunk(position, accessor, factory);
        fallback.setPersistedStatus(ChunkStatus.NOISE);
        var emptySection = fast.getSection(1);
        var biome = Holder.direct(GensokyoOreGenerationTest.biome(BiomeGenerationSettings.EMPTY));
        for (var target : List.of(fast, fallback)) {
            target.fillBiomesFromNoise((x, y, z, sampler) -> (x & 1) == 0 ? biome : factory.defaultBiome(), null);
            generator.fillFromNoise(null, null, null, target).join();
            target.initializeLightSources();
        }

        assertNotSame(emptySection, fast.getSection(1), "an empty underground section takes the fast path");
        assertNotSame(fast.getSection(1).getStates(), fast.getSection(2).getStates());
        assertSame(biome, fast.getSection(1).getNoiseBiome(0, 0, 0));
        assertSame(factory.defaultBiome(), fast.getSection(1).getNoiseBiome(1, 0, 0));
        var pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                assertEquals(fallback.getSkyLightSources().getLowestSourceY(x, z), fast.getSkyLightSources().getLowestSourceY(x, z));
                for (int y = 0; y < 1536; y++) {
                    pos.set(x, y, z);
                    assertSame(fallback.getBlockState(pos), fast.getBlockState(pos));
                }
            }
        }
        assertTrue(fast.getBlockState(pos.set(0, 32, 0)).isAir());
        assertTrue(fast.getBlockState(pos.set(1, 32, 0)).is(Blocks.STONE));
    }

    @Test void bulkFillDigestAndTimingStayStable() throws Exception {
        var override = new GensokyoMapData.UndergroundBlock(80, 1100, 80, Blocks.STONE.defaultBlockState());
        var map = GensokyoOreGenerationTest.map(1084, 0, Map.of(new ChunkPos(5, 5).pack(), List.of(override)));
        map.surfaces()[map.index(50, 50)] = 1;
        map.surfaces()[map.index(60, 60)] = 2;
        map.heights()[map.index(70, 70)] = 1010;
        map.heights()[map.index(90, 90)] = GensokyoMapData.NO_SURFACE;
        var generator = generator(map);
        var accessor = LevelHeightAccessor.create(0, 1536);
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        var positions = List.of(new ChunkPos(3, 3), new ChunkPos(4, 4), new ChunkPos(5, 5),
                new ChunkPos(8, 0), new ChunkPos(-1, -1));
        var digest = MessageDigest.getInstance("SHA-256");
        var pos = new BlockPos.MutableBlockPos();
        for (var position : positions) {
            var chunk = chunk(position, accessor, factory);
            generator.fillFromNoise(null, null, null, chunk).join();
            digest.update(("chunk:" + position + ";").getBytes(StandardCharsets.UTF_8));
            for (int y = 0; y < 1536; y++) {
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        digest.update(chunk.getBlockState(pos.set(lx, y, lz)).toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
            for (var type : List.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG)) {
                var heightmap = chunk.getOrCreateHeightmapUnprimed(type);
                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        digest.update((type.name() + ":" + heightmap.getFirstAvailable(lx, lz) + ";")
                                .getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }
        String hex = HexFormat.of().formatHex(digest.digest());
        for (int i = 0; i < 3; i++) {
            generator.fillFromNoise(null, null, null, chunk(new ChunkPos(4, 4), accessor, factory)).join();
        }
        long start = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            generator.fillFromNoise(null, null, null, chunk(new ChunkPos(4, 4), accessor, factory)).join();
        }
        long nanos = System.nanoTime() - start;
        Path report = Path.of("build/gensokyo-fill/report.txt");
        Files.createDirectories(report.getParent());
        Files.writeString(report, "digest=" + hex + "\navg_ms=" + nanos / 5_000_000.0 + "\n", StandardCharsets.UTF_8);
        // Baseline captured before bulk writes bypassed ProtoChunk's per-block heightmap updates.
        assertEquals("0a732feb136eee0b2562b42bb38ce319c6b3e823469ea2ec95ef011e46d68b52", hex);
    }

    @Test void biomeSourceUsesPeripheralBandsAndKeepsInteriorAuthoredBiomes() throws Exception {
        var original = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        var names = List.of("minecraft:river", "reverie_dreams:gensokyo_forest", "minecraft:windswept_forest", "minecraft:meadow", "minecraft:stony_peaks", "minecraft:frozen_peaks");
        var indices = new byte[128 * 128]; Arrays.fill(indices, (byte) 1);
        var map = new GensokyoMapData(1, 2, original.id(), 128, 128, 0, 0, 0, 1536, 1024, 3, 0,
                original.spawn(), names.stream().map(Identifier::parse).toList(), original.surfacePalette(), original.heights(), indices,
                original.surfaces(), List.of(), Map.of());
        var holders = names.stream().map(name -> Holder.direct(GensokyoOreGenerationTest.biome(BiomeGenerationSettings.EMPTY))).toList();
        var source = new GensokyoBiomeSource(map.id(), holders);
        var field = GensokyoBiomeSource.class.getDeclaredField("mapData"); field.setAccessible(true); field.set(source, map);
        assertSame(holders.get(1), source.getNoiseBiome(8, 250, 8, null));
        assertSame(holders.get(1), source.getNoiseBiome(-1, 250, 8, null));
        assertSame(holders.get(5), source.getNoiseBiome(-150, 250, 8, null));
        assertSame(holders.get(5), source.getNoiseBiome(-150, 250, 8, null));
        assertSame(holders.get(1), source.getNoiseBiome(8, 250, 8, null));
    }

    @Test void borderSampleCacheMatchesFreshInstanceAcrossReuseCollisionsAndEviction() throws Exception {
        var map = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        var cached = new GensokyoBorderTerrain(GensokyoBorderMapView.of(map));
        var fresh = new GensokyoBorderTerrain(GensokyoBorderMapView.of(map));
        var coordinates = new ArrayList<int[]>();
        for (int i = 0; i < 1000; i++) coordinates.add(new int[]{-320 + i * 13, 480 + i * 17});
        for (int[] coordinate : coordinates) {
            assertEquals(fresh.sample(coordinate[0], coordinate[1]), cached.sample(coordinate[0], coordinate[1]));
        }
        for (int i = coordinates.size() - 1; i >= 0; i--) {
            var coordinate = coordinates.get(i);
            assertEquals(fresh.sample(coordinate[0], coordinate[1]), cached.sample(coordinate[0], coordinate[1]));
        }
        for (int i = 0; i < 20_000; i++) cached.sample(-40_000 + i * 11, -40_000 + i * 7);
        for (int i = 0; i < 200; i++) {
            var coordinate = coordinates.get(i);
            assertEquals(fresh.sample(coordinate[0], coordinate[1]), cached.sample(coordinate[0], coordinate[1]));
        }
    }

    @Test void biomeFillResolvesOnlySixteenQuartColumnsPerChunk() throws Exception {
        var original = GensokyoOreGenerationTest.map(1050, 0, Map.of());
        var names = List.of("minecraft:plains", "minecraft:windswept_forest", "minecraft:snowy_slopes",
                "minecraft:frozen_peaks", "minecraft:meadow", "minecraft:stony_peaks",
                "minecraft:river", "minecraft:frozen_river", "minecraft:grove");
        var indices = new byte[128 * 128];
        Arrays.fill(indices, (byte) 1);
        var map = new GensokyoMapData(1, 2, original.id(), 128, 128, 0, 0, 0, 1536, 1024, 3, 0,
                original.spawn(), names.stream().map(Identifier::parse).toList(), original.surfacePalette(),
                original.heights(), indices, original.surfaces(), List.of(), Map.of());
        var holders = names.stream()
                .map(name -> Holder.direct(GensokyoOreGenerationTest.biome(BiomeGenerationSettings.EMPTY))).toList();
        var source = new CountingBiomeSource(map.id(), holders);
        var field = GensokyoBiomeSource.class.getDeclaredField("mapData");
        field.setAccessible(true);
        field.set(source, map);
        var generator = new GensokyoChunkGenerator(source, map.id());
        var randomState = randomState();
        var accessor = LevelHeightAccessor.create(0, 1536);
        var factory = PalettedContainerFactory.create(GensokyoOreGenerationTest.registries);
        for (var position : List.of(new ChunkPos(4, 4), new ChunkPos(8, 0))) {
            var direct = chunk(position, accessor, factory);
            source.calls.set(0);
            direct.fillBiomesFromNoise(source, null);
            assertEquals(96 * 64, source.calls.get(), "direct fill at " + position);
            var cached = chunk(position, accessor, factory);
            source.calls.set(0);
            generator.createBiomes(randomState, Blender.empty(), null, cached).join();
            assertEquals(16, source.calls.get(), "cached fill at " + position);
            for (int section = 0; section < 96; section++) {
                var expected = direct.getSection(section).getBiomes();
                var actual = cached.getSection(section).getBiomes();
                for (int x = 0; x < 4; x++) for (int y = 0; y < 4; y++) for (int z = 0; z < 4; z++) {
                    assertSame(expected.get(x, y, z), actual.get(x, y, z),
                            "section " + section + " cell " + x + "," + y + "," + z + " at " + position);
                }
            }
        }
    }

    private static ProtoChunk chunk(ChunkPos position, LevelHeightAccessor accessor, PalettedContainerFactory factory) {
        var sections = new LevelChunkSection[96];
        for (int i = 0; i < sections.length; i++) sections[i] = new LevelChunkSection(
                new PalettedContainer<>(Blocks.AIR.defaultBlockState(), factory.blockStatesStrategy()), factory.createForBiomes());
        return new ProtoChunk(position, UpgradeData.EMPTY, sections, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), accessor, factory, null);
    }

    private static RandomState randomState() {
        var owner = new MappedRegistry<NormalNoise.NoiseParameters>(Registries.NOISE, Lifecycle.stable());
        HolderGetter<NormalNoise.NoiseParameters> getter = new HolderGetter<>() {
            @Override
            public Optional<Holder.Reference<NormalNoise.NoiseParameters>> get(ResourceKey<NormalNoise.NoiseParameters> key) {
                return Optional.of(Holder.Reference.createIntrusive(owner, new NormalNoise.NoiseParameters(0, 1.0)));
            }

            @Override
            public Optional<HolderSet.Named<NormalNoise.NoiseParameters>> get(TagKey<NormalNoise.NoiseParameters> key) {
                return Optional.empty();
            }
        };
        return RandomState.create(NoiseGeneratorSettings.dummy(), getter, 42);
    }

    static final class CountingBiomeSource extends GensokyoBiomeSource {
        final AtomicInteger calls = new AtomicInteger();

        CountingBiomeSource(Identifier mapId, List<Holder<Biome>> biomes) {
            super(mapId, biomes);
        }

        @Override
        public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
            this.calls.incrementAndGet();
            return super.getNoiseBiome(x, y, z, sampler);
        }
    }
}
