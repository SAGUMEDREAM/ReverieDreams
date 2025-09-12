package cc.thonly.minecraft.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

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
    void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, PlayerEntity player, Consumer<Text> textConsumer, TooltipType type);
}
