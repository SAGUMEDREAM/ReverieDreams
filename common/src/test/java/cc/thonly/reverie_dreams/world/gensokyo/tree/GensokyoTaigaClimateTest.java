package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoTaigaClimateTest {
    private static final int SEA_LEVEL = 1024;

    private Biome taiga() {
        return GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.BIOME)
                .getOrThrow(GensokyoForestBootstrap.biome(GensokyoForestType.TAIGA)).value();
    }

    @Test
    void ordinaryForestGroundAndTallTreeTopsReceiveRainAcrossTheAuthoredElevationRange() {
        var biome = taiga();
        // Current terrain: 1055..1271; include a 30-block canopy above its highest ground.
        for (int y : new int[]{1055, 1116, 1146, 1184, 1271, 1301}) {
            for (int x : new int[]{-128, 0, 6400, 12345}) {
                for (int z : new int[]{-256, 0, 8192, 10000}) {
                    var pos = new BlockPos(x, y, z);
                    assertTrue(biome.warmEnoughToRain(pos, SEA_LEVEL), pos::toString);
                    assertEquals(Biome.Precipitation.RAIN, biome.getPrecipitationAt(pos, SEA_LEVEL), pos::toString);
                }
            }
        }
    }

    @Test
    void generationAndWeatherSnowAndIcePredicatesRejectTheTemperateForest() {
        var biome = taiga();
        var level = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(),
                new Class<?>[]{LevelReader.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getSeaLevel")) return SEA_LEVEL;
                    throw new AssertionError("Warm biome must reject snow/ice before testing a surface: " + method);
                });
        for (int y : new int[]{1055, 1146, 1271, 1301}) {
            var pos = new BlockPos(6000, y, 6000);
            assertFalse(biome.shouldSnow(level, pos));
            assertFalse(biome.shouldFreeze(level, pos, false));
        }
    }

    @Test
    void vanillaClimateAndGenuineHighMountainSnowRemainIndependent() {
        var biomes = GensokyoForestPreviewExporter.registries().lookupOrThrow(Registries.BIOME);
        var vanilla = biomes.getOrThrow(Biomes.TAIGA).value();
        assertEquals(0.25F, vanilla.getBaseTemperature());
        assertTrue(vanilla.coldEnoughToSnow(new BlockPos(6000, 1146, 6000), SEA_LEVEL), "Reproduce the inherited climate bug");
        assertTrue(biomes.getOrThrow(Biomes.SNOWY_TAIGA).value()
                .coldEnoughToSnow(new BlockPos(6000, SEA_LEVEL, 6000), SEA_LEVEL));
        assertEquals(Biome.Precipitation.SNOW, taiga().getPrecipitationAt(new BlockPos(6000, 1500, 6000), SEA_LEVEL),
                "Altitude cooling was retained rather than globally suppressing snow");
    }

    @Test
    void generatedBiomeResourceMatchesTheBootstrapClimate() throws Exception {
        var path = Path.of(System.getProperty("reverie.taiga.resource",
                "src/generated/resources/data/reverie_dreams/worldgen/biome/gensokyo_taiga.json"));
        var resource = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        var registries = GensokyoForestPreviewExporter.registries();
        var encoded = Biome.DIRECT_CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), taiga()).getOrThrow().getAsJsonObject();
        for (String field : new String[]{"temperature", "downfall"}) {
            assertEquals(encoded.get(field).getAsFloat(), resource.get(field).getAsFloat(), 0.000001F, field);
        }
        for (String field : new String[]{"has_precipitation", "features"}) {
            assertEquals(encoded.get(field), resource.get(field), field);
        }
    }
}
