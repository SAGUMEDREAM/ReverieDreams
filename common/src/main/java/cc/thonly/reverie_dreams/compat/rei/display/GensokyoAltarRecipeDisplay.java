package cc.thonly.reverie_dreams.compat.rei.display;

import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.util.item.REIItemUtils;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GensokyoAltarRecipeDisplay extends BasicDisplay {
    public static final DisplaySerializer<GensokyoAltarRecipeDisplay> SERIALIZER =
            DisplaySerializer.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                    GensokyoAltarRecipe.CODEC.fieldOf("recipe").forGetter(GensokyoAltarRecipeDisplay::getRecipe)
            ).apply(instance, GensokyoAltarRecipeDisplay::new)), StreamCodec.composite(
                    BaseRecipe.forStreamCodec(GensokyoAltarRecipe.CODEC), GensokyoAltarRecipeDisplay::getRecipe,
                    GensokyoAltarRecipeDisplay::new
            ));
    @Getter
    GensokyoAltarRecipe recipe;

    public GensokyoAltarRecipeDisplay(GensokyoAltarRecipe recipe) {
        super(recipe.getSlots()
                .stream()
                .map(IngredientStack::build)
                .filter(itemStack -> !itemStack.isEmpty())
                .map(REIItemUtils::getItem)
                .toList(),
                List.of(REIItemUtils.getItem(recipe.getOutput()))
        );
        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REICategoryIdentifiers.GENSOKYO_ALTAR;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
