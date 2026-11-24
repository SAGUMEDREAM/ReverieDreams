package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.block.decor.living.FutonBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keep the normal bed clearance above a mattress five pixels lower than a vanilla bed.
 */
@Mixin(LivingEntity.class)
public abstract class FutonSleepingHeightMixin {
    @Inject(method = "setPosToBed", at = @At("TAIL"))
    private void reverie_dreams$lowerToFuton(BlockPos pos, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().getBlockState(pos).getBlock() instanceof FutonBlock)
            entity.setPos(entity.getX(), pos.getY() + 6.0 / 16.0, entity.getZ());
    }
}
