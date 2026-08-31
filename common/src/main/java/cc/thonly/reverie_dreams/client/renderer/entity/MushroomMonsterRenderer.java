package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.MushroomMonsterModel;
import cc.thonly.reverie_dreams.entity.MushroomMonster;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class MushroomMonsterRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<MushroomMonster, R> {
    public MushroomMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new MushroomMonsterModel());
    }
}
