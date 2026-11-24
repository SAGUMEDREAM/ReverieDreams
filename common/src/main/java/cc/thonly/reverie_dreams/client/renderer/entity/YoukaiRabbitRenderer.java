package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.YoukaiRabbitModel;
import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class YoukaiRabbitRenderer<R extends EntityRenderState & GeoRenderState> extends IGeoEntityRenderer<YoukaiRabbit, R> {
    public YoukaiRabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new YoukaiRabbitModel());
        this.shadowRadius = .35F;
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this, "right_hand", "left_hand"));
    }

    @Override
    public void captureDefaultRenderState(YoukaiRabbit entity, Void related, R state, float partialTick) {
        state.addGeckolibData(YoukaiRabbitModel.FEMALE, entity.isFemale());
        state.addGeckolibData(YoukaiRabbitModel.VARIANT, entity.getVariant());
        super.captureDefaultRenderState(entity, related, state, partialTick);
    }
}
