package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.BaguaFurnaceRendererState;
import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.client.RDMthTool;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class BaguaFurnaceRenderer extends EntityRenderer<BaguaFurnaceEntity, BaguaFurnaceRendererState> {
    private final ItemModelResolver itemModelResolver;

    public BaguaFurnaceRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void extractRenderState(BaguaFurnaceEntity entity, BaguaFurnaceRendererState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getInitXRot();
        state.yRot = entity.getInitYRot();
        state.xRot0 = entity.xRotO;
        state.yRot0 = entity.yRotO;
        state.partialTick = partialTick;
        state.lookAngle = entity.getLookAngle();
        this.itemModelResolver.updateForNonLiving(state.itemRenderState, RDItems.BAGUA_FURNACE.createStack(), ItemDisplayContext.GUI, entity);
    }

    @Override
    public void submit(BaguaFurnaceRendererState state, PoseStack matrices, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, matrices, nodeCollector, cameraRenderState);
        matrices.pushPose();

        matrices.scale(2f, 2f, 2f);
        matrices.translate(0.0f, -0.1f, 0.0f);
        matrices.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        matrices.mulPose(Axis.XP.rotationDegrees(state.xRot + 45));

        state.itemRenderState.submit(
                matrices,
                nodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
    }

    @Override
    public BaguaFurnaceRendererState createRenderState() {
        return new BaguaFurnaceRendererState();
    }
}
