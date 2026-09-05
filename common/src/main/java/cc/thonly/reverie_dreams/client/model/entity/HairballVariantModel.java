package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Hairball;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class HairballVariantModel extends DefaultedEntityGeoModel<Hairball> {
    public static final Identifier NORMAL = ReverieDreams.id("hairball");
    public static final Identifier BLACK = ReverieDreams.id("black_hairball");

    public HairballVariantModel() {
        super(NORMAL);
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        Boolean geckolibData = renderState.getGeckolibData(Hairball.BLACK_COLOR_TICKET);
        if (geckolibData == null) {
            return super.getModelResource(renderState);
        }
        if (geckolibData) {
            return BLACK;
        }
        return NORMAL;
    }
}
