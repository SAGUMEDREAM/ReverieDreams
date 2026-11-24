package cc.thonly.reverie_dreams.api.item.callback;

import cc.thonly.reverie_dreams.data.BeverageProperty;
import dev.architectury.event.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface BeveragePropertyItemUseCallback {
    /**
     * @deprecated Use {@link ItemEvent#BEVERAGE_PROPERTY_USE}.
     */
    @Deprecated
    Event<BeveragePropertyItemUseCallback> EVENT = ItemEvent.BEVERAGE_PROPERTY_USE;

    void onUse(ServerLevel world, LivingEntity user, ItemStack itemStack, BeverageProperty property, List<MobEffectInstance> effectInstances, List<MobEffectInstance> negativeEffectInstances);
}
