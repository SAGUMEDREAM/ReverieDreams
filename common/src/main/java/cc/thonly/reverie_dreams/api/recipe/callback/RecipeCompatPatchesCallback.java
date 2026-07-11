package cc.thonly.reverie_dreams.api.recipe.callback;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

@FunctionalInterface
public interface RecipeCompatPatchesCallback {
    void onLoad();
    Event<RecipeCompatPatchesCallback> EVENT = EventFactory.of(
            (listeners) -> () -> {
                for (RecipeCompatPatchesCallback callback : listeners) {
                    callback.onLoad();
                }
            }
    );

}
