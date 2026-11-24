package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Hairball;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class HairballVariantModel extends DefaultedEntityGeoModel<Hairball> {
    public static final Identifier NORMAL = ReverieDreams.id("hairball");
    public static final Identifier BLACK = ReverieDreams.id("black_hairball");

    public HairballVariantModel() {
        super(NORMAL);
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.buildFormattedModelPath(renderState.getOrDefaultGeckolibData(Hairball.BLACK_COLOR_TICKET, false) ? BLACK : NORMAL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.buildFormattedTexturePath(renderState.getOrDefaultGeckolibData(Hairball.BLACK_COLOR_TICKET, false) ? BLACK : NORMAL);
    }

    @Override
    public Identifier getAnimationResource(Hairball entity) {
        return this.buildFormattedAnimationPath(entity.isBlackColor() ? BLACK : NORMAL);
    }
}
