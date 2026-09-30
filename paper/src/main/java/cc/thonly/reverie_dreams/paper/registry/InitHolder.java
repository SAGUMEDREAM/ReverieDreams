package cc.thonly.reverie_dreams.paper.registry;

import cc.thonly.reverie_dreams.paper.registry.content.BlockBehaviours;
import cc.thonly.reverie_dreams.paper.registry.content.ItemBehaviours;

public class InitHolder {
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        BlockBehaviours.initialize();
        ItemBehaviours.initialize();
        initialized = true;
    }
}
