package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.MushroomMonsterModel;
import cc.thonly.reverie_dreams.client.model.entity.ScarecrowModel;
import cc.thonly.reverie_dreams.entity.MushroomMonster;
import cc.thonly.reverie_dreams.entity.Scarecrow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class MushroomMonsterRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<MushroomMonster, R> {
    public MushroomMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new MushroomMonsterModel());
    }
}
