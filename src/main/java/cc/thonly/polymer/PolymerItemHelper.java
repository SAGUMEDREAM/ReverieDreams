package cc.thonly.polymer;

import cc.thonly.polymer.item.PolymerItemImpl;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;

public class PolymerItemHelper {
    public static void registerOverlay(Item item) {
        if (!ReverieDreamsConfiguration.POLYMER_PATCH) {
            return;
        }
        PolymerItem.registerOverlay(item, requestItemOverlay(item));
    }

    public static PolymerItem requestItemOverlay(Item item) {
        return new PolymerItemImpl(item);
    }
}
