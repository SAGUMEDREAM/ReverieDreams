package cc.thonly.reverie_dreams.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    static EntityDataAccessor<Optional<BlockPos>> getSLEEPING_POSITION() {
        throw new UnsupportedOperationException();
    }
}
