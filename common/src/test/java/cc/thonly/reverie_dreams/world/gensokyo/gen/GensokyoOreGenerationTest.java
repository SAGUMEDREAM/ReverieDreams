package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoOreGenerationTest {
    static RegistryAccess.Frozen registries;
    static Holder<Biome> plains, mountain, badlands, maple;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var features = new MappedRegistry<ConfiguredFeature<?, ?>>(Registries.CONFIGURED_FEATURE, Lifecycle.stable());
        for (var band : GensokyoOreGeneration.BANDS) {
            if (features.containsKey(band.feature().identifier())) continue;
            String name = band.feature().identifier().getPath();
            int size;
            float exposure;
            if (band.feature().identifier().getNamespace().equals("minecraft")) {
                String resource = "/data/minecraft/worldgen/configured_feature/" + name + ".json";
                try (var input = GensokyoOreGenerationTest.class.getResourceAsStream(resource)) {
                    assertNotNull(input, resource);
                    var config = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                            .getAsJsonObject().getAsJsonObject("config");
                    size = config.get("size").getAsInt();
                    exposure = config.get("discard_chance_on_air_exposure").getAsFloat();
                }
            } else {
                // Mod blocks are represented by distinct vanilla markers in this fixture;
                // their registered size/exposure are retained without booting the mod.
                size = name.contains("silver") ? 15 : 7;
                exposure = name.contains("silver") ? .3F : .32F;
            }
            var output = name.contains("coal") ? Blocks.COAL_ORE : name.contains("iron") ? Blocks.IRON_ORE
                    : name.contains("copper") ? Blocks.COPPER_ORE : name.contains("gold") ? Blocks.GOLD_ORE
                    : name.contains("diamond") ? Blocks.DIAMOND_ORE : name.contains("redstone") ? Blocks.REDSTONE_ORE
                    : name.contains("lapis") ? Blocks.LAPIS_ORE : name.contains("emerald") ? Blocks.EMERALD_ORE
                    : name.contains("silver") ? Blocks.RAW_IRON_BLOCK : Blocks.AMETHYST_BLOCK;
            var config = new OreConfiguration(List.of(OreConfiguration.target(new BlockMatchTest(Blocks.STONE), output.defaultBlockState())), size, exposure);
            features.register(band.feature(), new ConfiguredFeature<>(Feature.ORE, config), RegistrationInfo.BUILT_IN);
        }
        features.freeze();
        var biomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        plains = biomes.register(Biomes.PLAINS, biome(BiomeGenerationSettings.EMPTY), RegistrationInfo.BUILT_IN);
        badlands = biomes.register(Biomes.BADLANDS, biome(BiomeGenerationSettings.EMPTY), RegistrationInfo.BUILT_IN);
        var emerald = features.getOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.withDefaultNamespace("ore_emerald")));
        var placedKey = ResourceKey.create(Registries.PLACED_FEATURE, Identifier.withDefaultNamespace("ore_emerald"));
        var placed = new MappedRegistry<PlacedFeature>(Registries.PLACED_FEATURE, Lifecycle.stable());
        var emeraldPlacement = placed.register(placedKey, new PlacedFeature(emerald, List.of()), RegistrationInfo.BUILT_IN);
        placed.freeze();
        var settings = new BiomeGenerationSettings.PlainBuilder().addFeature(6, emeraldPlacement).build();
        mountain = biomes.register(Biomes.STONY_PEAKS, biome(settings), RegistrationInfo.BUILT_IN);
        maple = biomes.register(ResourceKey.create(Registries.BIOME, Identifier.parse("reverie_dreams:maple_forest")), biome(BiomeGenerationSettings.EMPTY), RegistrationInfo.BUILT_IN);
        biomes.bindTags(Map.of(BiomeTags.IS_OVERWORLD, List.of(plains, badlands, mountain), BiomeTags.IS_BADLANDS, List.of(badlands)));
        biomes.freeze();
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(features, biomes, placed)).freeze();
    }

    static Biome biome(BiomeGenerationSettings settings) {
        return new Biome.BiomeBuilder().temperature(.7F).downfall(.5F)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x3f76e4).build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build()).generationSettings(settings).build();
    }

    static GensokyoMapData map(int top, int surface, Map<Long, List<GensokyoMapData.UndergroundBlock>> underground) {
        short[] heights = new short[128 * 128], surfaces = new short[128 * 128];
        Arrays.fill(heights, (short) top);
        Arrays.fill(surfaces, (short) surface);
        return new GensokyoMapData(1, 2, Identifier.parse("test:ores"), 128, 128, 0, 0,
                0, 1536, 1024, 3, 0, new GensokyoMapData.Spawn(new BlockPos(64, top + 1, 64), 0, 0),
                List.of(Biomes.PLAINS.identifier()), List.of(Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.LAVA.defaultBlockState()),
                heights, new byte[128 * 128], surfaces, List.of(), underground);
    }

    static GensokyoOreGeneration.Band band(String name) {
        return GensokyoOreGeneration.BANDS.stream().filter(b -> b.name().equals(name)).findFirst().orElseThrow();
    }

    @Test void depthBandsHaveUniformPeakedAndIncreasingDistributions() {
        int[] coal = new int[3], iron = new int[3], diamond = new int[3];
        RandomSource random = RandomSource.create(58124);
        for (int i = 0; i < 100_000; i++) {
            for (var entry : List.of(Map.entry(band("coal_shallow"), coal), Map.entry(band("iron_middle"), iron), Map.entry(band("diamond"), diamond))) {
                var b = entry.getKey();
                int depth = 1084 - b.sampleY(random, 1084, 1024);
                assertTrue(depth >= b.low() && depth <= b.high());
                entry.getValue()[Math.min(2, (depth - b.low()) * 3 / (b.high() - b.low() + 1))]++;
            }
        }
        assertTrue(Math.abs(coal[0] - coal[2]) < 2000, Arrays.toString(coal));
        assertTrue(iron[1] > iron[0] * 2 && iron[1] > iron[2] * 2, Arrays.toString(iron));
        assertTrue(diamond[2] > diamond[0] * 4, Arrays.toString(diamond));
        // Terrain depth follows a raised mountain by translation, without stretching its width.
        for (var b : GensokyoOreGeneration.BANDS) {
            if (b.shape() == GensokyoOreGeneration.Shape.ALTITUDE_TRIANGLE) continue;
            assertEquals(300, b.sampleY(RandomSource.create(77), 1384, 1024) - b.sampleY(RandomSource.create(77), 1084, 1024));
        }
    }

    @Test void rareLargeDiamondsAndFeatureSeedsAreStable() {
        var random = new WorldgenRandom(new XoroshiroRandomSource(0));
        int rare = 0;
        for (int i = 0; i < 90_000; i++) rare += band("diamond_large").sampleCount(random);
        assertTrue(rare > 9500 && rare < 10500, "rare placements=" + rare);
        long seed = GensokyoOreGeneration.decorationSeed(random, 42, new ChunkPos(2, 3));
        GensokyoOreGeneration.seedBand(random, seed, band("iron_middle"));
        long first = random.nextLong();
        GensokyoOreGeneration.seedBand(random, seed, band("diamond"));
        random.nextLong();
        GensokyoOreGeneration.seedBand(random, seed, band("iron_middle"));
        assertEquals(first, random.nextLong());
    }

    @Test void completeVeinBoundsProtectDepthFloorSlopesAndCrossChunkOverrides() {
        var map = map(1084, 0, Map.of());
        assertEquals(704, GensokyoOreGeneration.miningFloor(map));
        assertTrue(GensokyoOreGeneration.fitsVein(map, 47, 900, 32, 17));
        assertFalse(GensokyoOreGeneration.fitsVein(map, 47, 768, 32, 17));
        assertFalse(GensokyoOreGeneration.fitsVein(map(1010, 0, Map.of()), 47, 707, 32, 17));
        assertFalse(GensokyoOreGeneration.fitsVein(map, 1, 900, 32, 17));
        map.heights()[map.index(49, 32)] = 1300;
        assertFalse(GensokyoOreGeneration.fitsVein(map, 47, 900, 32, 17));
        var block = new GensokyoMapData.UndergroundBlock(49, 900, 32, Blocks.STONE.defaultBlockState());
        var protectedMap = map(1084, 0, Map.of(new ChunkPos(3, 2).pack(), List.of(block)));
        assertFalse(GensokyoOreGeneration.fitsVein(protectedMap, 47, 900, 32, 17));
    }

    @Test void waterAndLavaUseTheSolidBottomAndBiomeBonusesStayLocal() {
        var water = map(1084, 1, Map.of());
        assertTrue(GensokyoOreGeneration.solidTop(water, 64, 64) < 1084);
        assertEquals(988, GensokyoOreGeneration.solidTop(map(1084, 2, Map.of()), 64, 64));
        assertFalse(GensokyoOreGeneration.fitsVein(water, 64, 1080, 64, 17));
        assertFalse(GensokyoOreGeneration.allowsBiome(band("emerald_highland"), plains));
        assertTrue(GensokyoOreGeneration.allowsBiome(band("emerald_highland"), mountain));
        assertFalse(GensokyoOreGeneration.allowsBiome(band("gold_badlands"), plains));
        assertTrue(GensokyoOreGeneration.allowsBiome(band("gold_badlands"), badlands));
        assertTrue(GensokyoOreGeneration.allowsBiome(band("iron_middle"), maple));
    }

    @Test void productionPassPlacesRealVanillaClustersAndLeavesDeepStoneIntact() throws Exception {
        var map = map(1084, 0, Map.of());
        var first = new StoneWorld(42, plains, 1084);
        var again = new StoneWorld(42, plains, 1084);
        var otherSeed = new StoneWorld(43, plains, 1084);
        for (var world : List.of(first, again, otherSeed)) {
            for (int cx = 3; cx <= 4; cx++) for (int cz = 3; cz <= 4; cz++)
                GensokyoOreGeneration.place(world.level, null, new ChunkPos(cx, cz), map);
        }
        var blocks = first.ores();
        assertTrue(blocks.size() > 400, "actual ore blocks=" + blocks.size());
        assertEquals(blocks, again.ores());
        assertNotEquals(blocks, otherSeed.ores());
        Set<String> types = new HashSet<>(blocks.values());
        for (var block : List.of(Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE,
                Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.RAW_IRON_BLOCK, Blocks.AMETHYST_BLOCK))
            assertTrue(types.contains(block.defaultBlockState().toString()), block.toString());
        assertFalse(types.contains(Blocks.EMERALD_ORE.defaultBlockState().toString()));
        for (long key : blocks.keySet()) {
            var pos = BlockPos.of(key);
            assertTrue(pos.getY() >= 704);
            assertTrue(1084 - pos.getY() <= 320 && 1084 - pos.getY() >= 4);
        }
        // Real OreFeature produces adjacent connected blocks, rather than isolated random pixels.
        long connected = blocks.keySet().stream().filter(k -> blocks.containsKey(BlockPos.of(k).east().asLong())
                || blocks.containsKey(BlockPos.of(k).above().asLong()) || blocks.containsKey(BlockPos.of(k).south().asLong())).count();
        assertTrue(connected > blocks.size() / 2);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("scope", "Production ore pass with real vanilla OreFeature in in-memory stone chunks, not a game save or full terrain generation");
        report.put("world_seed", 42);
        report.put("source_chunks", 4);
        report.put("surface_y", 1084);
        report.put("ore_blocks_including_cross_chunk_spill", blocks.size());
        report.put("blocks_with_a_positive_axis_ore_neighbor", connected);
        Map<String, Integer> counts = new TreeMap<>();
        blocks.values().forEach(value -> counts.merge(value, 1, Integer::sum));
        report.put("block_counts", counts);
        var canonical = new TreeMap<>(blocks);
        var digest = MessageDigest.getInstance("SHA-256");
        for (var entry : canonical.entrySet()) {
            var pos = BlockPos.of(entry.getKey());
            digest.update((pos.getX() + "," + pos.getY() + "," + pos.getZ() + "=" + entry.getValue() + "\n")
                    .getBytes(StandardCharsets.UTF_8));
        }
        report.put("ore_map_sha256", HexFormat.of().formatHex(digest.digest()));
        report.put("min_ore_y", blocks.keySet().stream().mapToInt(k -> BlockPos.of(k).getY()).min().orElseThrow());
        report.put("max_ore_y", blocks.keySet().stream().mapToInt(k -> BlockPos.of(k).getY()).max().orElseThrow());
        report.put("same_seed_identical", true);
        report.put("different_seed_differs", true);
        report.put("reserved_deep_stone_unchanged", true);
        report.put("mod_block_fixture_markers", Map.of("silver_ore", "raw_iron_block", "orb_ore", "amethyst_block"));
        report.put("bands", GensokyoOreGeneration.BANDS.stream().map(b -> Map.of(
                "name", b.name(), "configured_feature", b.feature().identifier().toString(),
                "min_count", b.minCount(), "max_count", b.maxCount(), "rarity", b.rarity(),
                "low", b.low(), "high", b.high(), "shape", b.shape().name(), "biome_restriction", b.restriction().name())).toList());
        Path output = Path.of("build/ore-validation/fixture.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }

    /** In-memory stone chunks; no server, disk world, or game loop is started. */
    static final class StoneWorld {
        final Map<ChunkPos, ProtoChunk> chunks = new HashMap<>();
        final WorldGenLevel level;
        final PalettedContainerFactory factory = PalettedContainerFactory.create(registries);

        StoneWorld(long seed, Holder<Biome> biome, int top) {
            level = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class[]{WorldGenLevel.class}, (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "registryAccess" -> registries;
                    case "getSeed" -> seed;
                    case "getHeight" -> args == null || args.length == 0 ? 1536 : top + 1;
                    case "getMinY", "getMinSectionY" -> 0;
                    case "getSectionsCount" -> 96;
                    case "getSectionIndex" -> (int) args[0] >> 4;
                    case "getBiome" -> biome;
                    case "ensureCanWrite" -> true;
                    case "getChunk" -> chunk(new ChunkPos((int) args[0], (int) args[1]));
                    case "isOutsideBuildHeight" -> {
                        int y = args[0] instanceof BlockPos pos ? pos.getY() : (int) args[0];
                        yield y < 0 || y >= 1536;
                    }
                    case "getBlockState" -> {
                        BlockPos pos = (BlockPos) args[0];
                        yield chunk(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4)).getBlockState(pos);
                    }
                    default -> {
                        if (method.isDefault()) yield InvocationHandler.invokeDefault(proxy, method, args);
                        throw new UnsupportedOperationException(method.toString());
                    }
                };
            });
        }

        ProtoChunk chunk(ChunkPos pos) {
            return chunks.computeIfAbsent(pos, p -> {
                LevelChunkSection[] sections = new LevelChunkSection[96];
                for (int i = 0; i < sections.length; i++) sections[i] = new LevelChunkSection(
                        new PalettedContainer<>(Blocks.STONE.defaultBlockState(), factory.blockStatesStrategy()), factory.createForBiomes());
                return new ProtoChunk(p, UpgradeData.EMPTY, sections, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), LevelHeightAccessor.create(0, 1536), factory, null);
            });
        }

        Map<Long, String> ores() {
            Map<Long, String> result = new HashMap<>();
            for (var entry : chunks.entrySet()) {
                var chunk = entry.getValue();
                for (int si = 0; si < 96; si++) {
                    var section = chunk.getSection(si);
                    if (!section.getStates().maybeHas(state -> !state.is(Blocks.STONE))) continue;
                    for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                        var state = section.getBlockState(x, y, z);
                        if (!state.is(Blocks.STONE)) result.put(BlockPos.asLong(entry.getKey().getMinBlockX() + x, si * 16 + y, entry.getKey().getMinBlockZ() + z), state.toString());
                    }
                }
            }
            return result;
        }
    }
}
