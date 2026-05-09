package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;

import java.util.List;

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
    protected final Identifier recipeType;
    protected final List<IngredientStack> ingredients;
    protected final IngredientStack output;
    private final Double costTime;

    public KitchenRecipe(KitchenRecipeType.MappingType kitchenType, List<IngredientStack> ingredients, IngredientStack output, Number costTime) {
        this(kitchenType.toId(), ingredients,output, costTime);
    }

    public KitchenRecipe(Identifier recipeType, List<IngredientStack> ingredients, IngredientStack output, Number costTime) {
        this.recipeType = recipeType;
        this.ingredients = ingredients;
        this.output = output;
        this.costTime = costTime.doubleValue();
        if (this.ingredients.size() > 5) {
            log.error("Kitchen Recipe {} ingredients size > 5 in {}", recipeType, recipeType + ".json");
        }
    }

    public IngredientStack getOutput() {
        return this.output.copy();
    }

    public KitchenRecipeType.MappingType getType() {
        return KitchenRecipeType.MappingType.getFromId(this.recipeType);
    }
}
