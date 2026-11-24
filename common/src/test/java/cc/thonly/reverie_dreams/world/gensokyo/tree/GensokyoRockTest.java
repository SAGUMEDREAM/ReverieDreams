package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.gensokyo.rock.*;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Locale;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoRockTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
    @BeforeAll static void setup() { GensokyoForestPreviewExporter.registries(); VanillaTreeTags.bind(); }

    @Test void allTwentyOneProfilesRoundTripAndRockLayersPrecedeNewUnderstory() {
        var registry = GensokyoForestPreviewExporter.registries();
        var ops = registry.createSerializationContext(JsonOps.INSTANCE);
        assertEquals(21, GensokyoRockBootstrap.PROFILES.size());
        for (var profile : GensokyoRockBootstrap.PROFILES) {
            var configured = registry.lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(profile.configured()).value();
            var json = ConfiguredFeature.DIRECT_CODEC.encodeStart(ops, configured).getOrThrow();
            assertEquals(configured.config(), ConfiguredFeature.DIRECT_CODEC.parse(ops, json).getOrThrow().config());
            assertNotNull(registry.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(profile.placed()));
        }
        for (var theme : GensokyoForestType.values()) {
            var biome = registry.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(theme)).value();
            var layers = biome.getGenerationSettings().features().stream().flatMap(s -> s.stream())
                    .map(h -> h.unwrapKey().orElseThrow().identifier().getPath()).toList();
            var rocks = GensokyoRockBootstrap.PROFILES.stream().filter(p -> p.theme() == theme).toList();
            assertEquals(3, layers.stream().filter(p -> p.startsWith("gensokyo/rocks/")).count());
            assertTrue(layers.indexOf(rocks.getLast().path()) < layers.indexOf("gensokyo/understory/" + theme.name().toLowerCase(Locale.ROOT) + "/leaf_patch"));
            assertTrue(rocks.getFirst().rarity() < rocks.get(1).rarity() && rocks.get(1).rarity() < rocks.getLast().rarity());
        }
    }

    @Test void rocksOnlyReplaceTheSurfaceLayerAndAllAddedColumnsHaveRealSupport() {
        for (var profile : GensokyoRockBootstrap.PROFILES) for (long seed = 0; seed < 16; seed++) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            world.maxY = 96;
            var config = GensokyoRockBootstrap.configuration(profile);
            assertTrue(GensokyoRockFeature.placeRock(world.level, ORIGIN, config, seed, false), profile + " seed=" + seed);
            assertTrue(world.states.keySet().stream().allMatch(p -> p.getY() >= 63));
            assertTrue(world.states.keySet().stream().noneMatch(p -> p.getY() < world.terrain.applyAsInt(p.getX(), p.getZ())));
            assertTrue(world.states.values().stream().noneMatch(s -> s.isAir()));
            assertTrue(world.states.entrySet().stream().filter(e -> isRock(e.getValue())).allMatch(e -> {
                var below = world.state(e.getKey().below());
                return isRock(below) || below.is(BlockTags.SUBSTRATE_OVERWORLD);
            }), "Every rock column has a solid base");
            int height = world.states.entrySet().stream().filter(e -> isRock(e.getValue())).mapToInt(e -> e.getKey().getY() - 63).max().orElseThrow();
            assertTrue(height <= (profile.kind() == GensokyoRockConfiguration.Kind.LOW_OUTCROP ? 1 : profile.kind() == GensokyoRockConfiguration.Kind.ROCK_CLUSTER ? 3 : 4));
        }
    }

    @Test void gentleSlopesAreFollowedWithoutFlatteningOrDeeperExcavation() {
        for (var profile : GensokyoRockBootstrap.PROFILES) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            world.terrain = (x, z) -> x > 0 ? 64 : 63;
            assertTrue(GensokyoRockFeature.placeRock(world.level, ORIGIN, GensokyoRockBootstrap.configuration(profile), 4, false));
            assertTrue(world.states.keySet().stream().allMatch(p -> p.getY() >= world.terrain.applyAsInt(p.getX(), p.getZ())));
            assertTrue(world.states.values().stream().noneMatch(s -> s.isAir()));
        }
    }

    @Test void unsafeGroundRoadsBuildingsTreesWaterFlowersAndBoundariesRejectAtomically() {
        var config = GensokyoRockBootstrap.configuration(new GensokyoRockBootstrap.Profile(GensokyoForestType.MAGIC_FOREST, GensokyoRockConfiguration.Kind.LANDMARK_BOULDER));
        for (var block : List.of(Blocks.CHEST, Blocks.OAK_LEAVES, Blocks.OAK_LOG, Blocks.WATER, Blocks.POPPY)) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld(); world.states.put(ORIGIN, block.defaultBlockState());
            assertFalse(GensokyoRockFeature.placeRock(world.level, ORIGIN, config, 4, false)); assertEquals(0, world.writes);
        }
        for (var block : List.of(Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.STONE_BRICKS, Blocks.COBBLESTONE)) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld(); world.states.put(ORIGIN.offset(1, -1, 0), block.defaultBlockState());
            assertFalse(GensokyoRockFeature.placeRock(world.level, ORIGIN, config, 4, false)); assertEquals(0, world.writes);
        }
        var hole = new GensokyoTreeFeatureTest.MemoryWorld(); hole.states.put(ORIGIN.below(2), Blocks.AIR.defaultBlockState());
        assertFalse(GensokyoRockFeature.placeRock(hole.level, ORIGIN, config, 4, false)); assertEquals(0, hole.writes);
        var slope = new GensokyoTreeFeatureTest.MemoryWorld(); slope.terrain = (x, z) -> x > 0 ? 66 : 63;
        assertFalse(GensokyoRockFeature.placeRock(slope.level, ORIGIN, config, 4, false)); assertEquals(0, slope.writes);
        var boundary = new GensokyoTreeFeatureTest.MemoryWorld(); boundary.writable = p -> p.getX() >= 0;
        assertFalse(GensokyoRockFeature.placeRock(boundary.level, ORIGIN, config, 4, false)); assertEquals(0, boundary.writes);
        var height = new GensokyoTreeFeatureTest.MemoryWorld(); height.maxY = 66;
        assertFalse(GensokyoRockFeature.placeRock(height.level, ORIGIN, config, 4, false)); assertEquals(0, height.writes);
    }

    @Test void naturalRocksDoNotBecomeBuildingMarkersAndRepeatedPlacementCannotStack() {
        var world = new GensokyoTreeFeatureTest.MemoryWorld();
        var config = GensokyoRockBootstrap.configuration(new GensokyoRockBootstrap.Profile(GensokyoForestType.FOREST, GensokyoRockConfiguration.Kind.LOW_OUTCROP));
        assertTrue(GensokyoRockFeature.placeRock(world.level, ORIGIN, config, 2, false));
        int writes = world.writes;
        assertFalse(GensokyoRockFeature.placeRock(world.level, ORIGIN, config, 2, false)); assertEquals(writes, world.writes);
        assertFalse(GensokyoUnderstoryFeature.isNearProtectedGround(world.level, ORIGIN.offset(2, 0, 0)));
        world.states.put(ORIGIN.offset(3, -1, 0), Blocks.COBBLESTONE.defaultBlockState());
        assertTrue(GensokyoUnderstoryFeature.isNearProtectedGround(world.level, ORIGIN.offset(2, 0, 0)));
    }

    @Test void naturalPlacementBudgetsKeepRocksSparseAndSurfaceChangesShallow() {
        for (var theme : GensokyoForestType.values()) {
            var result = GensokyoRockPreview.landscape(theme, 20261105L, 16);
            assertTrue(result.rockColumns() > 0, theme.toString());
            assertTrue(result.rockColumns() < result.area() * .025, theme + " rock coverage=" + (double) result.rockColumns() / result.area());
            assertTrue(result.counts().get(GensokyoRockConfiguration.Kind.LOW_OUTCROP) > result.counts().get(GensokyoRockConfiguration.Kind.LANDMARK_BOULDER));
            assertTrue(result.world().states.keySet().stream().allMatch(p -> p.getY() >= 63));
        }
    }

    @Test void lowOutcropsHaveAreaRatherThanDegeneratingIntoStraightStrips() {
        for (var theme : GensokyoForestType.values()) for (int seed = 0; seed < 64; seed++) {
            var shape = GensokyoRockShape.create(theme, GensokyoRockConfiguration.Kind.LOW_OUTCROP, seed);
            assertTrue(shape.stream().map(c -> c.point().x()).distinct().count() >= 2);
            assertTrue(shape.stream().map(c -> c.point().z()).distinct().count() >= 2);
            assertTrue(shape.size() <= 12);
        }
    }

    @Test void rareLandmarksCanStillGenerateThroughTheRegisteredNaturalPlacementChain() {
        int generated = 0;
        for (long seed : new long[]{0, 42, 20261105}) {
            var result = GensokyoRockPreview.landscape(GensokyoForestType.TAIGA, seed, 32);
            generated += result.counts().get(GensokyoRockConfiguration.Kind.LANDMARK_BOULDER);
            if (generated > 0) break;
        }
        assertTrue(generated > 0, "Rare landmarks must remain reachable through the real placement modifiers and habitat filter");
    }
    static boolean isRock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.MOSSY_COBBLESTONE);
    }
}
