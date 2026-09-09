package cc.thonly.reverie_dreams.fabric.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.AbstractNPCEntity;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.fabric.integration.sparkle.NPCAnimatable;

import java.util.Map;
import java.util.WeakHashMap;

public class NPCMorphManager {

    private static final Map<BaseNPCLikeEntity, NPCAnimatable> CACHE = new WeakHashMap<>();

    private NPCMorphManager() {
    }

    public static NPCAnimatable get(
            BaseNPCLikeEntity entity
    ) {
        return CACHE.computeIfAbsent(
                entity,
                NPCCapability::new
        );
    }

    public static void remove(
            BaseNPCLikeEntity entity
    ) {
        NPCAnimatable animatable = CACHE.remove(entity);

        if (animatable != null) {
            animatable.resetModel();
        }
    }

    public static void tick(
            BaseNPCLikeEntity entity
    ) {
        NPCAnimatable animatable = get(entity);

        String modelId = entity.reverie_dreams$getModelId();
        String texture = entity.reverie_dreams$getTexture();

        if (modelId == null || modelId.isBlank()) {
            if (animatable.isModelActive()) {
                animatable.resetModel();
            }
            return;
        }

        if (!modelId.equals(animatable.getModelId())) {
            animatable.initModelWithTexture(
                    modelId,
                    texture
            );
        }

        animatable.tickModel();
    }
}