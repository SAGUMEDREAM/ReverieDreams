package cc.thonly.reverie_dreams.world.gensokyo.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoMapHeaderTest {
    private JsonObject header() {
        return JsonParser.parseString("""
                {"format_version":1,"map_version":2,"width":12800,"depth":10240,
                 "min_y":0,"world_height":1536,"sea_level":1024}
                """).getAsJsonObject();
    }

    @Test void validatesTheRealMapSizeWithoutAllocatingAnyLayers() {
        assertEquals(131072000, GensokyoMapValidator.validateHeader(header()));
        for (int dimension : new int[]{0, -1, Integer.MAX_VALUE}) {
            var root = header();
            root.addProperty("width", dimension);
            assertThrows(IllegalStateException.class, () -> GensokyoMapValidator.validateHeader(root));
        }
        var root = header();
        root.addProperty("width", 32768);
        root.addProperty("depth", 32768);
        assertThrows(IllegalStateException.class, () -> GensokyoMapValidator.validateHeader(root));
    }

    @Test void rejectsVersionHeightAndHeightOverflowBeforeReadingLayers() {
        for (String key : new String[]{"format_version", "map_version", "world_height", "min_y"}) {
            var root = header();
            root.addProperty(key, key.equals("min_y") ? Integer.MAX_VALUE : 0);
            assertThrows(IllegalStateException.class, () -> GensokyoMapValidator.validateHeader(root));
        }
    }

    @Test void loaderRejectsOversizedMetadataBeforeAccessingMissingSpawnAndLayers() {
        var error = assertThrows(IllegalStateException.class,
                () -> GensokyoMapLoader.get(Identifier.fromNamespaceAndPath("test", "oversized_map")));
        Throwable cause = error;
        while (cause.getCause() != null) cause = cause.getCause();
        assertTrue(cause.getMessage().contains("too large"), cause.toString());
    }
}
