package cc.thonly.reverie_dreams.paper.item.armor;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public class CrownOfTheUnderworldItem extends ItemBehavior {
    public static final ItemBehaviorFactory<CrownOfTheUnderworldItem> FACTORY = new Factory();

    public static class Factory implements ItemBehaviorFactory<CrownOfTheUnderworldItem> {
        @Override
        public CrownOfTheUnderworldItem create(Pack pack, Path path, Key key, ConfigSection configSection) {
            return new CrownOfTheUnderworldItem();
        }
    }
}
