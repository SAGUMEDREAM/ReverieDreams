package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.entity.villager.AbstractSeller;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

@SuppressWarnings("deprecation")
public class VillagerSellerRenderer<V extends AbstractSeller> extends AgeableMobRenderer<V, VillagerRenderState, VillagerModel> {
    private static final Identifier VILLAGER_BASE_SKIN = Identifier.withDefaultNamespace("textures/entity/villager/villager.png");
    public static final CustomHeadLayer.Transforms CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(-0.1171875F, -0.07421875F, 1.0F);

    public VillagerSellerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache(), CUSTOM_HEAD_TRANSFORMS));
        this.addLayer(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager", new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_NO_HAT)), new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY_NO_HAT))));
        this.addLayer(new CrossedArmsItemLayer<>(this));
    }

    public Identifier getTextureLocation(VillagerRenderState renderState) {
        return VILLAGER_BASE_SKIN;
    }

    protected float getShadowRadius(VillagerRenderState renderState) {
        float f = super.getShadowRadius(renderState);
        return renderState.isBaby ? f * 0.5F : f;
    }

    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    public void extractRenderState(V entity, VillagerRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        HoldingEntityRenderState.extractHoldingEntityRenderState(entity, renderState, this.itemModelResolver);
        renderState.isUnhappy = entity.getUnhappyCounter() > 0;
        renderState.villagerData = entity.tryGetModifyVillagerData();
    }
}
