package cc.thonly.reverie_dreams.paper.item.danmaku;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public class SpellcardItem extends ItemBehavior {
    public static final ItemBehaviorFactory<SpellcardItem> FACTORY = new Factory();

    public static class Factory implements ItemBehaviorFactory<SpellcardItem> {
        @Override
        public SpellcardItem create(Pack pack, Path path, Key key, ConfigSection configSection) {
            return new SpellcardItem();
        }
    }
}
