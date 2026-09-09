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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(BaseNPCLikeEntityRenderer.class)
public class BaseNPCLikeEntityRendererMixin {
    @Unique
    private static final ThreadLocal<CapturedNPC> REVERIE_CAPTURE = new ThreadLocal<>();

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
        REVERIE_CAPTURE.set(
                new CapturedNPC(
                        entity,
                        partialTick
                )
        );
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
        CapturedNPC captured = REVERIE_CAPTURE.get();

        if (captured == null) {
            return;
        }

        try {
            BaseNPCLikeEntity entity = captured.entity();

            if (entity.isRemoved()) {
                return;
            }

            /*
             * 防止 Renderer 被错误地复用到另一个 state。
             */
            if (state.id != entity.getId()) {
                return;
            }

            /*
             * 没有 YSM 模型 → 完全保持你原来的 PlayerModel renderer。
             */
            if (!entity.reverie_dreams$hasModel()) {
                return;
            }

            boolean rendered = SparkleClient.render(entity,
                    entity.getYRot(),
                    captured.partialTick(),
                    poseStack, collector,
                    Minecraft.getInstance()
                            .getEntityRenderDispatcher()
                            .getPackedLightCoords(entity, captured.partialTick())
            );

            /*
             * Sparkle 成功渲染以后，
             * 阻止 BaseNPCLikeEntityRenderer 的 PlayerModel。
             */
            if (rendered) {
                ci.cancel();
            }

        } finally {
            REVERIE_CAPTURE.remove();
        }
    }

}
