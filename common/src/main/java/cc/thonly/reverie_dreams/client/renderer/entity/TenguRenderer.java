package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.TenguModel;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.GensokyoFairyHelmetLayer;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.TenguItemInHandLayer;
import cc.thonly.reverie_dreams.entity.Tengu;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class TenguRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<Tengu, R> {
    public TenguRenderer(EntityRendererProvider.Context context) {
        super(context, new TenguModel());
        this.shadowRadius = .45F;
        this.withRenderLayer(new TenguItemInHandLayer<>(context, this));
        this.withRenderLayer(new GensokyoFairyHelmetLayer<>(this, context));
    }

    @Override
    public void captureDefaultRenderState(Tengu entity, Void related, R state, float partial) {
        state.addGeckolibData(TenguModel.APPEARANCE, entity.appearance());
        super.captureDefaultRenderState(entity, related, state, partial);
    }
}
