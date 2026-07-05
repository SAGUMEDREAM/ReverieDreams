package cc.thonly.reverie_dreams.compat.ysm;

import cc.thonly.reverie_dreams.client.CapturedEntity;
import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import cc.thonly.reverie_dreams.util.YsmModType;
import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.client.renderer.CustomFishingHookRenderer;
import com.micaftic.morpher.client.renderer.CustomProjectileRenderer;
import com.micaftic.morpher.client.renderer.CustomVehicleRenderer;
import com.micaftic.morpher.client.renderer.ModelPreviewRenderer;
import com.micaftic.morpher.config.GeneralConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.jspecify.annotations.Nullable;

@Slf4j
public class SparkleMorpherCompats {
    public static void bootstrap() {
        YsmModType.setType(YsmModType.Type.SPARKLE_MORPHER);
        try {
//            ReverieDreamsCompats.SPARKLE_MORPHER_YSM_SUBMIT = SparkleMorpherCompats.class.getDeclaredMethod("submit", CapturedEntity.class, EntityRenderer.class, EntityRenderState.class, PoseStack.class, SubmitNodeCollector.class, CameraRenderState.class);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

//    public static boolean submit(@Nullable CapturedEntity captured, EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
//        if (captured == null) {
//            return true;
//        }
//        Entity entity = captured.entity();
//        if (!YesSteveModel.isAvailable()) {
//            return true;
//        } else {
//            float partialTick = captured.partialTick();
//            float entityYaw = entity.getYRot();
//            int packedLight = captured.packedLight();
//            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//            if (entity instanceof Projectile projectile) {
//                if (!(Boolean) GeneralConfig.DISABLE_PROJECTILE_MODEL.get()) {
//                    if (projectile instanceof FishingHook fishingHook) {
//                        boolean shouldRenderVanilla = CustomFishingHookRenderer.tryRenderCustomHook(fishingHook, entityYaw, partialTick, poseStack, bufferSource, packedLight);
//                        if (!shouldRenderVanilla) {
//                            bufferSource.endBatch();
//                        }
//
//                        return shouldRenderVanilla;
//                    }
//
//                    boolean shouldRenderVanilla = CustomProjectileRenderer.renderProjectile(projectile, entityYaw, partialTick, poseStack, bufferSource, packedLight);
//                    if (!shouldRenderVanilla) {
//                        bufferSource.endBatch();
//                    }
//
//                    return shouldRenderVanilla;
//                }
//            }
//
//            if (!(Boolean)GeneralConfig.DISABLE_VEHICLE_MODEL.get()) {
//                ModelPreviewRenderer.renderVehicleModel(entity, poseStack, partialTick);
//                boolean shouldRenderVanilla = CustomVehicleRenderer.renderVehicle(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
//                if (!shouldRenderVanilla) {
//                    bufferSource.endBatch();
//                }
//
//                return shouldRenderVanilla;
//            } else {
//                return true;
//            }
//        }
//    }
}
