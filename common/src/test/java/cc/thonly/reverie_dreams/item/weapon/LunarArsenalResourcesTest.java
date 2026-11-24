package cc.thonly.reverie_dreams.item.weapon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class LunarArsenalResourcesTest {
    private static final String ASSETS = "assets/reverie_dreams/";
    private static final String DATA = "data/reverie_dreams/";

    @Test void everyItemDefinitionResolvesToAMinecraftReadableModel() throws Exception {
        for (var type : LunarWeaponType.values()) for (var id : new String[]{type.itemPath(), type.magazinePath()}) {
            var definition = json(ASSETS + "items/" + id + ".json").getAsJsonObject("model");
            assertEquals("minecraft:model", definition.get("type").getAsString());
            assertEquals("reverie_dreams:item/" + id, definition.get("model").getAsString());
            var parsed = CuboidModel.fromStream(new StringReader(json(ASSETS + "models/item/" + id + ".json").toString()));
            assertNotNull(parsed);
            if (id.equals(LunarWeaponType.RIFLE.itemPath())) {
                assertEquals("reverie_dreams:item/weapon_of_the_moon", parsed.parent().toString());
                assertNotNull(CuboidModel.fromStream(new StringReader(json(ASSETS + "models/item/weapon_of_the_moon.json").toString())).geometry());
            } else assertNotNull(parsed.geometry(), id);
        }
    }

    @Test void newModelsKeepOneConsistentTexelDensityAndClassicCubeRotations() throws Exception {
        int bitmapWidth;
        try (var stream = getClass().getClassLoader().getResourceAsStream(ASSETS + "textures/item/lunar_arsenal.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertEquals(image.getWidth(), image.getHeight());
            bitmapWidth = image.getWidth();
        }
        for (var type : LunarWeaponType.values()) for (var id : new String[]{type.itemPath(), type.magazinePath()}) {
            if (id.equals(LunarWeaponType.RIFLE.itemPath())) continue;
            var model = json(ASSETS + "models/item/" + id + ".json");
            assertEquals("reverie_dreams:item/lunar_arsenal", model.getAsJsonObject("textures").get("0").getAsString());
            for (var entry : model.getAsJsonArray("elements")) {
                var e = entry.getAsJsonObject();
                var from = e.getAsJsonArray("from"); var to = e.getAsJsonArray("to");
                double[] size = new double[3];
                for (int axis = 0; axis < 3; axis++) {
                    double min = from.get(axis).getAsDouble(), max = to.get(axis).getAsDouble();
                    assertTrue(min >= -16 && max <= 32 && max > min, id);
                    size[axis] = max - min;
                }
                if (e.has("rotation")) {
                    var r = e.getAsJsonObject("rotation");
                    assertTrue(r.has("axis") && r.has("angle"));
                    assertFalse(r.has("x") || r.has("y") || r.has("z"));
                    assertEquals(0, r.get("angle").getAsDouble() % 22.5, 1e-9);
                }
                for (var face : e.getAsJsonObject("faces").entrySet()) {
                    var f = face.getValue().getAsJsonObject(); var uv = f.getAsJsonArray("uv");
                    assertEquals("#0", f.get("texture").getAsString());
                    double w = switch (face.getKey()) { case "east", "west" -> size[2]; default -> size[0]; };
                    double h = switch (face.getKey()) { case "up", "down" -> size[2]; default -> size[1]; };
                    assertEquals(2, Math.abs(uv.get(2).getAsDouble() - uv.get(0).getAsDouble()) * bitmapWidth / 16 / w, 1e-6, id);
                    assertEquals(2, Math.abs(uv.get(3).getAsDouble() - uv.get(1).getAsDouble()) * bitmapWidth / 16 / h, 1e-6, id);
                    uv.forEach(value -> assertTrue(value.getAsDouble() >= 0 && value.getAsDouble() <= 16, id));
                }
            }
        }
    }

    @Test void recipesHaveDistinctCraftingGridsAndCorrectUnlocksIncludingTheLegacyMagazine() throws Exception {
        Set<String> patterns = new HashSet<>();
        for (var type : LunarWeaponType.values()) for (boolean magazine : new boolean[]{false, true}) {
            String item = magazine ? type.magazinePath() : type.itemPath();
            String recipe = magazine && type == LunarWeaponType.RIFLE ? "magazine" : item;
            var data = json(DATA + "recipe/" + recipe + ".json");
            assertEquals("reverie_dreams:" + item, data.getAsJsonObject("result").get("id").getAsString());
            var key = data.getAsJsonObject("key");
            StringBuilder normal = new StringBuilder(), mirrored = new StringBuilder();
            for (var row : data.getAsJsonArray("pattern")) {
                String line = row.getAsString();
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i), reverse = line.charAt(line.length() - 1 - i);
                    normal.append(c == ' ' ? "air" : key.get(String.valueOf(c))).append(';');
                    mirrored.append(reverse == ' ' ? "air" : key.get(String.valueOf(reverse))).append(';');
                }
                normal.append('/'); mirrored.append('/');
            }
            assertTrue(patterns.add(normal.toString()), item);
            if (!normal.toString().equals(mirrored.toString())) assertTrue(patterns.add(mirrored.toString()), item);
            var advancement = json(DATA + "advancement/recipes/combat/" + recipe + ".json");
            assertEquals("reverie_dreams:" + recipe, advancement.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
        }
    }

    @Test void allFourLanguagesNameAndDescribeEachWeaponAndItsOwnMagazine() throws Exception {
        for (var locale : new String[]{"zh_cn", "zh_tw", "en_us", "ja_jp"}) {
            var language = json(ASSETS + "lang/" + locale + ".json");
            Set<String> names = new HashSet<>();
            for (var type : LunarWeaponType.values()) for (var id : new String[]{type.itemPath(), type.magazinePath()}) {
                String key = "item.reverie_dreams." + id;
                assertTrue(names.add(language.get(key).getAsString()), locale + ':' + id);
                assertFalse(language.get(key + ".description").getAsString().isBlank());
            }
            assertFalse(language.get("item.reverie_dreams.lunar_weapon.controls").getAsString().isBlank());
        }
    }

    private JsonObject json(String path) throws Exception {
        var resource = getClass().getClassLoader().getResourceAsStream(path);
        // Common exports generated resources to loader projects, separately from its own test classpath.
        for (Path directory = Path.of("").toAbsolutePath(); resource == null && directory != null; directory = directory.getParent()) {
            Path generated = directory.resolve("common/src/generated/resources").resolve(path);
            if (Files.isRegularFile(generated)) resource = Files.newInputStream(generated);
        }
        try (var stream = resource) {
            assertNotNull(stream, path);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }
}
