package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.UfoModel;
import cc.thonly.reverie_dreams.entity.UFO;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class UfoRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<UFO, R> {
    public UfoRenderer(EntityRendererProvider.Context context) {
        super(context, new UfoModel());
    }
}
