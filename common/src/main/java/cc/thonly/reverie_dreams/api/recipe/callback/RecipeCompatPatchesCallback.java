package cc.thonly.reverie_dreams.api.recipe.callback;

import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

@FunctionalInterface
public interface RecipeCompatPatchesCallback {
    void onLoad();
    Event<RecipeCompatPatchesCallback> EVENT = EventFactory.createArrayBacked(
            RecipeCompatPatchesCallback.class,
            (listeners) -> () -> {
                for (RecipeCompatPatchesCallback callback : listeners) {
                    callback.onLoad();
                }
            }
    );

    class Helper {
        public <R extends BaseRecipe> RecipeCompatPatchesImpl.Builder<R> recipeAccess(BaseRecipeType<R> recipeType) {
            return RecipeCompatPatchesImpl.getOrCreateBuilder(recipeType);
        }
    }
}
