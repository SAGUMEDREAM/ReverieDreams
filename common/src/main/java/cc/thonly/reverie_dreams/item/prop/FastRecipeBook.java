package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.gui.item.FastRecipeBookGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class FastRecipeBook extends Item {
    public FastRecipeBook(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            SimpleGui gui = new FastRecipeBookGui((ServerPlayer) player, itemStack);
            gui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        this.useOnKitchenBlock(context);
        return super.useOn(context);
    }

    public void useOnKitchenBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return;
        }
        BlockPos clickedPos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        Player player = context.getPlayer();
    }
}
