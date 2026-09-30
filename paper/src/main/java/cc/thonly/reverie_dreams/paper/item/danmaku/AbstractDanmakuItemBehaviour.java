package cc.thonly.reverie_dreams.paper.item.danmaku;

import net.momirealms.craftengine.core.entity.AbstractEntity;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public abstract class AbstractDanmakuItemBehaviour extends ItemBehavior {
    @Override
    public InteractionResult use(World world, @Nullable Player player, InteractionHand hand) {
        if (player == null) {
            return InteractionResult.PASS;
        }
        this.shoot(world, player, hand);
        return InteractionResult.SUCCESS;
    }

    public abstract void shoot(World serverWorld, AbstractEntity user, InteractionHand hand);
}
