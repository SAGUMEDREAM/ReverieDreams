package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.ScarecrowModel;
import cc.thonly.reverie_dreams.entity.Scarecrow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ScarecrowRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<Scarecrow, R> {
    public ScarecrowRenderer(EntityRendererProvider.Context context) {
        super(context, new ScarecrowModel());
    }
}
