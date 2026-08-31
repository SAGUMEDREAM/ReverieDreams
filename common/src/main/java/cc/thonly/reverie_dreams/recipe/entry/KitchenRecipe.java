package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

@Accessors(chain = true)
@Setter
@Getter
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder(toBuilder = true)
public class KitchenRecipe extends BaseRecipe {
    public static final Codec<KitchenRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("recipe_type").forGetter(KitchenRecipe::getRecipeType),
            Codec.list(IngredientStack.CODEC).fieldOf("ingredients").forGetter(KitchenRecipe::getIngredients),
            IngredientStack.CODEC.fieldOf("output").forGetter(KitchenRecipe::getOutput),
            Codec.DOUBLE.optionalFieldOf("cost_time", 5.0).forGetter(KitchenRecipe::getCostTime)
    ).apply(instance, KitchenRecipe::new));
    public static final LazySupplier<KitchenRecipe> EMPTY = LazySupplier.of(() -> new KitchenRecipe(KitchenRecipeType.TypeInstance.COOKING_POT, List.of(IngredientStack.empty()), IngredientStack.empty(), 1));
    protected final Identifier recipeType;
    protected final List<IngredientStack> ingredients;
    protected final IngredientStack output;
    private final Double costTime;

    public KitchenRecipe(KitchenRecipeType.TypeInstance recipeType, List<IngredientStack> ingredients, IngredientStack output, Number costTime) {
        this(recipeType.toId(), ingredients, output, costTime);
    }

    public KitchenRecipe(Identifier recipeType, List<IngredientStack> ingredients, IngredientStack output, Number costTime) {
        this.recipeType = recipeType;
        this.ingredients = ingredients;
        this.output = output;
        this.costTime = costTime.doubleValue();
        if (this.ingredients.isEmpty()) {
            log.error("Kitchen Recipe {} ingredients is 0", recipeType);
        }
        if (this.ingredients.size() > 5) {
            log.error("Kitchen Recipe {} ingredients size > 5 in {}", recipeType, recipeType + ".json");
        }
    }

    @Override
    public IngredientStack getOutput() {
        return this.output.copy();
    }

    public KitchenRecipeType.TypeInstance getTypeInstance() {
        return KitchenRecipeType.TypeInstance.getFromId(this.recipeType);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public record IdEntry(Optional<Identifier> key, Optional<KitchenRecipe> recipe) {
        public static final Codec<IdEntry> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("key").forGetter(IdEntry::key),
                KitchenRecipe.CODEC.optionalFieldOf("recipe").forGetter(IdEntry::recipe)
        ).apply(instance, IdEntry::new)));

        public IdEntry() {
            this(Optional.empty(), Optional.empty());
        }

        public IdEntry(Identifier key) {
            this(Optional.of(key),
                    Optional.ofNullable(RecipeManager.KITCHEN_TYPE.getRecipeById(key))
            );
        }

        public IdEntry(KitchenRecipe recipe) {
            this(Optional.ofNullable(RecipeManager.KITCHEN_TYPE.getRecipeKey(recipe)),
                    Optional.of(recipe.toBuilder().build())
            );
        }

        public Optional<Identifier> key() {
            return this.key;
        }

        public Optional<KitchenRecipe> recipe() {
            return this.recipe;
        }

        public boolean map(BiConsumer<Identifier, KitchenRecipe> consumer) {
            if (this.isEmpty()) {
                return false;
            }
            consumer.accept(this.keyOrThrow(), this.recipeOrThrow());
            return true;
        }

        public boolean hasRegistryKey() {
            if (this.key.isEmpty()) {
                return false;
            }
            return RecipeManager.KITCHEN_TYPE.getRecipeById(this.keyOrThrow()) != null;
        }

        public boolean isEmpty() {
            return this.key.isEmpty() || this.recipe.isEmpty();
        }

        public Identifier keyOrThrow() {
            return this.key.get();
        }

        public KitchenRecipe recipeOrThrow() {
            return this.recipe.get();
        }

        @Override
        public int hashCode() {
            return this.isEmpty() ? 0 : Objects.hashCode(this.keyOrThrow());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IdEntry idEntry)) {
                return false;
            }
            return this == obj || this.hashCode() == obj.hashCode();
        }
    }
}
