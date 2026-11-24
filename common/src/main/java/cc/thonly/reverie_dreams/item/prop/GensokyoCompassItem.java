package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.server.dialog.compass.GensokyoCompassDialog;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GensokyoCompassItem extends Item {

    public GensokyoCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!(user instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        if (!player.level().dimension().equals(RDBuiltinLevels.GENSOKYO)) {
            player.sendSystemMessage(Component.translatable(
                    "message.reverie_dreams.gensokyo_compass.wrong_dimension"), false);
            return InteractionResult.SUCCESS_SERVER;
        }
        GensokyoCompassDialog.open(player, hand, 0);
        return InteractionResult.SUCCESS_SERVER;
    }
}
