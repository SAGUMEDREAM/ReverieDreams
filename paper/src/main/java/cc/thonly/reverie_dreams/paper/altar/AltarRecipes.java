package cc.thonly.reverie_dreams.paper.altar;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.io.IOException;

/** Ordered exact-slot altar recipes. Unimplemented output definitions fail before inventory mutation. */
final class AltarRecipes {
    record Ingredient(String id, int count) { boolean empty() { return count <= 0 || id.equals("minecraft:air"); } }
    record Recipe(String id, Ingredient core, List<Ingredient> slots, JsonObject output) {}
    record Plan(String id, int[] counts, ItemStack output, String unavailable) {}
    private static final List<Recipe> RECIPES = new ArrayList<>();
    private static final Set<String> CARD_ADAPTERS = new HashSet<>();
    private static Function<String, ItemStack> cards;

    static void load(Function<String, ItemStack> cardFactory) {
        cards = Objects.requireNonNull(cardFactory);
        RECIPES.clear(); CARD_ADAPTERS.clear();
        JsonObject root = read("/migration/altar-recipes.json");
        for (var value : root.getAsJsonArray("recipes")) {
            JsonObject json = value.getAsJsonObject();
            List<Ingredient> slots = new ArrayList<>();
            for (var slot : json.getAsJsonArray("slots")) slots.add(ingredient(slot.getAsJsonObject()));
            while (slots.size() < 8) slots.add(new Ingredient("minecraft:air", 0));
            if (slots.size() != 8) throw new IllegalArgumentException("Altar recipe must have eight outer positions");
            RECIPES.add(new Recipe(json.get("id").getAsString(), ingredient(json.getAsJsonObject("core")),
                    List.copyOf(slots), json.getAsJsonObject("output").deepCopy()));
        }
        CARD_ADAPTERS.addAll(read("/migration/npc-domain.json").getAsJsonObject("interaction")
                .getAsJsonObject("roleCardItems").keySet());
    }

    static JsonObject read(String path) {
        try (var reader = new InputStreamReader(Objects.requireNonNull(AltarRecipes.class.getResourceAsStream(path), path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException failure) { throw new IllegalStateException("Cannot read " + path, failure); }
    }

    static Ingredient ingredient(JsonObject object) {
        // IngredientStack's codec defaults count to ZERO, particularly for explicit AIR placeholders.
        return new Ingredient(object.get("id").getAsString(), object.has("count") ? object.get("count").getAsInt() : 0);
    }

    static String id(ItemStack stack) {
        if (AltarController.empty(stack)) return "minecraft:air";
        var custom = CraftEngineItems.getCustomItemId(stack);
        String id = custom == null ? stack.getType().getKey().toString() : custom.asString();
        // The original nineteen variants share the same Item and differ only by components.
        return CARD_ADAPTERS.contains(id) ? Ids.MOD_ID + ":role_card" : id;
    }

    private static boolean matches(Ingredient required, ItemStack input) {
        if (required.empty()) return AltarController.empty(input);
        return !AltarController.empty(input) && required.id.equals(id(input)) && input.getAmount() >= required.count;
    }

    static Plan match(ItemStack[] inputs) {
        boolean diamonds = true;
        for (int slot = 0; slot < 8; slot++) diamonds &= matches(new Ingredient("minecraft:diamond", 2), inputs[slot]);
        if (diamonds && id(inputs[8]).equals(Ids.MOD_ID + ":role_archive")) {
            // Setting an arbitrary PDC flag is not a replacement for the NPC service's missing respawn transaction.
            return new Plan(Ids.MOD_ID + ":role_archive", null, null, "archive_respawn_not_migrated");
        }
        if (diamonds && id(inputs[8]).equals(Ids.MOD_ID + ":spell_card_template")) {
            int[] amounts = {2, 2, 2, 2, 2, 2, 2, 2, inputs[8].getAmount()};
            ItemStack copy = inputs[8].clone(); copy.setAmount(2);
            return new Plan(Ids.MOD_ID + ":copy_spell_card_template", amounts, copy, null);
        }
        for (Recipe recipe : RECIPES) {
            if (!matches(recipe.core, inputs[8])) continue;
            boolean match = true;
            for (int slot = 0; slot < 8; slot++) if (!matches(recipe.slots.get(slot), inputs[slot])) { match = false; break; }
            if (!match) continue;
            ItemStack output;
            JsonObject components = recipe.output.getAsJsonObject("components");
            if (components != null && components.has("reverie_dreams:role_card_id")) {
                output = cards.apply(components.get("reverie_dreams:role_card_id").getAsString());
                output = output == null ? null : output.clone();
                if (output != null) output.setAmount(recipe.output.get("count").getAsInt());
            } else output = build(recipe.output);
            if (output == null) return new Plan(recipe.id, null, null, "output_not_migrated");
            int[] amounts = new int[9];
            for (int slot = 0; slot < 8; slot++) amounts[slot] = recipe.slots.get(slot).count;
            amounts[8] = recipe.core.count;
            return new Plan(recipe.id, amounts, output, null);
        }
        return null;
    }

    static ItemStack build(JsonObject item) {
        if (item.has("components") && !item.getAsJsonObject("components").isEmpty()) return null;
        String id = item.get("id").getAsString();
        ItemStack output;
        if (id.startsWith("minecraft:")) {
            Material material = Material.matchMaterial(id);
            if (material == null || !material.isItem()) return null;
            output = new ItemStack(material);
        } else {
            var definition = CraftEngineItems.byId(id);
            if (definition == null) return null;
            output = definition.buildBukkitItem();
        }
        output.setAmount(item.has("count") ? item.get("count").getAsInt() : 0);
        return output.isEmpty() ? null : output;
    }
}
