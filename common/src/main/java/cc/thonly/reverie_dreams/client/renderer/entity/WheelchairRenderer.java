package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.ItemHolderRenderState;
import cc.thonly.reverie_dreams.entity.misc.MagicBroom;
import cc.thonly.reverie_dreams.entity.misc.Wheelchair;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;

public class WheelchairRenderer extends EntityRenderer<Wheelchair, ItemHolderRenderState> {
    private final ItemModelResolver itemModelResolver;

    public WheelchairRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void extractRenderState(Wheelchair entity, ItemHolderRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.itemStack = RDBlocks.WHEEL_CHAIR.createStack();
        renderState.yBodyRot = entity.yBodyRot;
        renderState.xRot = entity.getXRot();
        renderState.yRot = entity.getYRot();
        renderState.xRotO = entity.xRotO;
        renderState.yRotO = entity.yRotO;
        this.itemModelResolver.updateForNonLiving(renderState.itemRenderState, renderState.itemStack, ItemDisplayContext.HEAD, entity);
    }

    @Override
    public void submit(ItemHolderRenderState renderState, PoseStack matrices, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(-renderState.yRot));
        matrices.popPose();
        super.submit(renderState, matrices, nodeCollector, cameraRenderState);
    }

    @Override
    public ItemHolderRenderState createRenderState() {
        return new ItemHolderRenderState();
    }
}
