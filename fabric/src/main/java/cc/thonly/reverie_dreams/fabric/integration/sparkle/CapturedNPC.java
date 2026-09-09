package cc.thonly.reverie_dreams.fabric.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;

public record CapturedNPC(
        BaseNPCLikeEntity entity,
        float partialTick
) {
}