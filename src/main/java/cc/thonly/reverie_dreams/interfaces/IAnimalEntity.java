package cc.thonly.reverie_dreams.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public interface IAnimalEntity {
    void eatStackFood(LivingEntity livingEntity, Hand hand, ItemStack stack);
    void loveEntity(@Nullable LivingEntity entity);
}
