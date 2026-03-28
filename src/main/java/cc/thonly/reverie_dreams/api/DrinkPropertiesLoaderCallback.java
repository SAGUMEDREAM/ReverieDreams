package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface DrinkPropertiesLoaderCallback {
    Event<DrinkPropertiesLoaderCallback> EVENT = EventFactory.createArrayBacked(
            DrinkPropertiesLoaderCallback.class,
            (listeners) -> (ctx) -> {
                for (DrinkPropertiesLoaderCallback callback : listeners) {
                    callback.modify(ctx);
                }
            }
    );

    void modify(Context ctx);

    interface Context {
        DrinkProperty getProperty();

        default void addItem(Item item) {
            this.getItems().add(item);
        }

        Set<Item> getItems();
    }
}
