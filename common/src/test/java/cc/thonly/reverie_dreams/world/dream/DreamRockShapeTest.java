package cc.thonly.reverie_dreams.world.dream;

import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import cc.thonly.reverie_dreams.world.gen.biome.DreamBiomeCreator;
import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.*;

class DreamRockShapeTest {
    @Test void productionSizesAreSmallFlattenedConnectedShapesWithoutSingleBlockDebris() {
        for (int radius = 1; radius <= 4; radius++) for (int seed = 0; seed < 128; seed++) {
            var shape = DreamFloatingRockShape.create(seed, radius);
            var cells = new HashSet<>(shape.blocks());
            assertEquals(cells.size(), shape.blocks().size());
            assertTrue(cells.contains(BlockPos.ZERO));
            assertTrue(cells.size() >= 7 && cells.size() < 200);
            assertTrue(shape.radiusX() <= 4 && shape.radiusZ() <= 4 && shape.radiusY() <= 2);
            for (var pos : cells) {
                assertTrue(Math.abs(pos.getX()) <= shape.radiusX());
                assertTrue(Math.abs(pos.getY()) <= shape.radiusY());
                assertTrue(Math.abs(pos.getZ()) <= shape.radiusZ());
            }
            var reached = new HashSet<BlockPos>();
            var pending = new ArrayDeque<BlockPos>();
            pending.add(BlockPos.ZERO); reached.add(BlockPos.ZERO);
            while (!pending.isEmpty()) {
                var pos = pending.removeFirst();
                for (Direction direction : Direction.values()) {
                    var next = pos.relative(direction);
                    if (cells.contains(next) && reached.add(next)) pending.add(next);
                }
            }
            assertEquals(cells, reached, "No detached pixels or floating sub-fragments");
        }
    }

    @Test void seedsAreRepeatableWithVisibleAspectVariationAndImmutableResults() {
        var shapes = new HashSet<DreamFloatingRockShape.Shape>();
        for (int seed = 0; seed < 64; seed++) {
            var shape = DreamFloatingRockShape.create(seed, 4);
            assertEquals(shape, DreamFloatingRockShape.create(seed, 4));
            assertThrows(UnsupportedOperationException.class, () -> shape.blocks().clear());
            shapes.add(shape);
        }
        assertTrue(shapes.size() >= 3, "Avoid replacing every old sphere with the same new rock");
        assertThrows(IllegalArgumentException.class, () -> DreamFloatingRockShape.create(0, 0));
        assertThrows(IllegalArgumentException.class, () -> DreamFloatingRockShape.create(0, 6));
    }

    @Test void authoredRockResourcesReduceAttemptsAndUseSmallNonzeroRadiiAndAbsoluteHeight() throws Exception {
        for (String name : new String[]{"floating_dream_stone", "floating_dream_crystal"}) {
            var config = json("configured_feature/" + name + ".json").getAsJsonObject("config");
            assertTrue(config.get("min_radius").getAsInt() >= 1);
            assertTrue(config.get("max_radius").getAsInt() <= 4);
            assertTrue(config.get("min_y").getAsInt() >= 0);
            assertTrue(config.get("max_y").getAsInt() <= 116);
            var placement = json("placed_feature/" + name + "_placed.json").getAsJsonArray("placement");
            boolean rarity = false;
            for (var modifier : placement) {
                var value = modifier.getAsJsonObject();
                assertNotEquals("minecraft:height_range", value.get("type").getAsString(),
                        "Do not randomize origin Y then add the configured height a second time");
                if (value.get("type").getAsString().equals("minecraft:rarity_filter")) {
                    rarity = true;
                    int previous = name.endsWith("stone") ? 11 : 6;
                    assertTrue(value.get("chance").getAsInt() >= previous * 3);
                }
            }
            assertTrue(rarity);
        }
    }

    @Test void dreamWaterResourceHasPinkChannelsAndMatchesItsSeparateUnderwaterFog() throws Exception {
        var biome = json("biome/dream.json");
        String surface = biome.getAsJsonObject("effects").get("water_color").getAsString();
        String fog = biome.getAsJsonObject("attributes").get("minecraft:visual/water_fog_color").getAsString();
        int water = Integer.parseInt(surface.substring(1), 16), underwater = Integer.parseInt(fog.substring(1), 16);
        assertEquals(DreamBiomeCreator.DREAM_WATER_COLOR, water);
        assertEquals(DreamBiomeCreator.DREAM_WATER_FOG_COLOR, underwater);
        assertTrue(((water >> 16) & 255) > ((water >> 8) & 255));
        assertTrue((water & 255) > ((water >> 8) & 255));
        assertNotEquals(surface, biome.getAsJsonObject("attributes").get("minecraft:visual/fog_color").getAsString(),
                "Water changes must not recolor the entire dream atmosphere");
    }

    private static JsonObject json(String file) throws Exception {
        var root = Path.of(System.getProperty("reverie.dream.workspace", ".."));
        return JsonParser.parseString(Files.readString(root.resolve(
                "common/src/generated/resources/data/reverie_dreams/worldgen/" + file))).getAsJsonObject();
    }
}
