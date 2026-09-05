package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.BeverageProperty;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface BeveragePropertyItemUseCallback {
    Event<BeveragePropertyItemUseCallback> EVENT = EventFactory.createArrayBacked(
            BeveragePropertyItemUseCallback.class,
            (listeners) -> (world, user, itemStack, property, effectInstances, negativeEffectInstances) -> {
                for (BeveragePropertyItemUseCallback callback : listeners) {
                    callback.onUse(world, user, itemStack, property, effectInstances, negativeEffectInstances);
                }
            }
    );

    void onUse(ServerLevel world, LivingEntity user, ItemStack itemStack, BeverageProperty property, List<MobEffectInstance> effectInstances, List<MobEffectInstance> negativeEffectInstances);
}
