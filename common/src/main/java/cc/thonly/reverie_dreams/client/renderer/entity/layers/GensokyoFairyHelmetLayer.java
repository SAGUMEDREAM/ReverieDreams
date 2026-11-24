package cc.thonly.reverie_dreams.client.renderer.entity.layers;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemArmorGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.List;

public final class GensokyoFairyHelmetLayer<T extends BaseNPCLikeEntity & GeoEntity, R extends EntityRenderState & GeoRenderState>
        extends ItemArmorGeoLayer<T, Void, R> {
    public GensokyoFairyHelmetLayer(GeoRenderer<T, Void, R> renderer, EntityRendererProvider.Context context) {
        super(renderer, context);
    }

    @Override
    protected List<RenderData> getRelevantBones(RenderPassInfo<R> renderPassInfo) {
        return List.of(RenderData.head("head"));
    }
}
