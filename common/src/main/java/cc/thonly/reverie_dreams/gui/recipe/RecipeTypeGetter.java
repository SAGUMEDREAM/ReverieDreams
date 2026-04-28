package cc.thonly.reverie_dreams.gui.recipe;

import cc.thonly.reverie_dreams.recipe.BaseRecipeType;

@FunctionalInterface
public interface RecipeTypeGetter {
    BaseRecipeType<?> get();
}
