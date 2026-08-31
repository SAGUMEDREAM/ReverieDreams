package cc.thonly.reverie_dreams.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;

public class WingLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S,M> {
    final ItemModelResolver itemModelResolver;
    public WingLayer(RenderLayerParent<S, M> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S s, float v, float v1) {

    }
}
