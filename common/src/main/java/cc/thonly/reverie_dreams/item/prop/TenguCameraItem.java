package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.common.RDMPHooks;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

@Slf4j
public class TenguCameraItem extends Item {

    public TenguCameraItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            return RDMPHooks.TenguCameraItemUseCallback.EVENT.invoker().handle(level, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

}
