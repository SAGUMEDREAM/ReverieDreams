package cc.thonly.reverie_dreams.proxy;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface TenguCameraItemUse {
    InteractionResult handle(Level level, Player player, InteractionHand hand);
}
