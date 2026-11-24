package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.model.entity.GensokyoFairyModel;
import cc.thonly.reverie_dreams.entity.MaidYousei;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairySkin;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAppearance;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import cc.thonly.reverie_dreams.client.renderer.entity.layers.GensokyoFairyHelmetLayer;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyActions;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class GensokyoFairyRenderer<T extends BaseNPCLikeEntity & GeoEntity, R extends EntityRenderState & GeoRenderState>
        extends IGeoEntityRenderer<T, R> {
    public GensokyoFairyRenderer(EntityRendererProvider.Context context) {
        super(context, new GensokyoFairyModel<>());
        this.shadowRadius = 0.35F;
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this, "right_hand", "left_hand"));
        this.withRenderLayer(new GensokyoFairyHelmetLayer<>(this, context));
    }

    @Override
    public boolean shouldShowName(T entity, double distanceToCameraSq) {
        if (entity instanceof MaidYousei maid && maid.hasScarletName())
            return entity == this.entityRenderDispatcher.crosshairPickEntity && distanceToCameraSq < 64 * 64;
        return super.shouldShowName(entity, distanceToCameraSq);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState state) {
        return 0;
    }

    @Override
    public int getPackedOverlay(T entity, Void relatedObject, float u, float partialTick) {
        return entity.isDeadOrDying() ? OverlayTexture.NO_OVERLAY
                : super.getPackedOverlay(entity, relatedObject, u, partialTick);
    }

    @Override
    public RenderType getRenderType(R state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture, false);
    }

    @Override
    public int getRenderColor(T entity, Void relatedObject, float partialTick) {
        float alpha = entity.deathTime == 0 ? 1 : Mth.clamp((GensokyoFairyActions.DEATH_TICKS - entity.deathTime - partialTick) / 12.0F, 0, 1);
        return ARGB.color((int) (255 * alpha), 255, 255, 255);
    }

    @Override
    public void captureDefaultRenderState(T entity, Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(GensokyoFairyModel.APPEARANCE, GensokyoFairyAppearance.of(entity));
        renderState.addGeckolibData(GensokyoFairyModel.TEXTURE,
                GensokyoFairySkin.of(entity.getSkinType().getId()).textureId());
        super.captureDefaultRenderState(entity, relatedObject, renderState, partialTick);
    }
}
