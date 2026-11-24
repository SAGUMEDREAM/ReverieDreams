package cc.thonly.reverie_dreams.entity;

import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScarecrowResourcesTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static byte[] bytes(String name) throws Exception {
        try (var in = ScarecrowResourcesTest.class.getClassLoader().getResourceAsStream(name)) {
            assertNotNull(in, name);
            return in.readAllBytes();
        }
    }

    private static JsonObject json(String name) throws Exception {
        return JsonParser.parseString(new String(bytes(name), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test void nativeGeometryBakesWithTheStaticScarecrowRig() throws Exception {
        var geometry = json("assets/reverie_dreams/geckolib/models/entity/scarecrow.geo.json");
        var model = new GeckoLibGsonLoader().bakeGeckoLibModelFile(
                Identifier.fromNamespaceAndPath("reverie_dreams", "scarecrow"), geometry);
        for (var name : Set.of("root", "frame", "shirt", "right_arm", "left_arm", "sack", "hat", "scarf")) {
            assertTrue(model.getBone(name).isPresent(), name);
        }
        int cubes = 0;
        for (var entry : geometry.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")) {
            var bone = entry.getAsJsonObject();
            if (bone.has("cubes")) cubes += bone.getAsJsonArray("cubes").size();
        }
        assertEquals(30, cubes);
    }

    @Test void nativeAndBilUseTheSameAtlasWithUniformOpaqueFaceTexels() throws Exception {
        var source = json("model/reverie_dreams/scarecrow.bbmodel");
        byte[] png = bytes("assets/reverie_dreams/textures/entity/scarecrow.png");
        var embedded = source.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(png, Base64.getDecoder().decode(embedded.split(",", 2)[1]));
        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
        var used = new HashSet<Integer>();
        int faces = 0;
        for (var entry : source.getAsJsonArray("elements")) {
            var cube = entry.getAsJsonObject();
            if (!cube.get("type").getAsString().equals("cube")) continue;
            var from = cube.getAsJsonArray("from");
            var to = cube.getAsJsonArray("to");
            for (var entryFace : cube.getAsJsonObject("faces").entrySet()) {
                var side = entryFace.getKey();
                var uv = entryFace.getValue().getAsJsonObject().getAsJsonArray("uv");
                int uAxis = side.equals("east") || side.equals("west") ? 2 : 0;
                int vAxis = side.equals("up") || side.equals("down") ? 2 : 1;
                int x = uv.get(0).getAsInt(), y = uv.get(1).getAsInt();
                int x1 = uv.get(2).getAsInt(), y1 = uv.get(3).getAsInt();
                assertEquals(to.get(uAxis).getAsDouble() - from.get(uAxis).getAsDouble(), x1 - x, 1e-6);
                assertEquals(to.get(vAxis).getAsDouble() - from.get(vAxis).getAsDouble(), y1 - y, 1e-6);
                for (int v = y; v < y1; v++) for (int u = x; u < x1; u++) {
                    assertTrue(used.add(v * 128 + u), "Overlapping UV texel");
                    assertEquals(255, image.getRGB(u, v) >>> 24, cube.get("name").getAsString());
                }
                faces++;
            }
        }
        assertEquals(180, faces);
    }
}
