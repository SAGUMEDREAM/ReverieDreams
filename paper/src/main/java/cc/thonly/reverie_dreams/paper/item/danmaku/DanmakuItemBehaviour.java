package cc.thonly.reverie_dreams.paper.item.danmaku;

import net.momirealms.craftengine.core.entity.AbstractEntity;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;

import java.nio.file.Path;

public class DanmakuItemBehaviour extends AbstractDanmakuItemBehaviour {
    public static final ItemBehaviorFactory<DanmakuItemBehaviour> FACTORY = new Factory();

    @Override
    public void shoot(World serverWorld, AbstractEntity user, InteractionHand hand) {

    }

    public static class Factory implements ItemBehaviorFactory<DanmakuItemBehaviour> {
        @Override
        public DanmakuItemBehaviour create(Pack pack, Path path, Key key, ConfigSection configSection) {
            return new DanmakuItemBehaviour();
        }
    }
}
