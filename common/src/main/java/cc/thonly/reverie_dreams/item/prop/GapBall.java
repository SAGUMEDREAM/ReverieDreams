package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.gui.item.GapGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GapBall extends Item {

    public GapBall(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer serverPlayerEntity) {
            SimpleGui gapGui = new GapGui(serverPlayerEntity, stack);
            gapGui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

}
