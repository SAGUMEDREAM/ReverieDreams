package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.NPCSpeechRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class NPCSpeechRendererMixin {
    @Inject(method = "submitInner(Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V",
            at = @At("HEAD"), cancellable = true)
    private void reverieDreams$renderSpeech(TextDisplayEntityRenderState state, PoseStack poses,
                                            SubmitNodeCollector collector, int light, float interpolation, CallbackInfo ci) {
        if (NPCSpeechRenderer.render(state, poses, collector, light, interpolation)) {
            ci.cancel();
        }
    }
}
