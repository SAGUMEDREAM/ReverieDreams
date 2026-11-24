package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.*;

public final class NPCKnowledge {
    private static final int PAGE_SIZE = 8;

    private record Match(Identifier id, Item item, int rank) {
    }

    private NPCKnowledge() {
    }

    private static List<Match> matches(String query) {
        if (query == null || query.isBlank() || query.length() > 128)
            return List.of();
        return BuiltInRegistries.ITEM.keySet().stream().map(id -> {
                    Item item = BuiltInRegistries.ITEM.getValue(id);
                    return new Match(id, item, NPCItemNames.rank(query, id.toString(), NPCItemNames.of(item)));
                }).filter(m -> m.rank < Integer.MAX_VALUE)
                .sorted(Comparator.comparingInt(Match::rank).thenComparing(m -> m.id.toString())).toList();
    }

    public static String search(String query, int offset) {
        var matches = matches(query);
        JsonObject result = new JsonObject();
        result.addProperty("query", query);
        result.addProperty("total", matches.size());
        int start = Math.clamp(offset, 0, matches.size());
        JsonArray candidates = new JsonArray();
        matches.stream().skip(start).limit(PAGE_SIZE).forEach(m -> candidates.add(item(m.item)));
        result.add("candidates", candidates);
        result.addProperty("next_offset", start + candidates.size() < matches.size() ? start + candidates.size() : -1);
        return result.toString();
    }

    public static String lookup(NPCSimpleEntity npc, String requested) {
        return lookup(npc, requested, 0);
    }

    public static String lookup(NPCSimpleEntity npc, String requested, int offset) {
        var candidates = matches(requested);
        if (candidates.isEmpty())
            return "Failed: no registered item matches this ID/name. Try search_items with a shorter name.";
        var best = candidates.getFirst();
        if (best.rank > 2 || candidates.size() > 1 && candidates.get(1).rank == best.rank) {
            return search(requested, 0); // Ambiguity is a result, never silently pick an item.
        }
        JsonObject result = item(best.item);
        result.addProperty("recipe_scope", "Currently loaded vanilla/third-party server recipes and mod custom recipes. Special recipes without resolvable outputs may be omitted; no result does not prove unobtainability.");
        List<JsonObject> found = new ArrayList<>();
        int unresolved = 0;
        var context = SlotDisplayContext.fromLevel(npc.level());
        for (var holder : npc.level().getServer().getRecipeManager().getRecipes()) {
            try {
                boolean match = holder.value().display().stream().anyMatch(display -> display.result().resolveForStacks(context)
                        .stream().anyMatch(stack -> stack.is(best.item)));
                var encoded = match || holder.value().display().isEmpty()
                        ? Recipe.CODEC.encodeStart(npc.registryAccess().createSerializationContext(JsonOps.INSTANCE), holder.value()).result()
                        : Optional.<JsonElement>empty();
                if (!match && encoded.isPresent())
                    match = outputMatches(encoded.get(), best.id.toString());
                if (match)
                    found.add(describe(npc, holder, encoded.orElse(JsonNull.INSTANCE)));
                else if (holder.value().display().isEmpty())
                    unresolved++;
            } catch (RuntimeException exception) {
                unresolved++;
            }
        }
        for (var type : RecipeManager.RECIPE_TYPES.values())
            appendCustom(npc, type, best.item, found);
        found.sort(Comparator.comparing(data -> data.get("source").getAsString() + "/" + data.get("id").getAsString()));
        int start = Math.clamp(offset, 0, found.size());
        JsonArray recipes = new JsonArray();
        int end = start, characters = 0;
        while (end < found.size() && recipes.size() < PAGE_SIZE) {
            var entry = found.get(end);
            if (entry.toString().length() > 3000) {
                entry.remove("recipe");
                entry.addProperty("detail", "Large recipe: use lookup_recipe with this exact ID for full serialized details.");
            }
            int length = entry.toString().length();
            if (!recipes.isEmpty() && characters + length > 10000)
                break;
            recipes.add(entry);
            characters += length;
            end++;
        }
        result.add("recipes", recipes);
        result.addProperty("total_recipes", found.size());
        result.addProperty("next_offset", end < found.size() ? end : -1);
        result.addProperty("unresolved_recipes_in_registry", unresolved);
        return result.toString();
    }

