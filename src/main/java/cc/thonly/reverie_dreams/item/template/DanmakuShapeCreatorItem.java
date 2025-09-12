package cc.thonly.reverie_dreams.item.template;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.gui.DanmakuShapeEditGui;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class DanmakuShapeCreatorItem extends Item {
    public DanmakuShapeCreatorItem(Settings settings) {
        super(settings.maxCount(4));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            SimpleGui gui = new DanmakuShapeEditGui(player, user.getStackInHand(hand), hand);
            gui.open();
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        ItemStackWrapper itemStackWrapper = stack.getOrDefault(ModDataComponentTypes.Danmaku.SHAPE, ItemStackWrapper.of(Items.AIR));
//        ItemStack itemStack = itemStackWrapper.getItemStack();
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.shape")).append(itemStack.getName()));
//    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        ItemStackWrapper itemStackWrapper = stack.get(ModDataComponentTypes.Danmaku.SHAPE);
        return itemStackWrapper != null;
    }
}
