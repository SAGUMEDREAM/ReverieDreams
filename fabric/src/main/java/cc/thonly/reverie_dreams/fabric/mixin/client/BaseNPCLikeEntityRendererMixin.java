package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.entity.BaseNPCLikeEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import cc.thonly.reverie_dreams.util.YsmHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Pseudo
@Mixin(BaseNPCLikeEntityRenderer.class)
public class BaseNPCLikeEntityRendererMixin {
//    @Inject(
//            method = {"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"},
//            at = {@At("HEAD")},
//            cancellable = true
//    )
    public void reverie_dreams$onYsmSubmit(LivingEntityRenderState state,
                                     PoseStack poseStack,
                                     SubmitNodeCollector collector,
                                     CameraRenderState cameraState,
                                     CallbackInfo ci) {
        if (!YsmHolder.isInitialized()) {
            return;
        }
        this.reverie_dreams$onYsmSubmitProxy(state, poseStack, collector, cameraState, ci);
    }

    @Unique
    public void reverie_dreams$onYsmSubmitProxy(LivingEntityRenderState state,
                                                PoseStack poseStack,
                                                SubmitNodeCollector collector,
                                                CameraRenderState cameraState,
                                                CallbackInfo ci) {
        if (!YsmHolder.isInitialized()) {
            return;
        }
//        if (state instanceof NPCAvatarRenderState avatarState) {
//            if (Minecraft.getInstance().level != null && avatarState.dimension != null) {
//                NPCCompanionEntity player = reverie_dreams_ysm$resolveNPC(avatarState);
//                if (player != null) {
//                    float partialTick = ((MinecraftAccessor)Minecraft.getInstance()).ysm$getDeltaTracker().getGameTimeDeltaPartialTick(false);
//                    int packedLight = ((MinecraftAccessor)Minecraft.getInstance()).ysm$getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
//                    boolean preview = ModelPreviewRenderer.isPreview();
//                    float yaw = preview ? state.bodyRot : state.yRot;
//                    float oldBodyRot = player.yBodyRot;
//                    float oldBodyRotO = player.yBodyRotO;
//                    float oldYRot = player.getYRot();
//                    float oldYRotO = player.yRotO;
//                    float oldXRot = player.getXRot();
//                    float oldXRotO = player.xRotO;
//                    float oldHeadRot = player.yHeadRot;
//                    float oldHeadRotO = player.yHeadRotO;
//                    if (preview) {
//                        float bodyRot = state.bodyRot;
//                        float headRot = bodyRot + state.yRot;
//                        player.yBodyRot = bodyRot;
//                        player.yBodyRotO = bodyRot;
//                        player.setYRot(headRot);
//                        player.yRotO = headRot;
//                        player.setXRot(state.xRot);
//                        player.xRotO = state.xRot;
//                        player.yHeadRot = headRot;
//                        player.yHeadRotO = headRot;
//                    }
//
//                    VehicleCapability capability = (VehicleCapability)PlayerCapability.get(player).orElse((Object)null);
//                    if (capability != null) {
//                        capability.beginRenderState(avatarState);
//                    }
//
//                }
//            }
//        }
    }

    @Unique
    private static NPCCompanionEntity reverie_dreams_ysm$resolveNPC(NPCAvatarRenderState state) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level.getEntity(state.id);
        if (entity instanceof NPCCompanionEntity npc) {
            return npc;
        }
        return null;
    }
}
