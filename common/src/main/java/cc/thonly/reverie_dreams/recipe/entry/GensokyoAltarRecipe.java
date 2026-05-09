package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackTemplateWrapper;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.ItemWrapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
public class GensokyoAltarRecipe extends BaseRecipe {
    public static final Codec<GensokyoAltarRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStackTemplateWrapper.CODEC.fieldOf("core").forGetter(GensokyoAltarRecipe::getCore),
                    ItemStackTemplateWrapper.CODEC.listOf().fieldOf("slots").forGetter(GensokyoAltarRecipe::getSlots),
                    ItemStackTemplateWrapper.CODEC.fieldOf("output").forGetter(GensokyoAltarRecipe::getOutput)
            ).apply(instance, GensokyoAltarRecipe::new)
    );
    private final ItemStackTemplateWrapper core;
    private final List<ItemStackTemplateWrapper> slots;
    private final ItemStackTemplateWrapper output;

    public GensokyoAltarRecipe(ItemStackTemplateWrapper core,
                               List<ItemStackTemplateWrapper> slots,
                               ItemStackTemplateWrapper output) {
        this.core = core;
        this.slots = new LinkedList<>(slots);
        while (this.slots.size() < 8) {
            this.slots.add(ItemWrapper.empty());
        }
        this.output = output;
    }

    public List<ItemStackTemplateWrapper> getInputs() {
        return this.slots.stream().toList();
    }

    public List<ItemStackTemplateWrapper> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    public ItemStackTemplateWrapper getOutput() {
        return this.output.copy();
    }

}
