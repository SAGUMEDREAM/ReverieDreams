package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Oni;
import cc.thonly.reverie_dreams.entity.variant.OniAppearance;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public final class OniModel extends DefaultedEntityGeoModel<Oni> {
    public static final DataTicket<OniAppearance> APPEARANCE = DataTicket.create(ReverieDreams.MOD_ID + ":oni_appearance", OniAppearance.class);

    public OniModel() {
        super(ReverieDreams.id("oni_green"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return state.getOrDefaultGeckolibData(APPEARANCE, OniAppearance.GREEN).resourceId();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return state.getOrDefaultGeckolibData(APPEARANCE, OniAppearance.GREEN).textureId();
    }

    @Override
    public Identifier getAnimationResource(Oni entity) {
        return OniAppearance.of(entity.getSkinType().getId()).resourceId();
    }
}
