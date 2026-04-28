package cc.thonly.reverie_dreams.api.item;

import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ItemAttackHitCallback {
    Event<ItemAttackHitCallback> EVENT = EventFactory.createArrayBacked(
            ItemAttackHitCallback.class,
            (listeners) -> (stack, target, attacker) -> {
                boolean shouldContinue = true;
                for (ItemAttackHitCallback listener : listeners) {
                    shouldContinue = listener.postHit(stack, target, attacker);
                }
                return shouldContinue;
            }
    );

    boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker);
}
