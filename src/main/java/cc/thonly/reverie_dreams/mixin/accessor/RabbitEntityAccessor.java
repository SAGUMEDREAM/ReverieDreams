package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.RabbitEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RabbitEntity.class)
public interface RabbitEntityAccessor {
    @Accessor("VARIANT")
    static TrackedData<Integer> getVariant() {
        throw new UnsupportedOperationException();
    }
}
