package cc.thonly.reverie_dreams.paper.registry.content;

import cc.thonly.reverie_dreams.paper.util.Ids;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Supplier;

public enum ItemBehaviourKey implements Supplier<Key> {
    CROWN_OF_THE_UNDER_WORLD_ARMOR("crown_of_the_underworld_armor"),
    DREAM_ARMOR("dream_armor"),
    EARPHONE_ARMOR("earphone_armor"),
    KOISHI_HAT_ARMOR("koishi_hat_armor"),
    LOW_GRAVITY_BOOT_ARMOR("low_gravity_boot_armor"),
    WATERPROOF_ARMOR("waterproof_armor"),
    SILVER_ARMOR("silver_armor"),
    ;
    private final Key key;
    ItemBehaviourKey(Key key) {
        this.key = key;
    }

    ItemBehaviourKey(String path) {
        this.key = Ids.key(path);
    }

    @Override
    public Key get() {
        return this.key;
    }
}
