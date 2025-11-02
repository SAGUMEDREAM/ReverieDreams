package cc.thonly.reverie_dreams.item.template;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.gui.DanmakuShapeEditGui;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DanmakuShapeCreatorItem extends Item {
    public DanmakuShapeCreatorItem(Properties settings) {
        super(settings.stacksTo(4));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide && user instanceof ServerPlayer player) {
            SimpleGui gui = new DanmakuShapeEditGui(player, user.getItemInHand(hand), hand);
            gui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        ItemStackWrapper itemStackWrapper = stack.getOrDefault(ModDataComponentTypes.Danmaku.SHAPE, ItemStackWrapper.of(Items.AIR));
//        ItemStack itemStack = itemStackWrapper.getItemStack();
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.shape")).append(itemStack.getName()));
//    }

    @Override
    public boolean isFoil(ItemStack stack) {
        ItemStackWrapper itemStackWrapper = stack.get(ModDataComponentTypes.SHAPE);
        return itemStackWrapper != null;
    }
}
