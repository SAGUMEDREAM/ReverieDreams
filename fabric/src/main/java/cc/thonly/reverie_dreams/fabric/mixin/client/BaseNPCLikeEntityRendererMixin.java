package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.entity.BaseNPCLikeEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import cc.thonly.reverie_dreams.fabric.integration.sparkle.CapturedNPC;
import cc.thonly.reverie_dreams.fabric.integration.sparkle.SparkleClient;
import cc.thonly.reverie_dreams.util.YsmHolder;
import com.micaftic.morpher.capability.PlayerCapability;
import com.micaftic.morpher.capability.VehicleCapability;
import com.micaftic.morpher.client.renderer.ModelPreviewRenderer;
import com.micaftic.morpher.mixin.client.MinecraftAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
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

    /**
     * 你的 renderer 的 extractRenderState() 有真正的 Entity，
     * 而 submit() 只有 RenderState。
     * <p>
     * 所以这里把 entity + partialTick 暂存起来。
     */
    @Inject(
            method = "extractRenderState(Lcc/thonly/reverie_dreams/entity/npc/BaseNPCLikeEntity;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void reverie_dreams$captureEntity(BaseNPCLikeEntity entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {

    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reverie_dreams$submitSparkle(
            AvatarRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
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
                    poseStack, collector
            );

            if (rendered) {
                ci.cancel();
            }

        } catch (Exception e) {
            log.error("Renderer Error: ", e);
        }
    }

}
