package cc.thonly.reverie_dreams.paper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftEnginePackTest {
    private final Path pack = Path.of(System.getProperty("craftengine.packDirectory"));

    @Test
    void contentUsesVanillaMaterialsAndResolvableCustomItemReferences() throws Exception {
        Set<String> itemIds = new HashSet<>();
        Map<String, Integer> itemTags = new LinkedHashMap<>();
        try (var files = Files.list(pack.resolve("configuration"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                YamlConfiguration config = readYaml(file);
                ConfigurationSection items = config.getConfigurationSection("items");
                if (items == null) continue;
                for (String id : items.getKeys(false)) {
                    assertTrue(itemIds.add(id), "Duplicate custom item: " + id);
                    String material = items.getString(id + ".material");
                    assertNotNull(material, "Missing vanilla material: " + id);
                    assertNotNull(Material.matchMaterial(material), "Invalid vanilla material for " + id + ": " + material);
                    Object model = items.get(id + ".model");
                    assertNotNull(model, "Missing model: " + id);
                    assertTrue(checkModelDefinition(model) > 0, "No resolvable model paths: " + id);
                    for (String tag : items.getStringList(id + ".settings.tags")) {
                        itemTags.merge(tag, 1, Integer::sum);
                    }
                }
            }
        }
        assertTrue(itemIds.contains("reverie_dreams:silver_ingot"));
        assertTrue(itemIds.contains("reverie_dreams:silver_block"));
        assertTrue(itemIds.contains("reverie_dreams:chair"));

        for (ConfigurationSection recipe : recipes().values()) {
            if ("dye".equals(recipe.getString("type"))) {
                assertItemReference(itemIds, itemTags, recipe.getString("target"));
                assertItemReference(itemIds, itemTags, recipe.getString("dye"));
                continue;
            }
            assertItemReference(itemIds, itemTags, recipe.getString("result.id"));
            ConfigurationSection shaped = recipe.getConfigurationSection("ingredients");
            if (shaped != null) {
                for (String symbol : shaped.getKeys(false)) assertItemReference(itemIds, itemTags, shaped.getString(symbol));
            } else {
                for (String ingredient : recipe.getStringList("ingredients")) assertItemReference(itemIds, itemTags, ingredient);
                if (recipe.isString("ingredients")) assertItemReference(itemIds, itemTags, recipe.getString("ingredients"));
            }
            if (recipe.contains("ingredient")) assertItemReference(itemIds, itemTags, recipe.getString("ingredient"));
        }
    }

    @Test
    void migratedRecipesPreserveSourceInputsOutputsAndCookingSettings() throws Exception {
        Path originals = Path.of(System.getProperty("mod.recipeDirectory"));
        for (var entry : recipes().entrySet()) {
            String id = entry.getKey();
            ConfigurationSection migrated = entry.getValue();
            // These deliberate adapters have no original JSON. Reference closure
            // and actual server crafting cover them separately from source parity.
            if (id.startsWith("reverie_dreams:compat/") || "dye".equals(migrated.getString("type"))) continue;
            Path original = originals.resolve(id.substring(id.indexOf(':') + 1) + ".json");
            JsonObject source = JsonParser.parseString(Files.readString(original, StandardCharsets.UTF_8)).getAsJsonObject();
            String type = source.get("type").getAsString().substring("minecraft:".length()).replace("crafting_", "");
            assertEquals(type, migrated.getString("type"), id);
            JsonObject result = source.getAsJsonObject("result");
            assertEquals(result.get("id").getAsString(), migrated.getString("result.id"), id);
            assertEquals(result.has("count") ? result.get("count").getAsInt() : 1, migrated.getInt("result.count"), id);
            if (source.has("cookingtime")) {
                assertEquals(source.get("cookingtime").getAsInt(), migrated.getInt("time"), id);
                assertEquals(source.get("experience").getAsDouble(), migrated.getDouble("experience"), 0.00001, id);
                assertEquals(source.get("ingredient").getAsString(), migrated.getString("ingredient"), id);
                assertEquals(source.get("group").getAsString(), migrated.getString("group"), id);
            } else if (type.equals("stonecutting")) {
                assertEquals(source.get("ingredient").getAsString(), migrated.getString("ingredients"), id);
            } else if (type.equals("shaped")) {
                var pattern = new ArrayList<String>();
                source.getAsJsonArray("pattern").forEach(value -> pattern.add(value.getAsString()));
                assertEquals(pattern, migrated.getStringList("pattern"), id);
                var actualKeys = migrated.getConfigurationSection("ingredients");
                assertNotNull(actualKeys, id);
                assertEquals(source.getAsJsonObject("key").keySet(), actualKeys.getKeys(false), id);
                for (var ingredient : source.getAsJsonObject("key").entrySet()) {
                    assertEquals(ingredient.getValue().getAsString(), actualKeys.getString(ingredient.getKey()), id);
                }
            } else {
                var ingredients = new ArrayList<String>();
                source.getAsJsonArray("ingredients").forEach(value -> ingredients.add(value.getAsString()));
                assertEquals(ingredients, migrated.getStringList("ingredients"), id);
            }
        }
    }

    private Map<String, ConfigurationSection> recipes() throws Exception {
        Map<String, ConfigurationSection> result = new LinkedHashMap<>();
        try (var files = Files.list(pack.resolve("configuration"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).sorted().toList()) {
                ConfigurationSection recipes = readYaml(file).getConfigurationSection("recipes");
                if (recipes == null) continue;
                for (String id : recipes.getKeys(false)) {
                    assertTrue(!result.containsKey(id), "Duplicate recipe: " + id);
                    result.put(id, recipes.getConfigurationSection(id));
                }
            }
        }
        assertTrue(!result.isEmpty());
        return result;
    }

    @Test
    void customModelParentsAndTexturesAreBundled() throws IOException {
        try (var files = Files.walk(pack.resolve("resourcepack/assets/reverie_dreams/models"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonObject model = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                if (model.has("parent")) assertResourceExists("models", model.get("parent").getAsString(), ".json");
                JsonObject textures = model.getAsJsonObject("textures");
                if (textures != null) {
                    for (JsonElement texture : textures.asMap().values()) {
                        String reference = texture.getAsString();
                        if (!reference.startsWith("#")) assertResourceExists("textures", reference, ".png");
                    }
                }
            }
        }
    }

    private static YamlConfiguration readYaml(Path file) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
        return config;
    }

    private static void assertItemReference(Set<String> ids, Map<String, Integer> tags, String reference) {
        assertNotNull(reference);
        if (reference.startsWith("#")) {
            if (reference.startsWith("#minecraft:")) return;
            String tag = reference.substring(1);
            assertTrue(reference.startsWith("#reverie_dreams:") && tags.getOrDefault(tag, 0) > 0, "Unresolved tag: " + reference);
            return;
        }
        assertTrue(reference.startsWith("minecraft:") || ids.contains(reference), "Unresolved item: " + reference);
    }

    private void assertResourceExists(String directory, String reference, String extension) {
        String[] parts = reference.split(":", 2);
        if (parts.length == 1 || parts[0].equals("minecraft")) return;
        Path resource = pack.resolve("resourcepack/assets/" + parts[0] + "/" + directory + "/" + parts[1] + extension).normalize();
        assertTrue(resource.startsWith(pack), "Resource outside pack: " + reference);
        assertTrue(Files.isRegularFile(resource), "Missing resource: " + resource);
    }

    private int checkModelDefinition(Object value) {
        if (value instanceof String reference) {
            assertResourceExists("models", reference, ".json");
            return 1;
        }
        if (value instanceof ConfigurationSection section) return checkModelDefinition(section.getValues(false));
        int count = 0;
        if (value instanceof Map<?, ?> fields) {
            for (var entry : fields.entrySet()) {
                Object child = entry.getValue();
                if ((entry.getKey().equals("path") || entry.getKey().equals("model") || entry.getKey().equals("base") || entry.getKey().equals("fallback")) && child instanceof String) {
                    count += checkModelDefinition(child);
                } else if (child instanceof ConfigurationSection || child instanceof Map<?, ?> || child instanceof List<?>) count += checkModelDefinition(child);
            }
        } else if (value instanceof List<?> children) {
            for (Object child : children) if (!(child instanceof String)) count += checkModelDefinition(child);
        }
        return count;
    }

    @Test
    void equipmentLayersReferenceBundledOriginalTextures() throws IOException {
        Path directory = pack.resolve("resourcepack/assets/reverie_dreams/equipment");
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonObject layers = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("layers");
                assertTrue(!layers.isEmpty(), "No wearable layers: " + file);
                for (var entry : layers.entrySet()) for (var element : entry.getValue().getAsJsonArray()) {
                    String texture = element.getAsJsonObject().get("texture").getAsString();
                    String[] parts = texture.split(":", 2);
                    if (parts.length == 2 && parts[0].equals("reverie_dreams")) assertResourceExists("textures", parts[0] + ":entity/equipment/" + entry.getKey() + "/" + parts[1], ".png");
                }
            }
        }
    }
}
