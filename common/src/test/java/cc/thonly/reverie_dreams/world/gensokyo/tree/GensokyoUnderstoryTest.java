package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.gensokyo.understory.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import com.google.gson.JsonArray;
import java.util.List;
import net.minecraft.tags.BlockTags;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoUnderstoryTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
    @BeforeAll static void setup() { GensokyoForestPreviewExporter.registries(); VanillaTreeTags.bind(); }

    @Test void allTwentyEightProfilesRoundTripAndAppendExactlyFourLayersPerBiome() {
        var registries = GensokyoForestPreviewExporter.registries();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        assertEquals(28, GensokyoUnderstoryBootstrap.PROFILES.size());
        for (var profile : GensokyoUnderstoryBootstrap.PROFILES) {
            var feature = registries.lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(profile.configured()).value();
            var json = ConfiguredFeature.DIRECT_CODEC.encodeStart(ops, feature).getOrThrow();
            assertEquals(feature.config(), ConfiguredFeature.DIRECT_CODEC.parse(ops, json).getOrThrow().config());
            assertNotNull(registries.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(profile.placed()));
        }
        for (var theme : GensokyoForestType.values()) {
            var biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(theme)).value();
            var layers = biome.getGenerationSettings().features().stream().flatMap(s -> s.stream())
                    .map(h -> h.unwrapKey().orElseThrow()).filter(k -> k.identifier().getPath().startsWith("gensokyo/understory/")).toList();
            assertEquals(GensokyoUnderstoryBootstrap.PROFILES.stream().filter(p -> p.theme() == theme).map(GensokyoUnderstoryBootstrap.Profile::placed).toList(), layers);
        }
    }

    @Test void everyThemeAndScaleProducesSupportedDecayingFoliageAndSurvivingFloorPlants() {
        for (var profile : GensokyoUnderstoryBootstrap.PROFILES) for (long seed = 0; seed < 24; seed++) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            var config = GensokyoUnderstoryBootstrap.configuration(profile);
            assertTrue(GensokyoUnderstoryFeature.placePatch(world.level, ORIGIN, config, seed), profile + " seed=" + seed);
            verifyLeaves(world);
            for (var entry : world.states.entrySet()) {
                var state = entry.getValue();
                if (!state.hasProperty(LeavesBlock.DISTANCE) && config.logs().stream().noneMatch(log -> log.is(state.getBlock())))
                    assertTrue(state.canSurvive(world.level, entry.getKey()), () -> "Unsupported floor plant: " + entry);
            }
            assertTrue(world.states.keySet().stream().allMatch(p -> Math.abs(p.getX()) <= 7 && Math.abs(p.getZ()) <= 7 && p.getY() < 69));
            if (profile.kind() == GensokyoUnderstoryConfiguration.Kind.FLOOR_PATCH)
                assertTrue(world.states.values().stream().noneMatch(s -> s.hasProperty(LeavesBlock.DISTANCE)));
        }
    }

    @Test void slopesFollowRealSoilAndCroppingKeepsLeavesConnected() {
        for (var profile : GensokyoUnderstoryBootstrap.PROFILES) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            world.terrain = (x, z) -> x > 0 ? 64 : 63;
            assertTrue(GensokyoUnderstoryFeature.placePatch(world.level, ORIGIN, GensokyoUnderstoryBootstrap.configuration(profile), 9));
            verifyLeaves(world);
            assertTrue(world.states.keySet().stream().allMatch(p -> p.getY() > world.terrain.applyAsInt(p.getX(), p.getZ())));
        }
    }

    @Test void waterRoadsStructuresExistingVegetationAndUnwritableFootprintsLeaveNoPartialShrubs() {
        var config = GensokyoUnderstoryBootstrap.configuration(new GensokyoUnderstoryBootstrap.Profile(
                GensokyoForestType.MAGIC_FOREST, GensokyoUnderstoryConfiguration.Kind.SHRUB));
        for (var obstruction : List.of(Blocks.WATER, Blocks.OAK_LEAVES, Blocks.STONE_BRICKS, Blocks.CHEST, Blocks.TALL_GRASS)) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            world.states.put(ORIGIN, obstruction.defaultBlockState());
            assertFalse(GensokyoUnderstoryFeature.placePatch(world.level, ORIGIN, config, 0), obstruction.toString());
            assertEquals(0, world.writes);
        }
        var road = new GensokyoTreeFeatureTest.MemoryWorld();
        road.states.put(ORIGIN.offset(2, -1, 0), Blocks.DIRT_PATH.defaultBlockState());
        assertFalse(GensokyoUnderstoryFeature.placePatch(road.level, ORIGIN, config, 0));
        assertEquals(0, road.writes);
        var boundary = new GensokyoTreeFeatureTest.MemoryWorld();
        boundary.writable = p -> p.getX() >= 0;
        assertFalse(GensokyoUnderstoryFeature.placePatch(boundary.level, ORIGIN, config, 0));
        assertEquals(0, boundary.writes);
        var height = new GensokyoTreeFeatureTest.MemoryWorld();
        height.maxY = 65;
        assertFalse(GensokyoUnderstoryFeature.placePatch(height.level, ORIGIN, config, 0));
        assertEquals(0, height.writes);
    }

    @Test void malformedPalettesFailDecode() {
        var ops = GensokyoForestPreviewExporter.registries().createSerializationContext(JsonOps.INSTANCE);
        var config = GensokyoUnderstoryBootstrap.configuration(GensokyoUnderstoryBootstrap.PROFILES.getFirst());
        var json = GensokyoUnderstoryConfiguration.CODEC.encodeStart(ops, config).getOrThrow().getAsJsonObject();
        json.add("logs", new JsonArray());
        assertTrue(GensokyoUnderstoryConfiguration.CODEC.parse(ops, json).error().isPresent());
    }

    private static void verifyLeaves(GensokyoTreeFeatureTest.MemoryWorld world) {
        var distances = new HashMap<BlockPos, Integer>();
        var queue = new ArrayDeque<BlockPos>();
        world.states.forEach((pos, state) -> {
            if (state.is(BlockTags.LOGS)) { distances.put(pos, 0); queue.add(pos); }
        });
        while (!queue.isEmpty()) {
            var pos = queue.removeFirst();
            for (var direction : Direction.values()) {
                var next = pos.relative(direction);
                if (!distances.containsKey(next) && world.state(next).hasProperty(LeavesBlock.DISTANCE)) {
                    distances.put(next, distances.get(pos) + 1); queue.add(next);
                }
            }
        }
        world.states.forEach((pos, state) -> {
            if (!state.hasProperty(LeavesBlock.DISTANCE)) return;
            assertFalse(state.getValue(LeavesBlock.PERSISTENT));
            assertNotNull(distances.get(pos), () -> "Disconnected leaf " + pos);
            assertEquals(distances.get(pos), state.getValue(LeavesBlock.DISTANCE));
            assertTrue(distances.get(pos) <= 6);
        });
    }
}
