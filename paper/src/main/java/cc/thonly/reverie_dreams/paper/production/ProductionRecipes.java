package cc.thonly.reverie_dreams.paper.production;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.io.IOException;

/** Source data is copied without changing order, counts, or processing time. */
final class ProductionRecipes {
    record Ingredient(String id, int count) {}
    record BarrelRecipe(String id, List<Ingredient> materials, Ingredient output, int ticks) {}

    private static List<BarrelRecipe> barrels = List.of();
    private static List<String> iceOutputs = List.of();

    static void load() {
        var stream = Objects.requireNonNull(ProductionRecipes.class.getResourceAsStream(
                "/migration/production-recipes.json"), "Missing production recipes");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            List<BarrelRecipe> recipes = new ArrayList<>();
            for (var element : root.getAsJsonArray("barrel_recipes")) {
                JsonObject recipe = element.getAsJsonObject();
                List<Ingredient> ingredients = new ArrayList<>();
                for (var input : recipe.getAsJsonArray("materials")) {
                    ingredients.add(ingredient(input.getAsJsonObject()));
                }
                if (ingredients.isEmpty() || ingredients.size() > 9
                        || ingredients.stream().anyMatch(i -> i.count != 1)) {
                    throw new IllegalArgumentException("Invalid barrel slot recipe: " + recipe);
                }
                recipes.add(new BarrelRecipe(recipe.get("id").getAsString(), List.copyOf(ingredients),
                        ingredient(recipe.getAsJsonObject("output")), Math.max(1, recipe.get("cost_time").getAsInt())));
            }
            // The original matcher chooses the longest matching ordered prefix first.
            recipes.sort(Comparator.comparingInt((BarrelRecipe r) -> r.materials.size()).reversed());
            barrels = List.copyOf(recipes);
            JsonArray outputs = root.getAsJsonArray("ice_outputs");
            iceOutputs = outputs.asList().stream().map(e -> e.getAsString()).toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read production recipes", exception);
        }
    }

    private static Ingredient ingredient(JsonObject object) {
        return new Ingredient(object.get("id").getAsString(), object.get("count").getAsInt());
    }

    static BarrelRecipe match(ItemStack[] inputs) {
        for (BarrelRecipe recipe : barrels) {
            boolean matches = true;
            for (int i = 0; i < recipe.materials.size(); i++) {
                Ingredient ingredient = recipe.materials.get(i);
                ItemStack expected = build(ingredient);
                // Mod ItemStack.matches includes amount and components, not only the item ID.
                if (expected == null || inputs[i] == null || !expected.equals(inputs[i])) {
                    matches = false;
                    break;
                }
            }
            if (matches) return recipe;
        }
        return null;
    }

    static List<String> iceOutputs() { return iceOutputs; }

    static ItemStack build(Ingredient ingredient) {
        ItemStack result;
        if (ingredient.id.startsWith("minecraft:")) {
            Material material = Material.matchMaterial(ingredient.id);
            if (material == null || !material.isItem()) return null;
            result = new ItemStack(material);
        } else {
            var definition = CraftEngineItems.byId(ingredient.id);
            if (definition == null) return null;
            result = definition.buildBukkitItem();
        }
        result.setAmount(ingredient.count);
        return result;
    }

    /** Reproduce the original barrel's potion and on-consume effect merge before consuming inputs. */
    static ItemStack prepareOutput(BarrelRecipe recipe, ItemStack[] inputs) {
        ItemStack result = build(recipe.output);
        if (result == null) return null;
        Map<PotionEffectType, PotionEffect> potions = new LinkedHashMap<>();
        LinkedHashSet<ConsumeEffect> effects = new LinkedHashSet<>();
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) continue;
            PotionContents contents = input.getData(DataComponentTypes.POTION_CONTENTS);
            if (contents != null) {
                for (PotionEffect next : contents.allEffects()) {
                    PotionEffect previous = potions.get(next.getType());
                    potions.put(next.getType(), previous == null ? next : new PotionEffect(next.getType(),
                            Math.max(previous.getDuration(), next.getDuration()),
                            Math.max(previous.getAmplifier(), next.getAmplifier()) + 1,
                            next.isAmbient(), next.hasParticles(), next.hasIcon()));
                }
            }
            Consumable consumable = input.getData(DataComponentTypes.CONSUMABLE);
            if (consumable != null) effects.addAll(consumable.consumeEffects());
        }
        result.setData(DataComponentTypes.POTION_CONTENTS,
                PotionContents.potionContents().addCustomEffects(new ArrayList<>(potions.values())).build());
        Consumable base = result.getData(DataComponentTypes.CONSUMABLE);
        Consumable.Builder builder = base == null ? Consumable.consumable().consumeSeconds(1.6f)
                .animation(ItemUseAnimation.DRINK).sound(Key.key("minecraft:entity.generic.drink"))
                .hasConsumeParticles(false) : base.toBuilder();
        builder.addEffects(new ArrayList<>(effects));
        result.setData(DataComponentTypes.CONSUMABLE, builder.build());
        return result;
    }
}
