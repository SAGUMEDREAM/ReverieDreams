package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.CuisineAquaticModel;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.entity.aquatic.CuisineAquaticEntity;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class CuisineAquaticRenderer<R extends EntityRenderState & GeoRenderState>
        extends IGeoEntityRenderer<CuisineAquaticEntity, R> {
    public CuisineAquaticRenderer(EntityRendererProvider.Context context, AquaticSpecies species) {
        super(context, new CuisineAquaticModel(species));
        this.shadowRadius = species.width() * .35f;
    }
}
