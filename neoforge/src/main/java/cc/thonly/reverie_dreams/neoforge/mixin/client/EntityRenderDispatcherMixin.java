package cc.thonly.reverie_dreams.neoforge.mixin.client;

import cc.thonly.reverie_dreams.client.CapturedEntity;
import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.client.renderer.CustomFishingHookRenderer;
import com.micaftic.morpher.client.renderer.CustomProjectileRenderer;
import com.micaftic.morpher.client.renderer.CustomVehicleRenderer;
import com.micaftic.morpher.client.renderer.ModelPreviewRenderer;
import com.micaftic.morpher.config.GeneralConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Unique
    private static final Map<EntityRenderState, CapturedEntity> CAPTURED_ENTITIES = Collections.synchronizedMap(new IdentityHashMap<>());

//    @Inject(method = "extractEntity", at = @At("RETURN"))
//    private <E extends Entity> void reverie_dreams$captureEntity(E entity,
//                                                                 float partialTicks,
//                                                                 CallbackInfoReturnable<EntityRenderState> cir) {
//        EntityRenderState state = cir.getReturnValue();
//        if (state != null) {
//            int packedLight = ((EntityRenderDispatcher)(Object)this).getPackedLightCoords(entity, partialTicks);
//            CAPTURED_ENTITIES.put(state, new CapturedEntity(entity, partialTicks, packedLight));
//        }
//    }
//
//    @WrapWithCondition(
//            method = {"submit"},
//            at = {@At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
//            )}
//    )
//    private boolean ysm$renderCustom(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
//        CapturedEntity captured = CAPTURED_ENTITIES.remove(state);
//        if (ReverieDreamsCompats.SPARKLE_MORPHER_YSM_SUBMIT != null) {
//            return ReverieDreamsCompats.SPARKLE_MORPHER_YSM_SUBMIT.invoke(captured, );
//        }
//        return false;
//    }
}
