package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ForestYoukai;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public final class ForestYoukaiModel extends DefaultedEntityGeoModel<ForestYoukai> {
    public static final DataTicket<Identifier> APPEARANCE = DataTicket.create(ReverieDreams.MOD_ID + ":forest_youkai_appearance", Identifier.class);

    public ForestYoukaiModel() {
        super(ReverieDreams.id("youkai_cat_female_01"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return this.buildFormattedModelPath(state.getOrDefaultGeckolibData(APPEARANCE, ReverieDreams.id("youkai_cat_female_01")));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return this.buildFormattedTexturePath(state.getOrDefaultGeckolibData(APPEARANCE, ReverieDreams.id("youkai_cat_female_01")));
    }

    @Override
    public Identifier getAnimationResource(ForestYoukai entity) {
        return this.buildFormattedAnimationPath(entity.species().appearance(entity.variant()));
    }
}
