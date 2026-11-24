package cc.thonly.reverie_dreams.paper.kitchen;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.io.IOException;

/** Rules copied from current KitchenRecipeType, ItemUtils.buildFoodTags and CraftingConflict. */
final class KitchenRecipes {
    static final NamespacedKey FOOD_OVERRIDE = new NamespacedKey("reverie_dreams", "food_properties");
    record Ingredient(String id, int count) {}
    record Recipe(String id, String type, List<Ingredient> ingredients, Ingredient output, double seconds) {}
    private static final Map<String, Recipe> RECIPES = new LinkedHashMap<>();
    private static final Map<String, List<String>> CONFLICTS = new LinkedHashMap<>();
    private static final Set<String> CUISINE = new LinkedHashSet<>();
    private static JsonObject foodDomain;

    static void load() {
        JsonObject root = read("/migration/kitchen-recipes.json");
        foodDomain = read("/migration/food-domain.json");
        RECIPES.clear();
        CONFLICTS.clear();
        CUISINE.clear();
        for (var element : root.getAsJsonArray("recipes")) {
            JsonObject recipe = element.getAsJsonObject();
            List<Ingredient> ingredients = new ArrayList<>();
            for (var value : recipe.getAsJsonArray("ingredients")) ingredients.add(ingredient(value.getAsJsonObject()));
            if (ingredients.isEmpty() || ingredients.size() > 5) throw new IllegalArgumentException("Invalid kitchen ingredient slots");
            String id = recipe.get("id").getAsString();
            RECIPES.put(id, new Recipe(id, recipe.get("recipe_type").getAsString(), List.copyOf(ingredients),
                    ingredient(recipe.getAsJsonObject("output")), recipe.has("cost_time") ? recipe.get("cost_time").getAsDouble() : 5.0));
        }
        for (var entry : root.getAsJsonObject("conflicts").entrySet()) {
            CONFLICTS.put(entry.getKey(), strings(entry.getValue().getAsJsonArray()));
        }
        CUISINE.addAll(strings(root.getAsJsonArray("cuisine_items")));
    }

    private static JsonObject read(String path) {
        try (var reader = new InputStreamReader(Objects.requireNonNull(KitchenRecipes.class.getResourceAsStream(path), path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }

    private static Ingredient ingredient(JsonObject value) {
        return new Ingredient(value.get("id").getAsString(), value.has("count") ? value.get("count").getAsInt() : 1);
    }

    static Recipe byId(String id) { return RECIPES.get(id); }

    static List<Recipe> matches(String type, ItemStack[] inputs) {
        return RECIPES.values().stream().filter(recipe -> recipe.type.equals(type) && matches(recipe, inputs)).toList();
    }

    private static boolean matches(Recipe recipe, ItemStack[] inputs) {
        // Preserve the source's independent anyMatch checks: repeated ingredients may match one slot.
        for (Ingredient ingredient : recipe.ingredients) {
            boolean found = false;
            for (int slot = 0; slot < 5; slot++) {
                ItemStack input = inputs[slot];
                if (!KitchenController.empty(input) && id(input).equals(ingredient.id) && input.getAmount() >= ingredient.count) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    static ItemStack preview(Recipe recipe, ItemStack[] inputs) {
        ItemStack output = build(recipe.output);
        if (output == null) return null;
        List<ItemStack> filtered = new ArrayList<>();
        for (int slot = 0; slot < 5; slot++) {
            if (!KitchenController.empty(inputs[slot]) && !CUISINE.contains(id(inputs[slot]))) filtered.add(inputs[slot]);
        }
        boolean[] used = new boolean[filtered.size()];
        for (Ingredient ingredient : recipe.ingredients) {
            for (int i = 0; i < filtered.size(); i++) {
                if (!used[i] && id(filtered.get(i)).equals(ingredient.id)) {
                    used[i] = true;
                    break;
                }
            }
        }
        List<String> base = properties(output);
        Set<String> combined = new LinkedHashSet<>(base);
        for (int i = 0; i < filtered.size(); i++) if (!used[i]) combined.addAll(properties(filtered.get(i)));
        List<String> result = List.copyOf(combined);
        if (!result.equals(base)) {
            JsonArray encoded = new JsonArray();
            result.forEach(encoded::add);
            output.editMeta(meta -> meta.getPersistentDataContainer().set(FOOD_OVERRIDE, PersistentDataType.STRING, encoded.toString()));
        }
        List<String> conflicts = CONFLICTS.getOrDefault(recipe.output.id, List.of());
        if (result.stream().anyMatch(conflicts::contains)) return darkCuisine();
        return output;
    }

    static ItemStack darkCuisine() { return build(new Ingredient(Ids.MOD_ID + ":cuisine/dark_cuisine", 1)); }

    static List<String> properties(ItemStack stack) {
        String encoded = stack.getPersistentDataContainer().get(FOOD_OVERRIDE, PersistentDataType.STRING);
        if (encoded != null) {
            List<String> override = strings(JsonParser.parseString(encoded).getAsJsonArray()).stream()
                    .filter(property -> foodDomain.getAsJsonObject("foodProperties").has(property)).distinct().toList();
            if (!override.isEmpty()) return override;
        }
        String id = id(stack);
        var item = foodDomain.getAsJsonObject("items").getAsJsonObject(id);
        if (item != null) return strings(item.getAsJsonArray("foodProperties"));
        List<String> result = new ArrayList<>();
        for (var entry : foodDomain.getAsJsonObject("foodProperties").entrySet()) {
            if (strings(entry.getValue().getAsJsonObject().getAsJsonArray("items")).contains(id)) result.add(entry.getKey());
        }
        return result;
    }

    static String id(ItemStack item) {
        var custom = CraftEngineItems.getCustomItemId(item);
        String id = custom == null ? item.getType().getKey().toString() : custom.asString();
        var aliases = foodDomain.getAsJsonObject("aliases");
        return aliases.has(id) ? aliases.get(id).getAsString() : id;
    }

    static ItemStack build(Ingredient ingredient) {
        ItemStack output;
        if (ingredient.id.startsWith("minecraft:")) {
            Material material = Material.matchMaterial(ingredient.id);
            if (material == null || !material.isItem()) return null;
            output = new ItemStack(material);
        } else {
            var definition = CraftEngineItems.byId(ingredient.id);
            if (definition == null) return null;
            output = definition.buildBukkitItem();
        }
        output.setAmount(ingredient.count);
        return output;
    }

    private static List<String> strings(JsonArray values) {
        if (values == null) return List.of();
        return values.asList().stream().map(value -> value.getAsString()).toList();
    }
}
