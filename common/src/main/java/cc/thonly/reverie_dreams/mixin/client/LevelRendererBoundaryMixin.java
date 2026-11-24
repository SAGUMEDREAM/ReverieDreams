package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.GensokyoBoundaryGizmos;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.gizmos.Gizmos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererBoundaryMixin {
    @Inject(method = "collectPerFrameGizmos", at = @At("TAIL"))
    private void reverie_dreams$collectBoundaryGizmos(CallbackInfoReturnable<Gizmos.TemporaryCollection> cir) {
        GensokyoBoundaryGizmos.collect();
    }
}
