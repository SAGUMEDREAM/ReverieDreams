package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
public class GensokyoAltarRecipe extends BaseRecipe {
    public static final Codec<GensokyoAltarRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    IngredientStack.CODEC.fieldOf("core").forGetter(GensokyoAltarRecipe::getCore),
                    IngredientStack.CODEC.listOf().fieldOf("slots").forGetter(GensokyoAltarRecipe::getSlots),
                    IngredientStack.CODEC.fieldOf("output").forGetter(GensokyoAltarRecipe::getOutput)
            ).apply(instance, GensokyoAltarRecipe::new)
    );
    private final IngredientStack core;
    private final List<IngredientStack> slots;
    private final IngredientStack output;

    public GensokyoAltarRecipe(IngredientStack core,
                               List<IngredientStack> slots,
                               IngredientStack output) {
        this.core = core;
        this.slots = new LinkedList<>(slots);
        while (this.slots.size() < 8) {
            this.slots.add(IngredientStack.empty());
        }
        this.output = output;
    }

    public List<IngredientStack> getInputs() {
        return this.slots.stream().toList();
    }

    public List<IngredientStack> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    public IngredientStack getOutput() {
        return this.output.copy();
    }

}
