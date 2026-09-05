package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.DanmakuEntityRenderState;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

public class DanmakuLikeRenderer extends EntityRenderer<DanmakuEntity, DanmakuEntityRenderState> {
    protected static final float MIN_DISTANCE = 2;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemModelResolver itemModelResolver;

    public DanmakuLikeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    protected int getBlockLightLevel(DanmakuEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(DanmakuEntity entity, DanmakuEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.itemStack = entity.getItemStack();
        state.properties = entity.getDanmakuProperties();
        state.scale = state.properties.scale() * 0.85f;
        state.tile = state.properties.tile();
        state.xRot = entity.getXRot();
        state.yRot = entity.getYRot();
        state.xRotO = entity.xRotO;
        state.yRotO = entity.yRotO;
        state.partialTick = partialTick;
        if (this.entityRenderDispatcher.camera != null && this.entityRenderDispatcher.camera.entity().distanceToSqr(entity) < MIN_DISTANCE) {
            return;
        }
        if (!state.itemStack.isEmpty()) {
            this.itemModelResolver.updateForNonLiving(state.itemRenderState, state.itemStack, state.tile ? ItemDisplayContext.FIXED : ItemDisplayContext.GUI, entity);
            state.display = true;
        }
    }

    @Override
    public void submit(DanmakuEntityRenderState state,
                       PoseStack matrices,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState
    ) {
        if (!state.display) {
            return;
        }
        if (state.itemStack.isEmpty()) {
            return;
        }
        if (state.properties.tile()) {
            this.modifyPoseStack(state, matrices);
        } else {
            this.modifyPoseStackFlat(state, matrices);
        }
        state.itemRenderState.submit(matrices, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }

    public void modifyPoseStack(DanmakuEntityRenderState state, PoseStack matrices) {
        matrices.pushPose();
        matrices.scale(state.scale, state.scale, state.scale);
        matrices.translate(0.0f, 0.25f, 0.0f);
        if (this.entityRenderDispatcher.camera != null) {
            matrices.mulPose(this.entityRenderDispatcher.camera.rotation());
        }
        matrices.mulPose(Axis.YP.rotationDegrees(180.0f));
    }

    public void modifyPoseStackFlat(DanmakuEntityRenderState state, PoseStack matrices) {
        matrices.pushPose();
        matrices.scale(state.scale, state.scale, state.scale);
        matrices.mulPose(Axis.YP.rotationDegrees(Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 90.0f));
        matrices.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(state.partialTick, state.xRotO, state.xRot)));
        matrices.mulPose(Axis.XP.rotationDegrees(90.0f));
        matrices.mulPose(Axis.ZP.rotationDegrees(-90.0f));
    }

    @Override
    public DanmakuEntityRenderState createRenderState() {
        return new DanmakuEntityRenderState();
    }
}
