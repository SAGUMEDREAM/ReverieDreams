package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.interfaces.IAnimalEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Animal.class)
public abstract class AnimalEntityMixin extends AgeableMob implements IAnimalEntity {

    @Shadow private int loveTicks;

    protected AnimalEntityMixin(EntityType<? extends AgeableMob> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    @Override
    public void eatStackFood(LivingEntity livingEntity, InteractionHand hand, ItemStack stack) {
        int i = stack.getCount();
        UseRemainder useRemainderComponent = stack.get(DataComponents.USE_REMAINDER);
        stack.shrink(1);
        if (useRemainderComponent != null) {
            ItemStack itemStack = useRemainderComponent.convertIntoRemainder(stack, i, false, livingEntity::handleExtraItemsCreatedOnUse);
            livingEntity.setItemInHand(hand, itemStack);
        }
    }

    @Unique
    @Override
    public void loveEntity(@Nullable LivingEntity entity) {
        this.loveTicks = 600;
        this.level().broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
    }
}
