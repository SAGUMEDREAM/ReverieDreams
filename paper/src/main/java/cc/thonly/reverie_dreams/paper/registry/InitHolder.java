package cc.thonly.reverie_dreams.paper.registry;

import cc.thonly.reverie_dreams.paper.registry.content.RDBlockBehaviours;
import cc.thonly.reverie_dreams.paper.registry.content.RDItemBehaviours;

public class InitHolder {
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        RDBlockBehaviours.initialize();
        RDItemBehaviours.initialize();
        initialized = true;
    }
}
