package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.UfoModel;
import cc.thonly.reverie_dreams.entity.UFO;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class UfoRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<UFO, R> {
    public UfoRenderer(EntityRendererProvider.Context context) {
        super(context, new UfoModel());
    }
}
