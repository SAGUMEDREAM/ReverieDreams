package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.ForestYoukaiModel;
import cc.thonly.reverie_dreams.entity.ForestYoukai;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class ForestYoukaiRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<ForestYoukai, R> {
    public ForestYoukaiRenderer(EntityRendererProvider.Context context) {
        super(context, new ForestYoukaiModel());
        this.shadowRadius = .35F;
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this, "right_hand", "left_hand"));
    }

    @Override
    public void captureDefaultRenderState(ForestYoukai entity, Void related, R state, float partialTick) {
        state.addGeckolibData(ForestYoukaiModel.APPEARANCE, entity.species().appearance(entity.variant()));
        super.captureDefaultRenderState(entity, related, state, partialTick);
    }
}
