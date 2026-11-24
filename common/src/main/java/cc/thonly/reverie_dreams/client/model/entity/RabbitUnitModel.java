package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAppearance;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public final class RabbitUnitModel extends DefaultedEntityGeoModel<RabbitUnit> {
    public static final DataTicket<RabbitUnitAppearance> APPEARANCE = DataTicket.create(ReverieDreams.MOD_ID + ":rabbit_unit_appearance", RabbitUnitAppearance.class);

    public RabbitUnitModel() {
        super(ReverieDreams.id("rabbit_unit_0"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return state.getOrDefaultGeckolibData(APPEARANCE, RabbitUnitAppearance.LILAC).resourceId();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return state.getOrDefaultGeckolibData(APPEARANCE, RabbitUnitAppearance.LILAC).textureId();
    }

    @Override
    public Identifier getAnimationResource(RabbitUnit entity) {
        return RabbitUnitAppearance.of(entity.getSkinType().getId()).resourceId();
    }
}
