package cc.thonly.reverie_dreams.client;

import net.minecraft.world.entity.Entity;

public record CapturedEntity(Entity entity, float partialTick, int packedLight) {
}
