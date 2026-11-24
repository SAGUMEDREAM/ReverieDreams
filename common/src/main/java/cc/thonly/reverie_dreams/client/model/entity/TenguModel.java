package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Tengu;
import cc.thonly.reverie_dreams.entity.variant.TenguAppearance;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public final class TenguModel extends DefaultedEntityGeoModel<Tengu> {
    public static final DataTicket<TenguAppearance> APPEARANCE = DataTicket.create(ReverieDreams.MOD_ID + ":tengu_appearance", TenguAppearance.class);

    public TenguModel() {
        super(ReverieDreams.id("white_wolf_tengu_01"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return state.getOrDefaultGeckolibData(APPEARANCE, TenguAppearance.WOLF_RED).resourceId();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return state.getOrDefaultGeckolibData(APPEARANCE, TenguAppearance.WOLF_RED).textureId();
    }

    @Override
    public Identifier getAnimationResource(Tengu entity) {
        return entity.appearance().resourceId();
    }
}
