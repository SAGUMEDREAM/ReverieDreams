package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.client.ClientModelManager;
import com.micaftic.morpher.client.renderer.SubmitRenderContext;
import com.micaftic.morpher.mixin.client.EntityRenderDispatcherAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("resource")
@Slf4j
public class SparkleClient {
    public static final Map<ResourceKey<Level>, Map<Integer, NPCAnimatable>> ANIMATABLES = new WeakHashMap<>();

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
            renderer = new SparkleRenderer(createContext());
        }

        return renderer;
    }

    private static NPCAnimatable animatable(BaseNPCLikeEntity entity) {
        ResourceKey<Level> dimension = entity.level().dimension();
        int entityId = entity.getId();

        Map<Integer, NPCAnimatable> entities = ANIMATABLES.computeIfAbsent(
                dimension,
                key -> new HashMap<>()
        );

        NPCAnimatable current = entities.get(entityId);

        if (current == null || current.getEntity() != entity) {
            current = new NPCCapability(entity);
            entities.put(entityId, current);
        }

        return current;
    }

    public static void remove(BaseNPCLikeEntity entity) {
        ResourceKey<Level> dimension = entity.level().dimension();

        Map<Integer, NPCAnimatable> entities = ANIMATABLES.get(dimension);
        if (entities == null) {
            return;
        }

        entities.remove(entity.getId());

        if (entities.isEmpty()) {
            ANIMATABLES.remove(dimension);
        }
    }

    public static boolean render(
            NPCAvatarRenderState renderState,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector
    ) {
        String modelId = renderState.modelId;
        if (modelId == null || modelId.isBlank()) {
            return false;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }

        Entity entity = level.getEntity(renderState.id);
        if (!(entity instanceof BaseNPCLikeEntity npc)) {
            return false;
        }

        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        int packedLight = entityRenderDispatcher.getPackedLightCoords(entity, renderState.partialTick);

        NPCAnimatable animatable = animatable(npc);
        String texture = renderState.modelTexture;

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
        } catch (Exception e) {
            log.error("Error: ", e);
        } finally {
            SubmitRenderContext.set(null);
        }

        bufferSource.endBatch();

        return true;
    }
}
