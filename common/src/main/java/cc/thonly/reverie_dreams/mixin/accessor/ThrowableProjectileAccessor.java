package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThrowableProjectile.class)
public interface ThrowableProjectileAccessor {
    @Invoker("handleFirstTickBubbleColumn")
    void invokeHandleFirstTickBubbleColumn();

    @Invoker("applyInertia")
    void invokeApplyInertia();
}
