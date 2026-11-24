package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {
    @Inject(method = "computeMargin", at = @At("RETURN"), cancellable = true)
    private static void reverie_dreams$enlargedDanmakuHitMargin(Entity source, CallbackInfoReturnable<Float> cir) {
        if (source instanceof BaseDanmakuEntity danmaku) {
            // Vanilla traces the projectile centre against inflated targets; its own width is not used.
            // Small bullets reduce the early sweep margin, but never below a tiny positive value.
            cir.setReturnValue(Math.max(0.02F, cir.getReturnValue() + danmaku.getExtraHitMargin()));
        }
    }
}
