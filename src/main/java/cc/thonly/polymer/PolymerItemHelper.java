package cc.thonly.polymer;

import cc.thonly.polymer.item.PolymerItemImpl;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.world.item.Item;

public class PolymerItemHelper {
    public static void registerOverlay(Item item) {
        PolymerItem.registerOverlay(item, requestItemOverlay(item));
    }

    public static PolymerItem requestItemOverlay(Item item) {
        return new PolymerItemImpl(item);
    }
}
