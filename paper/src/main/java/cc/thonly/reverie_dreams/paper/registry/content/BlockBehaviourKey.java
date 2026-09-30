package cc.thonly.reverie_dreams.paper.registry.content;

import cc.thonly.reverie_dreams.paper.util.Ids;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Supplier;

public class BlockBehaviourKey implements Supplier<Key> {
    ;
    private final Key key;
    BlockBehaviourKey(Key key) {
        this.key = key;
    }

    BlockBehaviourKey(String path) {
        this.key = Ids.key(path);
    }

    @Override
    public Key get() {
        return this.key;
    }
}
