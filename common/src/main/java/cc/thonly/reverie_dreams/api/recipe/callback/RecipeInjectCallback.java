package cc.thonly.reverie_dreams.api.recipe.callback;

import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

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
