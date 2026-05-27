package cc.thonly.reverie_dreams.api.recipe;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;

public class RecipeCompatContext {
    public <R extends BaseRecipe> Builder<R> recipeAccess(BaseRecipeType<R> recipeType) {
        return RecipeCompatPatches.getOrCreateBuilder(recipeType);
    }
}
