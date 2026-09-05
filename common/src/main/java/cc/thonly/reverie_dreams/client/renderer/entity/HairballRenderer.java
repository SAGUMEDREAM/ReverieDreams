package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.HairballVariantModel;
import cc.thonly.reverie_dreams.entity.Hairball;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class HairballRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<Hairball, R> {
    public HairballRenderer(EntityRendererProvider.Context context) {
        super(context, new HairballVariantModel());
    }
}
