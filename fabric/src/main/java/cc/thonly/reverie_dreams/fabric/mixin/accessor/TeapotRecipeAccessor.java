package cc.thonly.reverie_dreams.fabric.mixin.accessor;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TeapotRecipe.class)
public interface TeapotRecipeAccessor {
    @Accessor("ingredient")
    Ingredient reverie_dreams$getIngredient();

    @Accessor("ingredient")
    void reverie_dreams$setIngredients(Ingredient ingredient);
}
