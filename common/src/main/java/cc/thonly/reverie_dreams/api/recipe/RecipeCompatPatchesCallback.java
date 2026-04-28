package cc.thonly.reverie_dreams.api.recipe;

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
}
