package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.HairballVariantModel;
import cc.thonly.reverie_dreams.entity.Hairball;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class HairballRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<Hairball, R> {
    public HairballRenderer(EntityRendererProvider.Context context) {
        super(context, new HairballVariantModel());
    }

    @Override
    public void captureDefaultRenderState(Hairball entity, Void relatedObject, R state, float partialTick) {
        state.addGeckolibData(Hairball.BLACK_COLOR_TICKET, entity.isBlackColor());
        super.captureDefaultRenderState(entity, relatedObject, state, partialTick);
    }
}
