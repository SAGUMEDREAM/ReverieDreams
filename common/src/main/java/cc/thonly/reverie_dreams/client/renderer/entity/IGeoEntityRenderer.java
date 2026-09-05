package cc.thonly.reverie_dreams.client.renderer.entity;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class IGeoEntityRenderer<
        T extends Entity & GeoAnimatable,
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<T, R> {
    public IGeoEntityRenderer(EntityRendererProvider.Context context, EntityType<T> entityType) {
        super(context, entityType);
    }
}
