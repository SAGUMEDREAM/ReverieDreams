package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.BeverageProperty;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface BeveragePropertyItemUseCallback {
    Event<BeveragePropertyItemUseCallback> EVENT = EventFactory.of(
            (listeners) -> (world, user, itemStack, property, effectInstances, negativeEffectInstances) -> {
                for (BeveragePropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, itemStack, property, effectInstances, negativeEffectInstances);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, ItemStack itemStack, BeverageProperty property, List<MobEffectInstance> effectInstances, List<MobEffectInstance> negativeEffectInstances);
}
