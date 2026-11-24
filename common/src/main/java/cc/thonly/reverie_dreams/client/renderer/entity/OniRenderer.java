package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.OniModel;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.GensokyoFairyHelmetLayer;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.OniItemInHandLayer;
import cc.thonly.reverie_dreams.entity.Oni;
import cc.thonly.reverie_dreams.entity.variant.OniAppearance;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class OniRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<Oni, R> {
    public OniRenderer(EntityRendererProvider.Context context) {
        super(context, new OniModel());
        this.shadowRadius = 0.5F;
        this.withRenderLayer(new OniItemInHandLayer<>(context, this));
        this.withRenderLayer(new GensokyoFairyHelmetLayer<>(this, context));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState state) {
        return 0;
    }

    @Override
    public void captureDefaultRenderState(Oni entity, Void relatedObject, R state, float partialTick) {
        state.addGeckolibData(OniModel.APPEARANCE, OniAppearance.of(entity.getSkinType().getId()));
        super.captureDefaultRenderState(entity, relatedObject, state, partialTick);
    }
}
