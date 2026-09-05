package cc.thonly.reverie_dreams.api.registry.callback;

import cc.thonly.reverie_dreams.data.BeverageProperty;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.world.item.Item;

import java.util.Set;

@FunctionalInterface
public interface BeveragePropertiesLoaderCallback {
    Event<BeveragePropertiesLoaderCallback> EVENT = EventFactory.createArrayBacked(
            BeveragePropertiesLoaderCallback.class,
            (listeners) -> (ctx) -> {
                for (BeveragePropertiesLoaderCallback callback : listeners) {
                    callback.modify(ctx);
                }
            }
    );

    void modify(Context ctx);

    interface Context {
        BeverageProperty getProperty();

        default void addItem(Item item) {
            this.getItems().add(item);
        }

        Set<Item> getItems();
    }
}
