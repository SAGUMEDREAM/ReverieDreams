package cc.thonly.reverie_dreams.world.gensokyo.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.Random;
import java.util.function.ToIntBiFunction;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoTreeFeatureTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

    @BeforeAll
    static void bootstrap() {
        GensokyoForestPreviewExporter.registries();
        VanillaTreeTags.bind();
    }

    @Test
    void actualVanillaDirtTagExcludesGrassButNaturalTreeSubstrateIncludesIt() {
        assertFalse(Blocks.GRASS_BLOCK.defaultBlockState().is(BlockTags.DIRT));
        assertFalse(Blocks.PODZOL.defaultBlockState().is(BlockTags.DIRT));
        assertTrue(Blocks.DIRT.defaultBlockState().is(BlockTags.DIRT));
        assertTrue(Blocks.GRASS_BLOCK.defaultBlockState().is(BlockTags.SUBSTRATE_OVERWORLD));
        assertTrue(Blocks.PODZOL.defaultBlockState().is(BlockTags.SUBSTRATE_OVERWORLD));
    }

    @Test
    void successfulPlacementHasExpectedGeometryAndNaturallyDecayingLeaves() {
        var world = new MemoryWorld();
        assertTrue(GensokyoTreeFeature.placeTree(world.level, ORIGIN, GensokyoForestBootstrap.configuration(GensokyoTreeProfile.MAGIC_DARK_OAK), 42));
        var shape = GensokyoTreeShape.create(GensokyoTreeProfile.MAGIC_DARK_OAK.settings(), new Random(42));
        assertEquals(shape.voxels().size(), world.writes);
        assertTrue(world.states.values().stream().filter(state -> state.hasProperty(LeavesBlock.DISTANCE))
                .allMatch(state -> !state.getValue(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.DISTANCE) <= 6));
        assertEquals(Blocks.GRASS_BLOCK.defaultBlockState(), world.state(ORIGIN.below()));
    }

    @Test
    void everyTreeProfileGrowsOnRealGrassDirtPodzolMyceliumMossAndMud() {
        for (var soil : new Block[]{Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT,
                Blocks.ROOTED_DIRT, Blocks.PODZOL, Blocks.MYCELIUM, Blocks.MOSS_BLOCK, Blocks.PALE_MOSS_BLOCK, Blocks.MUD}) {
            for (var profile : GensokyoTreeProfile.values()) {
                var world = new MemoryWorld();
                world.soil = soil.defaultBlockState();
                assertTrue(GensokyoTreeFeature.placeTree(world.level, ORIGIN, GensokyoForestBootstrap.configuration(profile), 42),
                        () -> "Rejected " + profile + " on " + soil);
                assertTrue(world.writes > 0);
            }
        }
    }

    @Test
    void changedCanopiesSurviveNativeLeafUpdatesAndDecayAfterAllLogsAreRemoved() throws ReflectiveOperationException {
        var update = LeavesBlock.class.getDeclaredMethod("updateDistance", BlockState.class, LevelAccessor.class, BlockPos.class);
        update.setAccessible(true);
        for (var profile : GensokyoTreeProfile.values()) {
            var world = new MemoryWorld();
            assertTrue(GensokyoTreeFeature.placeTree(world.level, ORIGIN, GensokyoForestBootstrap.configuration(profile), 20261103));
            for (var entry : world.states.entrySet()) {
                if (!entry.getValue().hasProperty(LeavesBlock.DISTANCE)) continue;
                var state = (BlockState) update.invoke(null, entry.getValue(), world.level, entry.getKey());
                assertTrue(state.getValue(LeavesBlock.DISTANCE) < LeavesBlock.DECAY_DISTANCE, () -> "Unsupported natural canopy: " + profile);
            }
            world.states.entrySet().removeIf(entry -> entry.getValue().is(BlockTags.PREVENTS_NEARBY_LEAF_DECAY));
            for (int pass = 0; pass < 7; pass++) {
                for (var entry : world.states.entrySet()) {
                    if (entry.getValue().hasProperty(LeavesBlock.DISTANCE)) {
                        entry.setValue((BlockState) update.invoke(null, entry.getValue(), world.level, entry.getKey()));
                    }
                }
            }
            assertTrue(world.states.values().stream().allMatch(state -> state.getValue(LeavesBlock.DISTANCE) == LeavesBlock.DECAY_DISTANCE),
                    () -> "Leaf canopy should decay after removing its logs: " + profile);
        }
    }

    @Test
    void forestDensityVariesContinuouslyAcrossTheMapWithSparseAndDensePatches() {
        var registries = GensokyoForestPreviewExporter.registries();
        var biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(GensokyoForestType.BIRCH_FOREST));
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome),
                registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        var placed = registries.lookupOrThrow(Registries.PLACED_FEATURE)
                .getOrThrow(GensokyoForestBootstrap.placed(GensokyoForestType.BIRCH_FOREST.treesPath())).value();
        var context = new PlacementContext(new MemoryWorld().level, generator, Optional.of(placed));
        var sampledCounts = new HashSet<Long>();
        for (int x = -1024; x <= 1024; x += 64) {
            for (int z = -1024; z <= 1024; z += 64) {
                sampledCounts.add(placed.placement().getFirst().getPositions(context, RandomSource.create(42), new BlockPos(x, 0, z)).count());
            }
        }
        assertTrue(sampledCounts.size() >= 4, sampledCounts.toString());
        assertTrue(sampledCounts.contains(0L), "Leave some clearings");
        assertTrue(sampledCounts.stream().anyMatch(count -> count >= 5), "Keep dense tree groups as well");
    }

    @ParameterizedTest
    @EnumSource(GensokyoForestType.class)
    void singleBiomeSurfacePlacementRunsTheActualHeightmapBiomeFilterSelectorAndTreeFeature(GensokyoForestType type) {
        var registries = GensokyoForestPreviewExporter.registries();
        var biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(type));
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome),
                registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        var placed = registries.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(GensokyoForestBootstrap.placed(type.treesPath())).value();
        for (long seed : new long[]{1, 42, 20261103}) {
            var world = new MemoryWorld();
            world.biome = biome;
            world.writable = pos -> Math.abs(Math.floorDiv(pos.getX(), 16)) <= 1 && Math.abs(Math.floorDiv(pos.getZ(), 16)) <= 1;
            assertTrue(placed.placeWithBiomeCheck(world.level, generator, RandomSource.create(seed), new BlockPos(0, 0, 0)),
                    () -> "No tree placed by " + type + " with seed " + seed);
            assertTrue(world.writes > 0);
            assertTrue(world.states.entrySet().stream().anyMatch(entry -> entry.getKey().getY() > 70 && entry.getValue().hasProperty(LeavesBlock.DISTANCE)));
        }
    }

    @Test
    void blockedTrunkOrCrownLeavesNoPartialTree() {
        var shape = GensokyoTreeShape.create(GensokyoTreeProfile.MAGIC_OAK.settings(), new Random(8));
        for (boolean log : new boolean[]{true, false}) {
            var world = new MemoryWorld();
            var obstruction = shape.voxels().stream().filter(voxel -> voxel.log() == log).findFirst().orElseThrow().point();
            world.states.put(ORIGIN.offset(obstruction.x(), obstruction.y(), obstruction.z()), Blocks.STONE.defaultBlockState());
            assertFalse(GensokyoTreeFeature.placeTree(world.level, ORIGIN, GensokyoForestBootstrap.configuration(GensokyoTreeProfile.MAGIC_OAK), 8));
            assertEquals(0, world.writes);
        }
    }

    @Test
    void unsuitableOrUnevenGroundIsRejected() {
        var world = new MemoryWorld();
        world.states.put(ORIGIN.offset(1, -1, 1), Blocks.AIR.defaultBlockState());
        assertFalse(GensokyoTreeFeature.placeTree(world.level, ORIGIN, GensokyoForestBootstrap.configuration(GensokyoTreeProfile.MAGIC_DARK_OAK), 1));
        assertEquals(0, world.writes);
        world.states.put(ORIGIN.offset(1, -1, 1), Blocks.STONE.defaultBlockState());
        assertFalse(GensokyoTreeFeature.placeTree(world.level, ORIGIN, GensokyoForestBootstrap.configuration(GensokyoTreeProfile.MAGIC_DARK_OAK), 1));
        assertEquals(0, world.writes);
    }

    @Test
    void jungleButtressRequiresSoilUnderEveryOutlyingFootAndLeavesNoPartialTree() {
        var shape = GensokyoTreeShape.create(GensokyoTreeProfile.JUNGLE_CANOPY.settings(), new Random(42));
        var foot = shape.voxels().stream().filter(v -> v.log() && v.point().y() == 0 && v.point().x() < -1)
                .findFirst().orElseThrow().point();
        var world = new MemoryWorld();
        world.states.put(ORIGIN.offset(foot.x(), -1, foot.z()), Blocks.AIR.defaultBlockState());
        assertFalse(GensokyoTreeFeature.placeTree(world.level, ORIGIN,
                GensokyoForestBootstrap.configuration(GensokyoTreeProfile.JUNGLE_CANOPY), 42));
        assertEquals(0, world.writes, "Unsupported roots must not leave a partial tree");
    }

    @Test
    void heightLimitWaterAndUnwritableNeighborChunksRejectBeforeAnyWrites() {
        var config = GensokyoForestBootstrap.configuration(GensokyoTreeProfile.MAGIC_OAK);
        var height = new MemoryWorld();
        height.maxY = 70;
        assertFalse(GensokyoTreeFeature.placeTree(height.level, ORIGIN, config, 5));
        assertEquals(0, height.writes);
        var boundary = new MemoryWorld();
        boundary.writable = pos -> pos.getX() >= 0;
        assertFalse(GensokyoTreeFeature.placeTree(boundary.level, ORIGIN, config, 5));
        assertEquals(0, boundary.writes);
        var water = new MemoryWorld();
        water.states.put(ORIGIN, Blocks.WATER.defaultBlockState());
        assertFalse(GensokyoTreeFeature.placeTree(water.level, ORIGIN, config, 5));
        assertEquals(0, water.writes);
    }

    static final class MemoryWorld {
        final Map<BlockPos, BlockState> states = new HashMap<>();
        int maxY = 256;
        int writes;
        int brightness = 10;
        long seed = 20261105;
        BlockState soil = Blocks.GRASS_BLOCK.defaultBlockState();
        Holder<Biome> biome;
        Predicate<BlockPos> writable = pos -> true;
        ToIntBiFunction<Integer, Integer> terrain = (x, z) -> 63;
        final WorldGenLevel level = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class[]{WorldGenLevel.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMinY" -> 0;
                    case "getMaxY" -> maxY;
                    case "getHeight" -> args == null || args.length == 0 ? maxY : surfaceHeight((Heightmap.Types) args[0], (int) args[1], (int) args[2]);
                    case "getBiome" -> biome;
                    case "getSeed" -> seed;
                    case "getRawBrightness" -> brightness;
                    case "ensureCanWrite" -> writable.test((BlockPos) args[0]);
                    case "getBlockState" -> state((BlockPos) args[0]);
                    case "getFluidState" -> state((BlockPos) args[0]).getFluidState();
                    case "getBlockEntity" -> null;
                    case "isStateAtPosition" -> ((Predicate<BlockState>) args[1]).test(state((BlockPos) args[0]));
                    case "setBlock" -> { states.put(((BlockPos) args[0]).immutable(), (BlockState) args[1]); writes++; yield true; }
                    case "toString" -> "Tree preview in-memory world";
                    default -> {
                        if (method.isDefault()) yield InvocationHandler.invokeDefault(proxy, method, args);
                        throw new UnsupportedOperationException(method.getName());
                    }
                });

        BlockState state(BlockPos pos) {
            return states.getOrDefault(pos, pos.getY() <= terrain.applyAsInt(pos.getX(), pos.getZ()) ? soil : Blocks.AIR.defaultBlockState());
        }

        int surfaceHeight(Heightmap.Types type, int x, int z) {
            int top = terrain.applyAsInt(x, z);
            for (int y = maxY - 1; y > top; y--) if (type.isOpaque().test(state(new BlockPos(x, y, z)))) return y + 1;
            return top + 1;
        }
    }
}
