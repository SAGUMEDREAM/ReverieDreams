package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitAppearance;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public final class YoukaiRabbitModel extends DefaultedEntityGeoModel<YoukaiRabbit> {
    public static final DataTicket<Boolean> FEMALE = DataTicket.create(ReverieDreams.MOD_ID + ":youkai_rabbit_female", Boolean.class);
    public static final DataTicket<Integer> VARIANT = DataTicket.create(ReverieDreams.MOD_ID + ":youkai_rabbit_variant", Integer.class);

    public static Identifier appearance(boolean female) {
        return appearance(female, 0);
    }

    public static Identifier appearance(boolean female, int variant) {
        return YoukaiRabbitAppearance.byId(variant).resourceId();
    }

    public YoukaiRabbitModel() {
        super(appearance(false));
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return this.buildFormattedModelPath(YoukaiRabbitAppearance.byId(state.getOrDefaultGeckolibData(VARIANT, 0)).geometryId());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return this.buildFormattedTexturePath(appearance(state.getOrDefaultGeckolibData(FEMALE, false), state.getOrDefaultGeckolibData(VARIANT, 0)));
    }

    @Override
    public Identifier getAnimationResource(YoukaiRabbit entity) {
        return this.buildFormattedAnimationPath(ReverieDreams.id("youkai_rabbit"));
    }
}
