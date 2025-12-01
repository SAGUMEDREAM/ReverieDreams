package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class KitchenRecipeType extends BaseRecipeType<KitchenRecipe> {
    private static KitchenRecipeType INSTANCE;
    public final Map<KitchenType, Map<ResourceLocation, KitchenRecipe>> kitchenRegistries = new Object2ObjectOpenHashMap<>();

    public KitchenRecipeType() {
        INSTANCE = this;
    }

    public static synchronized KitchenRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {
        this.kitchenRegistries.clear();
        Map<ResourceLocation, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            ResourceLocation registryKey = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^kitchen_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<KitchenRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load kitchen recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            this.add(registryKey, recipe);
                        });
            } catch (IOException e) {
                log.error("Failed to load kitchen recipe {}, {}, {}", id, e.getMessage(), e);
            }
        }
    }

    @Override
    public BaseRecipeType<KitchenRecipe> add(ResourceLocation id, KitchenRecipe recipe) {
        super.add(id, recipe);
        this.register(recipe.getType(), id, recipe);;
        return this;
    }

    public void register(KitchenType type, ResourceLocation key, KitchenRecipe recipe) {
        Map<ResourceLocation, KitchenRecipe> registry = this.kitchenRegistries.computeIfAbsent(type, R -> new Object2ObjectOpenHashMap<>());
        recipe.setId(key);
        registry.put(key, recipe);
    }

    public Map<ResourceLocation, KitchenRecipe> getRecipeView(KitchenType type) {
        return Map.copyOf(this.kitchenRegistries.getOrDefault(type, new Object2ObjectOpenHashMap<>()));
    }

    @Override
    public void bootstrap() {

    }

    public List<KitchenRecipe> getMatches(KitchenType type, List<ItemStackWrapper> inputs) {
        List<KitchenRecipe> matches = new ArrayList<>();
        Map<ResourceLocation, KitchenRecipe> registryView = this.getRecipeView(type);

        for (KitchenRecipe recipe : registryView.values()) {
            List<ItemStackWrapper> ingredients = recipe.getIngredients();

            boolean allMatched = ingredients.stream().allMatch(ingredient ->
                    inputs.stream().anyMatch(input ->
                            ingredient.greaterThan(input.getItemStack())
                    )
            );

            if (allMatched) {
                matches.add(recipe);
            }
        }

        return matches;
    }

    @Override
    public List<KitchenRecipe> getMatches(List<ItemStackWrapper> list) {
        return List.of();
    }

    @Override
    public Boolean isMatch(ItemStackWrapper input, ItemStackWrapper recipe) {
        return false;
    }

    @Override
    public Codec<KitchenRecipe> getCodec() {
        return KitchenRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "kitchen";
    }

    @Override
    public ResourceLocation getId() {
        return ReverieDreams.id(this.getTypeId());
    }

    @Getter
    public enum KitchenType {
        COOKING_POT(ReverieDreams.id("cooking_pot")),
        CUTTING_BOARD(ReverieDreams.id("cutting_board")),
        FRYING_PAN(ReverieDreams.id("frying_pan")),
        GRILL(ReverieDreams.id("grill")),
        STREAMER(ResourceLocation.parse("streamer")),
        ;
        private static final Map<ResourceLocation, KitchenType> SEARCH_CACHED = new Object2ObjectOpenHashMap<>();
        private final ResourceLocation id;

        KitchenType(ResourceLocation id) {
            this.id = id;
        }

        public ResourceLocation toId() {
            return this.id;
        }

        public static KitchenType getFromId(ResourceLocation recipeId) {
            if (SEARCH_CACHED.containsKey(recipeId)) {
                SEARCH_CACHED.get(recipeId);
            }
            List<KitchenType> result = Arrays.stream(values()).toList().stream().filter(recipeType -> recipeId.equals(recipeType.id)).toList();
            if (result.isEmpty()) {
                return null;
            } else {
                KitchenType first = result.getFirst();
                SEARCH_CACHED.put(recipeId, first);
                return first;
            }
        }
    }
}
