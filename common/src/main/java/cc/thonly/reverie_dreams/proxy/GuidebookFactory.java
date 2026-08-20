package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.other.GuidebookItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.item.Item;

@FunctionalInterface
public interface GuidebookFactory {
    Event<GuidebookFactory> EVENT = EventFactory.of(
            listeners -> (props) -> {
                for (GuidebookFactory listener : listeners) {
                    Item result = listener.create(props);
                    if (result != null)
                        return result;
                }
                return new GuidebookItem(props.component(RDDataComponentTypes.GUIDE_BOOK_NAMESPACE.value(), ReverieDreams.MOD_ID));
            }
    );

    Item create(Item.Properties properties);
}
