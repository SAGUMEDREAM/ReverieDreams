package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
@Builder(toBuilder = true)
public class BarrelRecipe extends BaseRecipe {
    public static final Codec<BarrelRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IngredientStack.LIST_CODEC.fieldOf("materials").forGetter(BarrelRecipe::getMaterials),
            IngredientStack.CODEC.fieldOf("output").forGetter(BarrelRecipe::getOutput)
    ).apply(instance, BarrelRecipe::new));

    private final List<IngredientStack> materials;
    private final IngredientStack output;


}
