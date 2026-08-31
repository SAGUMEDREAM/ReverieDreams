package cc.thonly.reverie_dreams.api.recipe.callback;

import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

@FunctionalInterface
public interface RecipeInjectCallback {
    void onLoad(BaseRecipeType<?> type);

    Event<RecipeInjectCallback> EVENT = EventFactory.of(
            (listeners) -> (type) -> {
                for (RecipeInjectCallback callback : listeners) {
                    callback.onLoad(type);
                }
            }
    );
}
