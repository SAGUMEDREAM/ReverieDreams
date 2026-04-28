package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Animal.class)
public interface AnimalAccessor {
    @Invoker("playEatingSound")
    void reverie_dreams$playEatingSound();
}
