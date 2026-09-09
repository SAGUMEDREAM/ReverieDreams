package cc.thonly.reverie_dreams.fabric.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;

import cc.thonly.reverie_dreams.entity.npc.NPCMorphData;
import com.micaftic.morpher.client.ClientModelManager;
import com.micaftic.morpher.client.renderer.SubmitRenderContext;
import com.micaftic.morpher.mixin.client.EntityRenderDispatcherAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Map;
import java.util.WeakHashMap;

public class SparkleClient {
    private static final Map<BaseNPCLikeEntity, NPCAnimatable> ANIMATABLES = new WeakHashMap<>();

    private static SparkleRenderer renderer;

    private static EntityRendererProvider.Context createContext() {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        EntityRenderDispatcherAccessor accessor = (EntityRenderDispatcherAccessor) dispatcher;

        return new EntityRendererProvider.Context(
                dispatcher,
                accessor.ysm$getBlockModelResolver(),
                accessor.ysm$getItemModelResolver(),
                accessor.ysm$getMapRenderer(),
                minecraft.getResourceManager(),
                accessor.ysm$getEntityModels().get(),
                accessor.ysm$getEquipmentAssets(),
                accessor.ysm$getAtlasManager(),
                accessor.ysm$getFont(),
                accessor.ysm$getPlayerSkinRenderCache()
        );
    }

    private static SparkleRenderer renderer() {
        if (renderer == null) {
            renderer = new SparkleRenderer(
                    createContext()
            );
        }

        return renderer;
    }

    private static NPCAnimatable animatable(
            BaseNPCLikeEntity entity
    ) {
        return ANIMATABLES.computeIfAbsent(
                entity,
                NPCCapability::new
        );
    }

    public static boolean render(
            BaseNPCLikeEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight
    ) {
        if (!(entity instanceof NPCMorphData data)) {
            return false;
        }

        String modelId = data.reverie_dreams$getModelId();

        if (modelId == null || modelId.isBlank()) {
            return false;
        }

        NPCAnimatable animatable = animatable(entity);
        String texture = data.reverie_dreams$getTexture();

        if (!modelId.equals(animatable.getModelId())) {
            animatable.initModelWithTexture(modelId, texture);
        } else if (texture != null && !texture.equals(animatable.getCurrentTextureName())) {
            animatable.setCurrentTexture(texture);
        }

        animatable.tickModel();

        if (!animatable.isModelReady()) {
            return false;
        }

        ClientModelManager.markModelUsed(modelId);

        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        SparkleRenderer sparkleRenderer = renderer();
        SubmitRenderContext.set(collector);

        try {
            sparkleRenderer.renderEntity(
                    animatable,
                    entity.getYRot(),
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight
            );
        } finally {
            SubmitRenderContext.set(null);
        }

        bufferSource.endBatch();

        return true;
    }
}
