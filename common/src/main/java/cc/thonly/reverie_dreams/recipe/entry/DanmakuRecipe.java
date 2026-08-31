package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
@Builder(toBuilder = true)
public class DanmakuRecipe extends BaseRecipe {
    public static final Codec<DanmakuRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IngredientStack.CODEC.fieldOf("dye").forGetter(DanmakuRecipe::getDye),
            IngredientStack.CODEC.fieldOf("core").forGetter(DanmakuRecipe::getCore),
            IngredientStack.CODEC.fieldOf("power").forGetter(DanmakuRecipe::getPower),
            IngredientStack.CODEC.fieldOf("point").forGetter(DanmakuRecipe::getPoint),
            IngredientStack.CODEC.fieldOf("material").forGetter(DanmakuRecipe::getMaterial),
            IngredientStack.CODEC.fieldOf("output").forGetter(DanmakuRecipe::getOutput)
    ).apply(instance, DanmakuRecipe::new));

    private final IngredientStack dye;
    private final IngredientStack core;
    private final IngredientStack power;
    private final IngredientStack point;
    private final IngredientStack material;
    private final IngredientStack output;
}
