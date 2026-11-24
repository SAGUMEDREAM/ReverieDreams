package cc.thonly.reverie_dreams.paper.food;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

/** Source-derived rules; the native CE consumable already applies its base food values. */
public final class FoodRules {
    private final JsonObject domain;

    public FoodRules(JsonObject domain) {
        if (domain.get("schemaVersion").getAsInt() != 1) throw new IllegalArgumentException("Unsupported food schema");
        this.domain = domain.deepCopy();
    }

    public record Effect(String id, int duration, int amplifier) {}
    public record Plan(List<Effect> effects, int nutrition, float saturation, boolean clearFire, int fireTicks,
                       int alcoholProtectionTicks) {}

    public Plan plan(String id, List<String> foodOverride, List<String> drinkOverride, boolean hasFoodComponent,
                     boolean protectedFromAlcohol, RandomGenerator random) {
        if (domain.getAsJsonObject("aliases").has(id)) id = domain.getAsJsonObject("aliases").get(id).getAsString();
        JsonObject item = domain.getAsJsonObject("items").has(id) ? domain.getAsJsonObject("items").getAsJsonObject(id) : null;
        List<String> food = properties(id, item, "foodProperties", foodOverride);
        List<String> drink = properties(id, item, "beverageProperties", drinkOverride);
        boolean ingredient = strings(domain.getAsJsonObject("foodConsumption").getAsJsonArray("skipIngredientItems")).contains(id);
        boolean eligible = hasFoodComponent || item != null && item.get("category").getAsString().equals("cuisine");
        List<Effect> effects = new ArrayList<>();
        boolean[] clear = {false};
        int[] fire = {0};
        int nutrition = 0;
        float saturation = 0;
        if (!ingredient && eligible) {
            for (String property : food) append(domain.getAsJsonObject("foodProperties"), property, false, random, effects, clear, fire);
            JsonObject settings = domain.getAsJsonObject("foodConsumption");
            nutrition = food.size() * settings.get("nutritionPerProperty").getAsInt();
            saturation = food.size() * settings.get("saturationPerProperty").getAsFloat();
        }
        if (hasFoodComponent || item != null && item.get("category").getAsString().equals("beverage")) {
            for (String property : drink) append(domain.getAsJsonObject("beverageProperties"), property,
                    protectedFromAlcohol, random, effects, clear, fire);
        }
        JsonObject milk = domain.getAsJsonObject("beverageConsumption").getAsJsonObject("milkProtection");
        int protection = id.equals(milk.get("item").getAsString()) ? milk.get("durationTicks").getAsInt() : 0;
        return new Plan(List.copyOf(effects), nutrition, saturation, clear[0], fire[0], protection);
    }

    private List<String> properties(String id, JsonObject item, String category, List<String> override) {
        Set<String> result = new LinkedHashSet<>();
        if (override != null && !override.isEmpty()) {
            for (String property : override) if (domain.getAsJsonObject(category).has(property)) result.add(property);
        } else if (item != null) {
            result.addAll(strings(item.getAsJsonArray(category)));
        } else {
            for (var entry : domain.getAsJsonObject(category).entrySet()) {
                if (strings(entry.getValue().getAsJsonObject().getAsJsonArray("items")).contains(id)) result.add(entry.getKey());
            }
        }
        return List.copyOf(result);
    }

    private static void append(JsonObject definitions, String property, boolean suppressNegative, RandomGenerator random,
                               List<Effect> effects, boolean[] clear, int[] fire) {
        if (!definitions.has(property)) return;
        JsonObject definition = definitions.getAsJsonObject(property);
        appendEffects(definition.getAsJsonArray("effects"), suppressNegative, effects);
        JsonArray choices = definition.getAsJsonArray("randomChoice");
        int total = 0;
        for (var choice : choices) total += choice.getAsJsonObject().get("weight").getAsInt();
        if (total > 0) {
            int roll = random.nextInt(total);
            for (var choice : choices) {
                JsonObject branch = choice.getAsJsonObject();
                roll -= branch.get("weight").getAsInt();
                if (roll < 0) {
                    appendEffects(branch.getAsJsonArray("effects"), suppressNegative, effects);
                    break;
                }
            }
        }
        clear[0] |= definition.get("clearFire").getAsBoolean();
        fire[0] = Math.max(fire[0], definition.get("fireTicks").getAsInt());
    }

    private static void appendEffects(JsonArray input, boolean suppressNegative, List<Effect> output) {
        for (var element : input) {
            JsonObject effect = element.getAsJsonObject();
            if (suppressNegative && effect.get("negative").getAsBoolean()) continue;
            output.add(new Effect(effect.get("id").getAsString(), effect.get("durationTicks").getAsInt(), effect.get("amplifier").getAsInt()));
        }
    }

    private static List<String> strings(JsonArray array) {
        List<String> result = new ArrayList<>();
        if (array != null) for (var value : array) result.add(value.getAsString());
        return result;
    }
}
