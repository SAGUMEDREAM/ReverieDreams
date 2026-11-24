package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.gensokyo.understory.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import cc.thonly.reverie_dreams.block.MagicMushroomBlock;
import cc.thonly.reverie_dreams.block.MagicMushroomCapBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import static org.junit.jupiter.api.Assertions.*;

class MagicMushroomTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
    @BeforeAll static void setup() { GensokyoForestPreviewExporter.registries(); VanillaTreeTags.bind(); }

    @Test void magicMushroomsSurviveAtAnyLightLevelOnSuitableGround() {
        for (String colour : List.of("blue", "purple")) {
            var state = BuiltInRegistries.BLOCK.getValue(GensokyoForestBootstrap.id(colour + "_mushroom")).defaultBlockState();
            assertInstanceOf(MagicMushroomBlock.class, state.getBlock());
            assertEquals(MagicMushroomBlock.LIGHT_LEVEL, state.getLightEmission());
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            for (int brightness = 0; brightness <= 15; brightness++) {
                world.brightness = brightness;
                assertTrue(state.canSurvive(world.level, ORIGIN), colour + " brightness=" + brightness);
            }
            world.soil = Blocks.PODZOL.defaultBlockState();
            assertTrue(state.canSurvive(world.level, ORIGIN), colour + " on podzol");
            world.soil = Blocks.AIR.defaultBlockState();
            assertFalse(state.canSurvive(world.level, ORIGIN), colour + " needs a valid surface");
        }
        var brightGrass = new GensokyoTreeFeatureTest.MemoryWorld();
        brightGrass.brightness = 15;
        assertFalse(Blocks.BROWN_MUSHROOM.defaultBlockState().canSurvive(brightGrass.level, ORIGIN));
        assertFalse(Blocks.RED_MUSHROOM.defaultBlockState().canSurvive(brightGrass.level, ORIGIN));
    }

    @Test void everyConnectedCapStateEmitsAndNativeStemsAndMushroomsKeepTheirOriginalLight() {
        for (String colour : List.of("blue", "purple")) {
            var cap = BuiltInRegistries.BLOCK.getValue(GensokyoForestBootstrap.id(colour + "_mushroom_block"));
            assertInstanceOf(MagicMushroomCapBlock.class, cap);
            assertEquals(64, cap.getStateDefinition().getPossibleStates().size());
            assertTrue(cap.getStateDefinition().getPossibleStates().stream().allMatch(state -> state.getLightEmission() == MagicMushroomCapBlock.LIGHT_LEVEL));
        }
        assertEquals(0, BuiltInRegistries.BLOCK.getValue(GensokyoForestBootstrap.id("magic_mushroom_stem")).defaultBlockState().getLightEmission());
        assertEquals(1, Blocks.BROWN_MUSHROOM.defaultBlockState().getLightEmission());
        assertEquals(0, Blocks.RED_MUSHROOM.defaultBlockState().getLightEmission());
    }

    @Test void onlyMagicForestAddsGiantLayerAndBothSmallColours() {
        var registry = GensokyoForestPreviewExporter.registries();
        for (var theme : GensokyoForestType.values()) {
            var biome = registry.lookupOrThrow(Registries.BIOME).getOrThrow(GensokyoForestBootstrap.biome(theme)).value();
            assertEquals(theme == GensokyoForestType.MAGIC_FOREST ? 1 : 0,
                    biome.getGenerationSettings().features().stream().flatMap(s -> s.stream()).filter(h -> h.is(MagicMushroomBootstrap.GIANTS)).count());
            var config = GensokyoUnderstoryBootstrap.configuration(new GensokyoUnderstoryBootstrap.Profile(theme, GensokyoUnderstoryConfiguration.Kind.FLOOR_PATCH));
            long custom = config.accents().stream().filter(s -> s.getBlock() instanceof MagicMushroomBlock).count();
            assertEquals(theme == GensokyoForestType.MAGIC_FOREST ? 2 : 0, custom);
        }
    }

    @Test void giantVariantsPlaceTheirOwnMaterialsAndAreRecognisedByNativeBoneMeal() {
        for (boolean blue : new boolean[]{true, false}) for (int seed = 0; seed < 20; seed++) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            var configured = GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.CONFIGURED_FEATURE)
                    .getOrThrow(blue ? MagicMushroomBootstrap.HUGE_BLUE : MagicMushroomBootstrap.HUGE_PURPLE).value();
            assertInstanceOf(AbstractHugeMushroomFeature.class, configured.feature());
            assertTrue(place(world, blue, seed));
            assertTrue(world.states.values().stream().allMatch(s -> s.getBlock() instanceof HugeMushroomBlock));
            assertTrue(world.states.size() > 35);
            assertEquals(blue ? 7 : 8, world.states.keySet().stream().mapToInt(p -> p.getY() - 63).max().orElseThrow(), 2);
        }
    }

    @Test void obstructionWaterChestLogPathsHeightAndWriteBoundaryRejectWithoutAnyWrites() {
        for (boolean blue : new boolean[]{true, false}) {
            for (var block : List.of(Blocks.WATER, Blocks.CHEST, Blocks.OAK_LOG)) {
                var world = new GensokyoTreeFeatureTest.MemoryWorld();
                world.states.put(ORIGIN, block.defaultBlockState());
                assertFalse(place(world, blue, 0)); assertEquals(0, world.writes);
            }
            var path = new GensokyoTreeFeatureTest.MemoryWorld(); path.soil = Blocks.DIRT_PATH.defaultBlockState();
            assertFalse(place(path, blue, 0)); assertEquals(0, path.writes);
            var height = new GensokyoTreeFeatureTest.MemoryWorld(); height.maxY = 68;
            assertFalse(place(height, blue, 0)); assertEquals(0, height.writes);
            var edge = new GensokyoTreeFeatureTest.MemoryWorld(); edge.writable = p -> p.getX() >= 0;
            assertFalse(place(edge, blue, 0)); assertEquals(0, edge.writes);
        }
    }

    @Test void giantMushroomsReplaceCanopyLeavesButNotLogs() {
        for (boolean blue : new boolean[]{true, false}) {
            var canopy = new GensokyoTreeFeatureTest.MemoryWorld();
            for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) for (int y = 64; y <= 74; y++) {
                canopy.states.put(ORIGIN.offset(x, y - 64, z), Blocks.OAK_LEAVES.defaultBlockState());
            }
            assertTrue(place(canopy, blue, 0), (blue ? "blue" : "purple") + " giant mushroom blocked by canopy leaves");
            assertTrue(canopy.writes > 0);
        }
    }

    private static boolean place(GensokyoTreeFeatureTest.MemoryWorld world, boolean blue, int seed) {
        var configured = GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(blue ? MagicMushroomBootstrap.HUGE_BLUE : MagicMushroomBootstrap.HUGE_PURPLE).value();
        return ((MagicHugeMushroomFeature) configured.feature()).place(new FeaturePlaceContext<>(Optional.empty(), world.level, null,
                RandomSource.create(seed), ORIGIN, (HugeMushroomFeatureConfiguration) configured.config()));
    }
}
