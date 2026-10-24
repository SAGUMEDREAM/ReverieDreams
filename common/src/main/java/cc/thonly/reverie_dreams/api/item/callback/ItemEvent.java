package cc.thonly.reverie_dreams.api.item.callback;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public interface ItemEvent {
    Event<ItemAttackHit> ITEM_ATTACK_HIT = EventFactory.of(
            (listeners) -> (stack, target, attacker) -> {
                boolean shouldContinue = true;

                for (ItemAttackHit listener : listeners) {
                    shouldContinue = listener.postHit(stack, target, attacker);
                }

                return shouldContinue;
            });
    Event<ItemHitBlock> ITEM_HIT_BLOCK = EventFactory.of(
            (listeners) -> (player, world, hand, pos, direction) -> {
                for (ItemHitBlock event : listeners) {
                    InteractionResult result = event.interact(player, world, hand, pos, direction);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            }
    );
    Event<ItemTooltip> ITEM_TOOLTIP = EventFactory.of(
            (listeners) -> (stack, context, player, component, consumer, type) -> {
                for (ItemTooltip listener : listeners) {
                    listener.appendTooltip(stack, context, player, component, consumer, type);
                }
            }
    );

    @FunctionalInterface
    interface ItemAttackHit {
        boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker);
    }

    @FunctionalInterface
    interface ItemHitBlock {
        InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction);
    }

    @FunctionalInterface
    interface ItemTooltip {
        void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Player player, Consumer<Component> textConsumer, TooltipFlag type);
    }

}
