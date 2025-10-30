package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PigVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pig.class)
public interface PigEntityAccessor {
    @Accessor("DATA_VARIANT_ID")
    public static EntityDataAccessor<Holder<PigVariant>> VARIANT() {
        throw new UnsupportedOperationException();
    }
}
