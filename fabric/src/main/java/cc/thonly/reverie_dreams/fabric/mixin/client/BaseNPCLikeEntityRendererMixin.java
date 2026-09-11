package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.entity.BaseNPCLikeEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.fabric.integration.sparkle.SparkleClient;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Slf4j
@Pseudo
@Mixin(BaseNPCLikeEntityRenderer.class)
public abstract class BaseNPCLikeEntityRendererMixin<NPCEntity extends BaseNPCLikeEntity> extends LivingEntityRenderer<NPCEntity, AvatarRenderState, PlayerModel> {
    public BaseNPCLikeEntityRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadow) {
        super(context, model, shadow);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reverie_dreams$submitSparkle(
            AvatarRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!(state instanceof NPCAvatarRenderState renderState)) {
            return;
        }
        try {
            if (!renderState.reverie_dreams$hasModel()) {
                return;
            }

            boolean rendered = SparkleClient.render(renderState,
                    renderState.yRot,
                    renderState.partialTick,
                    poseStack, submitNodeCollector
            );

            if (rendered) {
                ci.cancel();
            }

        } catch (Exception e) {
            log.error("Renderer Error: ", e);
        }
    }

}
