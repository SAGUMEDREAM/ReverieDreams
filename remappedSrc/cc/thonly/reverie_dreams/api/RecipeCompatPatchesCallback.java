package cc.thonly.reverie_dreams.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

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
}
