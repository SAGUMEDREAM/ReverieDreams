package cc.thonly.reverie_dreams.paper.item.armor;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public class LowGravityBootItemBehaviour extends ItemBehavior {
    public static final ItemBehaviorFactory<LowGravityBootItemBehaviour> FACTORY = new Factory();

    public static class Factory implements ItemBehaviorFactory<LowGravityBootItemBehaviour> {
        @Override
        public LowGravityBootItemBehaviour create(Pack pack, Path path, Key key, ConfigSection configSection) {
            return new LowGravityBootItemBehaviour();
        }
    }
}
