package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.data.FoodProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.Item;

import java.util.List;
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
