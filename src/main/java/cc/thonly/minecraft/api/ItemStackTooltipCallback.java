package cc.thonly.minecraft.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

public interface ItemStackTooltipCallback {
    Event<ItemStackTooltipCallback> EVENT = EventFactory.createArrayBacked(
            ItemStackTooltipCallback.class,
            (listeners) -> (itemStack, player, context, consumer, tooltipFlag) -> {
                for (ItemStackTooltipCallback listener : listeners) {
                    listener.appendTooltip(itemStack, player, context, consumer, tooltipFlag);
                }
            }
    );

    void appendTooltip(ItemStack itemStack, Player player, Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag tooltipFlag);
}
