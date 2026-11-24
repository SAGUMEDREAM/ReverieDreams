package cc.thonly.reverie_dreams.client.model.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairySkin;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAppearance;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class GensokyoFairyModel<T extends BaseNPCLikeEntity & GeoEntity> extends DefaultedEntityGeoModel<T> {
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create(ReverieDreams.MOD_ID + ":fairy_texture", Identifier.class);
    public static final DataTicket<GensokyoFairyAppearance> APPEARANCE = DataTicket.create(ReverieDreams.MOD_ID + ":fairy_appearance", GensokyoFairyAppearance.class);

    public GensokyoFairyModel() {
        super(ReverieDreams.id("gensokyo_fairy"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        var appearance = renderState.getGeckolibData(APPEARANCE);
        if (appearance != null) {
            return appearance.textureId();
        }
        return renderState.getOrDefaultGeckolibData(TEXTURE, GensokyoFairySkin.BLUE.textureId());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        var appearance = renderState.getGeckolibData(APPEARANCE);
        return appearance != null ? appearance.resourceId() : super.getModelResource(renderState);
    }

    @Override
    public Identifier getAnimationResource(T entity) {
        var appearance = GensokyoFairyAppearance.of(entity);
        return appearance != null ? appearance.resourceId() : super.getAnimationResource(entity);
    }
}
