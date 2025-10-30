package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder(toBuilder = true)
public class StrengthTableRecipe extends BaseRecipe {
    public static final Codec<StrengthTableRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStackWrapper.CODEC.fieldOf("main_item").forGetter(StrengthTableRecipe::getMainItem),
                    ItemStackWrapper.CODEC.fieldOf("off_item").forGetter(StrengthTableRecipe::getOffItem),
                    ItemStackWrapper.CODEC.fieldOf("output").forGetter(StrengthTableRecipe::getOutput)
            ).apply(instance, StrengthTableRecipe::new)
    );
    private final ItemStackWrapper mainItem;
    private final ItemStackWrapper offItem;
    private final ItemStackWrapper output;

    public ItemStackWrapper getOutput() {
        return new ItemStackWrapper(this.output.getItemStack().copy());
    }
}
