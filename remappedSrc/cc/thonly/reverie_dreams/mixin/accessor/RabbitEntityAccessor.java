package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Rabbit.class)
public interface RabbitEntityAccessor {
    @Accessor("VARIANT")
    static EntityDataAccessor<Integer> getVariant() {
        throw new UnsupportedOperationException();
    }
}
