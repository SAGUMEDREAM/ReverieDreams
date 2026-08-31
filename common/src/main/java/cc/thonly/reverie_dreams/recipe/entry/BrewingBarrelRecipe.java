package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Builder(toBuilder = true)
public class BrewingBarrelRecipe extends BaseRecipe {
    public static final Codec<BrewingBarrelRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IngredientStack.LIST_CODEC.fieldOf("materials").forGetter(BrewingBarrelRecipe::getMaterials),
            IngredientStack.CODEC.fieldOf("output").forGetter(BrewingBarrelRecipe::getOutput),
            Codec.INT.optionalFieldOf("cost_time", 20 * 60 * 5).forGetter(BrewingBarrelRecipe::getCostTime)
    ).apply(instance, BrewingBarrelRecipe::new));

    private final List<IngredientStack> materials;
    private final IngredientStack output;
    private final int costTime;

    public BrewingBarrelRecipe(List<IngredientStack> materials, IngredientStack output, int costTime) {
        if (materials.isEmpty()) {
            throw new IllegalArgumentException("Materials is not null");
        }
        this.materials = materials;
        this.output = output;
        this.costTime = costTime;
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantValue"})
    public record IdEntry(Optional<Identifier> key, Optional<BrewingBarrelRecipe> recipe) {
        public static final Codec<BrewingBarrelRecipe.IdEntry> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("key").forGetter(BrewingBarrelRecipe.IdEntry::key),
                BrewingBarrelRecipe.CODEC.optionalFieldOf("recipe").forGetter(BrewingBarrelRecipe.IdEntry::recipe)
        ).apply(instance, BrewingBarrelRecipe.IdEntry::new)));

        public IdEntry() {
            this(Optional.empty(), Optional.empty());
        }

        public IdEntry(Identifier key) {
            this(Optional.of(key),
                    Optional.ofNullable(RecipeManager.BREWING_BARREL.getRecipeById(key))
            );
        }

        public IdEntry(BrewingBarrelRecipe recipe) {
            this(Optional.ofNullable(RecipeManager.BREWING_BARREL.getRecipeKey(recipe)),
                    Optional.of(recipe.toBuilder().build())
            );
        }

        public Optional<Identifier> key() {
            return this.key;
        }

        public Optional<BrewingBarrelRecipe> recipe() {
            return this.recipe;
        }

        public boolean map(BiConsumer<Identifier, BrewingBarrelRecipe> consumer) {
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

        public BrewingBarrelRecipe recipeOrThrow() {
            return this.recipe.get();
        }

        @Override
        public int hashCode() {
            return this.isEmpty() ? 0 : Objects.hashCode(this.keyOrThrow());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KitchenRecipe.IdEntry idEntry)) {
                return false;
            }
            return this == obj || this.hashCode() == obj.hashCode();
        }
    }
}
