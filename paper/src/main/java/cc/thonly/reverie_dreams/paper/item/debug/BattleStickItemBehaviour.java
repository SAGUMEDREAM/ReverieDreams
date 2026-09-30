package cc.thonly.reverie_dreams.paper.item.debug;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public class BattleStickItemBehaviour extends ItemBehavior {
    public static final ItemBehaviorFactory<BattleStickItemBehaviour> FACTORY = new Factory();

    public static class Factory implements ItemBehaviorFactory<BattleStickItemBehaviour> {
        @Override
        public BattleStickItemBehaviour create(Pack pack, Path path, Key key, ConfigSection configSection) {
            return new BattleStickItemBehaviour();
        }
    }
}
