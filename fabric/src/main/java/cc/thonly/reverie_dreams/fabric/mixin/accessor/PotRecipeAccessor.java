package cc.thonly.reverie_dreams.fabric.mixin.accessor;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PotRecipe.class)
public interface PotRecipeAccessor {
    @Accessor("ingredients")
    NonNullList<Ingredient> reverie_dreams$getIngredients();

    @Accessor("ingredients")
    void reverie_dreams$setIngredients(NonNullList<Ingredient> ingredient);
}