    public static String recipe(NPCSimpleEntity npc, String requested) {
        Identifier id = Identifier.tryParse(requested);
        if (id == null)
            return "Failed: invalid recipe ID.";
        for (var holder : npc.level().getServer().getRecipeManager().getRecipes()) {
            if (!holder.id().identifier().equals(id))
                continue;
            var encoded = Recipe.CODEC.encodeStart(npc.registryAccess().createSerializationContext(JsonOps.INSTANCE), holder.value()).result();
            return describe(npc, holder, encoded.orElse(JsonNull.INSTANCE)).toString();
        }
        List<JsonObject> found = new ArrayList<>();
        for (var type : RecipeManager.RECIPE_TYPES.values())
            customById(npc, type, id, found);
        return found.isEmpty() ? "Failed: recipe not loaded." : found.getFirst().toString();
    }

    private static JsonObject item(Item item) {
        JsonObject data = new JsonObject();
        data.addProperty("item", BuiltInRegistries.ITEM.getKey(item).toString());
        data.add("names", ChatProvider.GSON.toJsonTree(NPCItemNames.of(item)));
        data.addProperty("max_stack_size", new ItemStack(item).getMaxStackSize());
        return data;
    }

    private static boolean outputMatches(JsonElement recipe, String id) {
        if (!recipe.isJsonObject() || !recipe.getAsJsonObject().has("result"))
            return false;
        JsonElement result = recipe.getAsJsonObject().get("result");
        if (result.isJsonPrimitive())
            return result.getAsString().equals(id);
        return result.isJsonObject() && result.getAsJsonObject().has("id")
                && result.getAsJsonObject().get("id").getAsString().equals(id);
    }

    private static JsonObject describe(NPCSimpleEntity npc, RecipeHolder<?> holder, JsonElement encoded) {
        JsonObject data = new JsonObject();
        data.addProperty("id", holder.id().identifier().toString());
        data.addProperty("source", "server_recipe_manager");
        data.addProperty("type", BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType()).toString());
        data.addProperty("special", holder.value().isSpecial());
        JsonArray stations = new JsonArray();
        var context = SlotDisplayContext.fromLevel(npc.level());
        holder.value().display().stream().flatMap(display -> display.craftingStation().resolveForStacks(context).stream())
                .filter(stack -> !stack.isEmpty()).map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .distinct().limit(8).forEach(stations::add);
        data.add("workstations", stations);
        if (encoded.toString().length() <= 12000)
            data.add("recipe", encoded);
        else
            data.addProperty("detail", "Recipe data exceeds the response limit; consult the recipe viewer for full details.");
        return data;
    }

    private static <R extends BaseRecipe> void appendCustom(NPCSimpleEntity npc, BaseRecipeType<R> type, Item item, List<JsonObject> into) {
        for (var entry : type.getRegistryView().entrySet()) {
            if (entry.getValue().getOutput().getItem() == item)
                into.add(custom(npc, type, entry.getKey(), entry.getValue()));
        }
    }

    private static <R extends BaseRecipe> void customById(NPCSimpleEntity npc, BaseRecipeType<R> type, Identifier id, List<JsonObject> into) {
        for (var entry : type.getRegistryView().entrySet())
            if (entry.getKey().equals(id))
                into.add(custom(npc, type, id, entry.getValue()));
    }

    private static <R extends BaseRecipe> JsonObject custom(NPCSimpleEntity npc, BaseRecipeType<R> type, Identifier id, R recipe) {
        JsonObject data = new JsonObject();
        data.addProperty("id", id.toString());
        data.addProperty("type", type.getId().toString());
        data.addProperty("source", "mod_custom_recipe_registry");
        var encoded = type.getCodec().encodeStart(npc.registryAccess().createSerializationContext(JsonOps.INSTANCE), recipe).result();
        if (encoded.isPresent() && encoded.get().toString().length() <= 12000)
            data.add("recipe", encoded.get());
        else
            data.addProperty("detail", "Recipe could not be fully serialized; do not invent its ingredients.");
        return data;
    }
}
