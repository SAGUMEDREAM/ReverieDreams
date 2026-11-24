package cc.thonly.reverie_dreams.mixin.client;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
    @Accessor("layers")
    ItemStackRenderState.LayerRenderState[] reverie_dreams$getLayers();

    @Accessor("activeLayerCount")
    int reverie_dreams$getActiveLayerCount();

    @Accessor("cachedModelBoundingBox")
    void reverie_dreams$setCachedModelBoundingBox(@Nullable AABB bounds);
}
