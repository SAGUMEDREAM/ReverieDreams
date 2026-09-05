package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.data.FoodProperty;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.world.item.Item;

import java.util.Set;

@FunctionalInterface
public interface FoodPropertiesLoaderCallback {
    Event<FoodPropertiesLoaderCallback> EVENT = EventFactory.createArrayBacked(
            FoodPropertiesLoaderCallback.class,
            (listeners) -> (ctx) -> {
                for (FoodPropertiesLoaderCallback callback : listeners) {
                    callback.modify(ctx);
                }
            }
    );

    void modify(Context ctx);

    interface Context {
        FoodProperty getProperty();

        default void addItem(Item item) {
            this.getItems().add(item);
        }

        Set<Item> getItems();
    }
}
