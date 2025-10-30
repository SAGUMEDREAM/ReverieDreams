package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
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
                    ItemStackWrapper.CODEC.fieldOf("core").forGetter(GensokyoAltarRecipe::getCore),
                    ItemStackWrapper.CODEC.listOf().fieldOf("slots").forGetter(GensokyoAltarRecipe::getSlots),
                    ItemStackWrapper.CODEC.fieldOf("output").forGetter(GensokyoAltarRecipe::getOutput)
            ).apply(instance, GensokyoAltarRecipe::new)
    );
    private final ItemStackWrapper core;
    private final List<ItemStackWrapper> slots;
    private final ItemStackWrapper output;

    public GensokyoAltarRecipe(ItemStackWrapper core,
                               List<ItemStackWrapper> slots,
                               ItemStackWrapper output) {
        this.core = core;
        this.slots = new LinkedList<>(slots);
        while (this.slots.size() < 8) {
            this.slots.add(ItemStackWrapper.empty());
        }
        this.output = output;
    }

    public List<ItemStackWrapper> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    public ItemStackWrapper getOutput() {
        return new ItemStackWrapper(this.output.getItemStack().copy());
    }

}
