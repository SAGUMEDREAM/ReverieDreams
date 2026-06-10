package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.item.other.UrlTHGuideBookItem;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.world.item.Item;

@FunctionalInterface
public interface GuidebookFactory {
    Event<GuidebookFactory> EVENT = EventFactory.createArrayBacked(
            GuidebookFactory.class,
            listeners -> (props) -> {
                for (GuidebookFactory listener : listeners) {
                    Item result = listener.create(props);
                    if (result != null) return result;
                }
                return new UrlTHGuideBookItem(props);
            }
    );

    Item create(Item.Properties properties);
}
