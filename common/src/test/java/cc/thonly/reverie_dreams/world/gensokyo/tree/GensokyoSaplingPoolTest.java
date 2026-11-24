package cc.thonly.reverie_dreams.world.gensokyo.tree;

import cc.thonly.reverie_dreams.world.sapling.GensokyoSaplings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoSaplingPoolTest {
    private static NoiseBasedChunkGenerator generator;
    @BeforeAll
    static void bootstrap() {
        GensokyoForestPreviewExporter.registries();
        VanillaTreeTags.bind();
        var registries = GensokyoForestPreviewExporter.registries();
        generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)),
                registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD));
    }

    @Test
    void eachNativeGrowerSelectsItsExistingForestPoolWithOrWithoutFlowers() throws Exception {
        var select = TreeGrower.class.getDeclaredMethod("getConfiguredFeature", RandomSource.class, boolean.class);
        select.setAccessible(true);
        assertEquals(GensokyoForestType.values().length, GensokyoSaplings.entries().size());
        var names = new HashSet<String>();
        for (var entry : GensokyoSaplings.entries()) {
            assertTrue(names.add(entry.path()));
            for (boolean flowers : new boolean[]{true, false}) {
                assertEquals(entry.pool(), select.invoke(entry.grower(), RandomSource.create(42), flowers));
            }
            var configured = GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.CONFIGURED_FEATURE)
                    .getOrThrow(entry.pool()).value();
            assertSame(Feature.RANDOM_SELECTOR, configured.feature());
            var pool = (RandomFeatureConfiguration) configured.config();
            assertEquals(entry.forest().mix().size(), pool.features.size() + 1);
        }
    }

    @Test
    void saplingConfiguredPoolsGrowOutsideTheirNaturalBiomeAndKeepSupportedLeaves() {
        var lookup = GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.CONFIGURED_FEATURE);
        for (var entry : GensokyoSaplings.entries()) {
            for (long seed : new long[]{1, 42, 20261119}) {
                var world = new GensokyoTreeFeatureTest.MemoryWorld();
                // No biome is supplied: a planted sapling must not inherit BiomeFilter or density/clearing checks.
                assertTrue(lookup.getOrThrow(entry.pool()).value().place(world.level, generator,
                        RandomSource.create(seed), new BlockPos(0, 64, 0)), entry.path());
                assertTrue(world.writes > 0);
                assertEquals(Blocks.GRASS_BLOCK.defaultBlockState(), world.state(new BlockPos(0, 63, 0)));
                assertTrue(world.states.values().stream().filter(s -> s.hasProperty(LeavesBlock.DISTANCE))
                        .allMatch(s -> s.getValue(LeavesBlock.DISTANCE) <= 6 && !s.getValue(LeavesBlock.PERSISTENT)));
            }
        }
    }

    @Test
    void obstructedGrowthPoolPerformsNoPartialWrites() {
        var lookup = GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.CONFIGURED_FEATURE);
        for (var entry : GensokyoSaplings.entries()) {
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            var pos = new BlockPos(0, 64, 0);
            world.states.put(pos, Blocks.STONE.defaultBlockState());
            assertFalse(lookup.getOrThrow(entry.pool()).value().place(world.level, generator, RandomSource.create(42), pos));
            assertEquals(0, world.writes, entry.path());
        }
    }
}
