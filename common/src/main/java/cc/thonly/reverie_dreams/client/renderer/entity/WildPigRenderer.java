package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.pig.BabyPigModel;
import net.minecraft.client.model.animal.pig.ColdPigModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;

import java.util.Map;

public class WildPigRenderer extends MobRenderer<Pig, PigRenderState, PigModel> {
    private final Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> models;

    public WildPigRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), 0.7F);
        this.models = bakeModels(context);
        this.addLayer(
                new SimpleEquipmentLayer<>(
                        this,
                        context.getEquipmentRenderer(),
                        EquipmentClientInfo.LayerType.PIG_SADDLE,
                        state -> state.saddle,
                        new PigModel(context.bakeLayer(ModelLayers.PIG_SADDLE)),
                        null
                )
        );
    }

    private static Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> bakeModels(EntityRendererProvider.Context context) {
        return Maps.newEnumMap(
                Map.of(
                        PigVariant.ModelType.NORMAL,
                        new AdultAndBabyModelPair<>(new PigModel(context.bakeLayer(ModelLayers.PIG)), new BabyPigModel(context.bakeLayer(ModelLayers.PIG_BABY))),
                        PigVariant.ModelType.COLD,
                        new AdultAndBabyModelPair<>(
                                new ColdPigModel(context.bakeLayer(ModelLayers.COLD_PIG)), new BabyPigModel(context.bakeLayer(ModelLayers.PIG_BABY))
                        )
                )
        );
    }

    public void submit(PigRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.variant != null) {
            this.model = this.models.get(state.variant.modelAndTexture().model()).getModel(state.isBaby);
            super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
        }
    }

    public Identifier getTextureLocation(PigRenderState state) {
        return state.variant == null ? MissingTextureAtlasSprite.getLocation() : state.variant.modelAndTexture().asset().texturePath();
    }

    public PigRenderState createRenderState() {
        return new PigRenderState();
    }

    @SuppressWarnings("resource")
    public void extractRenderState(Pig pig, PigRenderState renderState, float partialTick) {
        super.extractRenderState(pig, renderState, partialTick);
        RegistryAccess registryAccess = pig.level().registryAccess();
        Registry<PigVariant> pigVariants = registryAccess.lookupOrThrow(Registries.PIG_VARIANT);
        renderState.saddle = pig.getItemBySlot(EquipmentSlot.SADDLE).copy();
        renderState.variant = pigVariants.getValue(ReverieDreams.id("wild_pig"));
    }
}
