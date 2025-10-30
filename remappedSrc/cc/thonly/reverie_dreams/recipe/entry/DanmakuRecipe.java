package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
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
            ItemStackWrapper.CODEC.fieldOf("dye").forGetter(DanmakuRecipe::getDye),
            ItemStackWrapper.CODEC.fieldOf("core").forGetter(DanmakuRecipe::getCore),
            ItemStackWrapper.CODEC.fieldOf("power").forGetter(DanmakuRecipe::getPower),
            ItemStackWrapper.CODEC.fieldOf("point").forGetter(DanmakuRecipe::getPoint),
            ItemStackWrapper.CODEC.fieldOf("material").forGetter(DanmakuRecipe::getMaterial),
            ItemStackWrapper.CODEC.fieldOf("output").forGetter(DanmakuRecipe::getOutput)
    ).apply(instance, DanmakuRecipe::new));

    private final ItemStackWrapper dye;
    private final ItemStackWrapper core;
    private final ItemStackWrapper power;
    private final ItemStackWrapper point;
    private final ItemStackWrapper material;
    private final ItemStackWrapper output;
}
