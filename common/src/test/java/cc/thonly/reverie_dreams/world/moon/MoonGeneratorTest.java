package cc.thonly.reverie_dreams.world.moon;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class MoonGeneratorTest {
    private static HolderLookup.Provider registries;
    private static Holder<NoiseGeneratorSettings> settings;
    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        // Load the production legacy noise router, substituting only the mod block in this loader-free JVM.
        Path json = Path.of("src/generated/resources/data/reverie_dreams/worldgen/noise_settings/moon.json");
        var raw = JsonParser.parseString(Files.readString(json).replace("reverie_dreams:moon_stone", "minecraft:end_stone"));
        settings = Holder.direct(NoiseGeneratorSettings.DIRECT_CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), raw).getOrThrow());
    }
    private MoonBiomeSource source() {
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        return new MoonBiomeSource(biomes.getOrThrow(Biomes.PLAINS), biomes.getOrThrow(Biomes.DESERT),
                biomes.getOrThrow(Biomes.ICE_SPIKES), biomes.getOrThrow(Biomes.OCEAN));
    }
    @Test void columnsHeightsAndMaterialsAgreeWithSubtractiveRelief() {
        long seed = 20261105;
        var source = source(); var generator = new MoonChunkGenerator(source, settings);
        var random = RandomState.create(settings.value(), registries.lookupOrThrow(Registries.NOISE), seed);
        // Standalone vanilla bootstrap has unresolved structure tags; the runtime probe covers actual structures.
        generator.createState(registries.lookupOrThrow(Registries.STRUCTURE_SET).filterElements(s -> false), random, seed);
        var baseline = new NoiseBasedChunkGenerator(source, settings);
        var height = LevelHeightAccessor.create(MoonTerrain.MIN_Y, MoonTerrain.HEIGHT);
        var seen = EnumSet.noneOf(MoonTerrain.Region.class);
        for (int x = -2048; x <= 2048 && seen.size() < 4; x += 32) for (int z = -2048; z <= 2048 && seen.size() < 4; z += 32) {
            var sample = MoonTerrain.sample(seed, x, z);
            if (!seen.add(sample.region())) continue;
            var column = generator.getBaseColumn(x, z, height, random);
            int oldTop = baseline.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random) - 1;
            int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random) - 1;
            int top = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, random) - 1;
            assertTrue(floor <= oldTop);
            assertTrue(column.getBlock(-128).is(Blocks.BEDROCK));
            assertFalse(column.getBlock(-127).isAir());
            assertFalse(column.getBlock(floor).isAir());
            assertTrue(column.getBlock(top + 1).isAir());
            if (top > floor) assertTrue(column.getBlock(top).is(Blocks.WATER));
            if (sample.region() == MoonTerrain.Region.BASALT_MARE) assertTrue(column.getBlock(floor).is(Blocks.SMOOTH_BASALT));
        }
        assertEquals(4, seen.size());
        generator.setReliefStrength((x, z) -> 0);
        for (int x = -16; x <= 16; x += 16) assertEquals(
                baseline.getBaseHeight(x, 0, Heightmap.Types.OCEAN_FLOOR_WG, height, random),
                generator.getBaseHeight(x, 0, Heightmap.Types.OCEAN_FLOOR_WG, height, random));
    }
    @Test void generatorAndFourBiomeSourceRoundTripThroughTheRealCodecs() {
        var generator = new MoonChunkGenerator(source(), settings);
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var encoded = MoonChunkGenerator.CODEC.codec().encodeStart(ops, generator).getOrThrow();
        var decoded = MoonChunkGenerator.CODEC.codec().parse(ops, encoded).getOrThrow();
        assertEquals(4, decoded.getBiomeSource().possibleBiomes().size());
        assertEquals(MoonTerrain.MIN_Y, decoded.getMinY());
        assertEquals(MoonTerrain.HEIGHT, decoded.getGenDepth());
        assertEquals(settings.value(), decoded.settings().value());
    }
}

