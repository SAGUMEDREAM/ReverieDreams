package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.interfaces.IAnimalEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity implements IAnimalEntity {

    @Shadow private int loveTicks;

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    @Override
    public void eatStackFood(LivingEntity livingEntity, Hand hand, ItemStack stack) {
        int i = stack.getCount();
        UseRemainderComponent useRemainderComponent = stack.get(DataComponentTypes.USE_REMAINDER);
        stack.decrement(1);
        if (useRemainderComponent != null) {
            ItemStack itemStack = useRemainderComponent.convert(stack, i, false, livingEntity::giveOrDropStack);
            livingEntity.setStackInHand(hand, itemStack);
        }
    }

    @Unique
    @Override
    public void loveEntity(@Nullable LivingEntity entity) {
        this.loveTicks = 600;
        this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_BREEDING_PARTICLES);
    }
}
