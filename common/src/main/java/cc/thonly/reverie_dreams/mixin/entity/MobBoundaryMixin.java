package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobBoundaryMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void reverie_dreams$confineToMap(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!mob.level().isClientSide()) {
            GensokyoBoundary.confineMob(mob);
        }
    }
}
