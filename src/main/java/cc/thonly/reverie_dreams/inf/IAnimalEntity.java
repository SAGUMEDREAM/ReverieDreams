package cc.thonly.reverie_dreams.inf;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IAnimalEntity {
    void eatStackFood(LivingEntity livingEntity, InteractionHand hand, ItemStack stack);
    void loveEntity(@Nullable LivingEntity entity);
}
