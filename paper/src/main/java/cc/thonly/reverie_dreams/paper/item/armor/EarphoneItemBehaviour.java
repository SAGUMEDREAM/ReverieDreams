package cc.thonly.reverie_dreams.paper.item.armor;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public class EarphoneItemBehaviour extends ItemBehavior {
    public static final ItemBehaviorFactory<EarphoneItemBehaviour> FACTORY = new Factory();

    public static class Factory implements ItemBehaviorFactory<EarphoneItemBehaviour> {
        @Override
        public EarphoneItemBehaviour create(Pack pack, Path path, Key key, ConfigSection configSection) {
            return new EarphoneItemBehaviour();
        }
    }
}
