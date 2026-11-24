package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.ScarecrowModel;
import cc.thonly.reverie_dreams.entity.Scarecrow;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class ScarecrowRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<Scarecrow, R> {
    public ScarecrowRenderer(EntityRendererProvider.Context context) {
        super(context, new ScarecrowModel());
    }
}
