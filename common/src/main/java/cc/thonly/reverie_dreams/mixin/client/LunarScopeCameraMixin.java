package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.scope.LunarScopeClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class LunarScopeCameraMixin {
    @Shadow
    private float fovModifier;
    @Shadow
    private float oldFovModifier;

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void reverie_dreams$lunarScopeFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        float dynamic = oldFovModifier + (fovModifier - oldFovModifier) * partialTick;
        cir.setReturnValue(LunarScopeClient.modifyFov(cir.getReturnValue(), dynamic));
    }
}
