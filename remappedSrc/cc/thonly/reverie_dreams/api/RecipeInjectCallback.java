package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface RecipeInjectCallback {
    void onLoad(BaseRecipeType<?> type);

    Event<RecipeInjectCallback> EVENT = EventFactory.createArrayBacked(
            RecipeInjectCallback.class,
            (listeners) -> (type) -> {
                for (RecipeInjectCallback callback : listeners) {
                    callback.onLoad(type);
                }
            }
    );
}
