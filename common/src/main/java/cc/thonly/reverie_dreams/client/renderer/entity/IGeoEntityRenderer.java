package cc.thonly.reverie_dreams.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

@Deprecated
public class IGeoEntityRenderer<
        T extends Entity & GeoAnimatable,
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<T, R> {
    public IGeoEntityRenderer(EntityRendererProvider.Context context, EntityType<T> entityType) {
        super(context, entityType);
    }
}
