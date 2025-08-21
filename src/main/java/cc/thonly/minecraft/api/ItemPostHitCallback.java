package cc.thonly.minecraft.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ItemPostHitCallback {
    Event<ItemPostHitCallback> EVENT = EventFactory.createArrayBacked(
            ItemPostHitCallback.class,
            (listeners) -> (stack, target, attacker) -> {
                boolean shouldContinue = true;
                for (ItemPostHitCallback listener : listeners) {
                    shouldContinue = listener.postHit(stack, target, attacker);
                }
                return shouldContinue;
            }
    );

    boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker);
}
