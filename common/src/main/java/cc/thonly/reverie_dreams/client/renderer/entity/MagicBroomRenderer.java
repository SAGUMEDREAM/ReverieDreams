package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.ItemHolderRenderState;
import cc.thonly.reverie_dreams.entity.misc.MagicBroom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public class MagicBroomRenderer extends EntityRenderer<MagicBroom, ItemHolderRenderState> {
    private final ItemModelResolver itemModelResolver;

    public MagicBroomRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void extractRenderState(MagicBroom entity, ItemHolderRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.itemStack = entity.getItemWrapper().getItemStack();
        renderState.yBodyRot = entity.yBodyRot;
        renderState.xRot = entity.getXRot();
        renderState.yRot = entity.getYRot();
        renderState.xRotO = entity.xRotO;
        renderState.yRotO = entity.yRotO;
        this.itemModelResolver.updateForNonLiving(renderState.itemRenderState, renderState.itemStack, ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void submit(ItemHolderRenderState renderState, PoseStack matrices, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        matrices.pushPose();
        matrices.translate(0, 0.25, 0);

//        matrices.mulPose(Axis.XP.rotationDegrees(45));
        matrices.mulPose(Axis.YP.rotationDegrees(-renderState.yBodyRot));
        matrices.mulPose(Axis.XP.rotationDegrees(90.0f));
        matrices.scale(1.2f, 1.2f, 1.2f);
        renderState.itemRenderState.submit(
                matrices,
                nodeCollector,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );
        matrices.popPose();
        super.submit(renderState, matrices, nodeCollector, cameraRenderState);
    }

    @Override
    protected boolean shouldShowName(MagicBroom entity, double distanceToCameraSq) {
        return false;
    }

    @Override
    public ItemHolderRenderState createRenderState() {
        return new ItemHolderRenderState();
    }
}
