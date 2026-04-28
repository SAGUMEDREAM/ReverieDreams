package cc.thonly.reverie_dreams.api.item;

import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public interface ItemStackTooltipCallback {
    Event<ItemStackTooltipCallback> EVENT = EventFactory.createArrayBacked(
            ItemStackTooltipCallback.class,
            (listeners) -> (stack, context, player, component, consumer, type) -> {
                for (ItemStackTooltipCallback listener : listeners) {
                    listener.appendTooltip(stack, context, player, component, consumer, type);
                }
            }
    );
    void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Player player, Consumer<Component> textConsumer, TooltipFlag type);
}
