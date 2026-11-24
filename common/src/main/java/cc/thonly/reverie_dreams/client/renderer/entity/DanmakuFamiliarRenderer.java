package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.DanmakuFamiliarRenderState;
import cc.thonly.reverie_dreams.entity.misc.DanmakuFamiliarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

/**
 * 使魔渲染：渲染 familiar 持有的 ItemStack，绕 Y 轴缓慢自转并始终面向相机。
 */
public class DanmakuFamiliarRenderer extends EntityRenderer<DanmakuFamiliarEntity, DanmakuFamiliarRenderState> {
    private static final float SCALE = 0.5f;
    private final ItemModelResolver itemModelResolver;

    public DanmakuFamiliarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    protected int getBlockLightLevel(DanmakuFamiliarEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(DanmakuFamiliarEntity entity, DanmakuFamiliarRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.itemStack = entity.getDisplayItem();
        state.spin = entity.getSpinDegrees(partialTick);
        state.display = false;
        if (!state.itemStack.isEmpty()) {
            this.itemModelResolver.updateForNonLiving(state.itemRenderState, state.itemStack,
                    ItemDisplayContext.GROUND, entity);
            state.display = true;
        }
    }

    @Override
    public void submit(DanmakuFamiliarRenderState state,
                       PoseStack matrices,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState
    ) {
        if (!state.display || state.itemStack.isEmpty()) {
            return;
        }
        matrices.pushPose();
        matrices.scale(SCALE, SCALE, SCALE);
        // 先面向相机（tile 风格），再叠加缓慢自转
        Quaternionf billboard = this.entityRenderDispatcher.camera != null
                ? this.entityRenderDispatcher.camera.rotation()
                : new Quaternionf();
        matrices.mulPose(billboard);
        matrices.mulPose(Axis.YP.rotationDegrees(180.0f));
        matrices.mulPose(Axis.YP.rotationDegrees(state.spin));
        state.itemRenderState.submit(matrices, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }

    @Override
    public DanmakuFamiliarRenderState createRenderState() {
        return new DanmakuFamiliarRenderState();
    }
}
