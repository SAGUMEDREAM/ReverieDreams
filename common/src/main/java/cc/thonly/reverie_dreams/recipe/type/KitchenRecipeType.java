package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.ItemComparatorView;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import cc.thonly.reverie_dreams.recipe.RecipeResourceLoader;

@Slf4j
public class KitchenRecipeType extends BaseRecipeType<KitchenRecipe> {
    private static KitchenRecipeType INSTANCE;
    public final Map<TypeInstance, Map<Identifier, KitchenRecipe>> kitchenRegistries = new Object2ObjectOpenHashMap<>();

    public KitchenRecipeType() {
        INSTANCE = this;
    }

    public static synchronized KitchenRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {
        this.kitchenRegistries.clear();
        RecipeResourceLoader.load(manager, this.getTypeId() + "_recipe",
                id -> id.getNamespace().equals(ReverieDreams.MOD_ID), this.getCodec(), this::add);
    }

    @Override
    public BaseRecipeType<KitchenRecipe> add(Identifier id, KitchenRecipe recipe) {
        super.add(id, recipe);
        this.register(recipe.getTypeInstance(), id, recipe);
        return this;
    }

    public void register(TypeInstance type, Identifier key, KitchenRecipe recipe) {
        Map<Identifier, KitchenRecipe> registry = this.kitchenRegistries.computeIfAbsent(type, R -> new Object2ObjectOpenHashMap<>());
        recipe.setId(key);
        registry.put(key, recipe);
    }

    public Map<Identifier, KitchenRecipe> getRecipeView(TypeInstance type) {
        return Map.copyOf(this.kitchenRegistries.getOrDefault(type, new Object2ObjectOpenHashMap<>()));
    }

    @Override
    public void bootstrap() {

    }

    public boolean isMatches(KitchenRecipe recipe, List<IngredientStack> inputs) {
        List<IngredientStack> ingredients = recipe.getIngredients();

        return ingredients.stream().allMatch(ingredient ->
                inputs.stream().anyMatch(input ->
                        ItemComparatorView.of(ingredient).greaterThan(input)
                )
        );
    }

    public List<KitchenRecipe> getMatches(TypeInstance type, List<IngredientStack> inputs) {
        List<KitchenRecipe> matches = new ArrayList<>();
        Map<Identifier, KitchenRecipe> registryView = this.getRecipeView(type);

        for (KitchenRecipe recipe : registryView.values()) {
            List<IngredientStack> ingredients = recipe.getIngredients();
            boolean allMatched = ingredients.stream().allMatch(ingredient ->
                    inputs.stream().anyMatch(input ->
                            {
                                return ItemComparatorView.of(ingredient).greaterThan(input);
                            }
                    )
            );

            if (allMatched) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    @Override
    public List<KitchenRecipe> getMatches(List<IngredientStack> list) {
        return List.of();
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
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
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }

    public record TypeInstance(Identifier id) {
        private static final Map<Identifier, TypeInstance> BY_ID = new Object2ObjectOpenHashMap<>();

        public static final TypeInstance COOKING_POT =
                new TypeInstance(ReverieDreams.id("cooking_pot"));

        public static final TypeInstance CUTTING_BOARD =
                new TypeInstance(ReverieDreams.id("cutting_board"));

        public static final TypeInstance FRYING_PAN =
                new TypeInstance(ReverieDreams.id("frying_pan"));

        public static final TypeInstance GRILL =
                new TypeInstance(ReverieDreams.id("grill"));

        public static final TypeInstance STEAMER =
                new TypeInstance(ReverieDreams.id("steamer"));

        public TypeInstance(Identifier id) {
            this.id = id;
            BY_ID.put(id, this);
        }

        public boolean is(TypeInstance other) {
            return Objects.equals(this.id, other.id);
        }

        public String toTranslateKey() {
            if (this == COOKING_POT) {
                return "block.reverie_dreams.cooking_pot";
            }
            if (this == CUTTING_BOARD) {
                return "block.reverie_dreams.cutting_board";
            }
            if (this == FRYING_PAN) {
                return "block.reverie_dreams.frying_pan";
            }
            if (this == GRILL) {
                return "block.reverie_dreams.grill";
            }
            if (this == STEAMER) {
                return "block.reverie_dreams.steamer";
            }
            return "unknown";
        }

        public Identifier toId() {
            return this.id;
        }

        public Block defaultBlock() {
            if (this == COOKING_POT) {
                return RDKitchenBlocks.COOKING_POT.asBlock();
            }
            if (this == CUTTING_BOARD) {
                return RDKitchenBlocks.CUTTING_BOARD.asBlock();
            }
            if (this == FRYING_PAN) {
                return RDKitchenBlocks.FRYING_PAN.asBlock();
            }
            if (this == GRILL) {
                return RDKitchenBlocks.GRILL.asBlock();
            }
            if (this == STEAMER) {
                return RDKitchenBlocks.STEAMER.asBlock();
            }
            return null;
        }

        public static TypeInstance getFromId(Identifier id) {
            return BY_ID.get(id);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof TypeInstance(Identifier id1) && id1.equals(this.id));
        }
    }
}
