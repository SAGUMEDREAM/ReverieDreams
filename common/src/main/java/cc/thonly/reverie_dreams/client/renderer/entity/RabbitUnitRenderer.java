package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.RabbitUnitModel;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.GensokyoFairyHelmetLayer;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.RabbitUnitItemInHandLayer;
import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAppearance;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public final class RabbitUnitRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<RabbitUnit, R> {
    public RabbitUnitRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitUnitModel());
        this.shadowRadius = .35f;
        this.withRenderLayer(new RabbitUnitItemInHandLayer<>(context, this));
        this.withRenderLayer(new GensokyoFairyHelmetLayer<>(this, context));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState state) {
        return 0;
    }

    @Override
    public int getPackedOverlay(RabbitUnit entity, Void related, float u, float partialTick) {
        return entity.isDeadOrDying() ? OverlayTexture.NO_OVERLAY : super.getPackedOverlay(entity, related, u, partialTick);
    }

    @Override
    public RenderType getRenderType(R state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture, false);
    }

    @Override
    public void captureDefaultRenderState(RabbitUnit entity, Void related, R state, float partialTick) {
        state.addGeckolibData(RabbitUnitModel.APPEARANCE, RabbitUnitAppearance.of(entity.getSkinType().getId()));
        super.captureDefaultRenderState(entity, related, state, partialTick);
    }
}
