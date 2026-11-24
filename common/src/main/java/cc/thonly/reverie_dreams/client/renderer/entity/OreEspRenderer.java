package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.OreEsRenderState;
import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import cc.thonly.reverie_dreams.mixin.accessor.BlockDisplayAccessor;
import cc.thonly.reverie_dreams.util.OreDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 只渲染矿石方块的发光轮廓，方块本体不显示，因此轮廓可以穿透方块看到。
 */
public class OreEspRenderer extends EntityRenderer<OreEspEntity, OreEsRenderState> {
    protected final BlockModelResolver blockModelResolver;

    public OreEspRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public void extractRenderState(OreEspEntity entity, OreEsRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        BlockState state = ((BlockDisplayAccessor) entity).reverie_dreams$getBlockState();
        this.blockModelResolver.update(reusedState.blockModel, state, BlockDisplayContext.create());
        reusedState.markerColor = OreDetector.colorFor(state) | 0xFF000000;
    }

    @Override
    public void submit(OreEsRenderState renderState, PoseStack matrices, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(renderState, matrices, nodeCollector, cameraRenderState);
        // 只提交描边通道，矿石本体不渲染；描边会穿过墙体显示
        int outlineColor = renderState.outlineColor != 0 ? renderState.outlineColor : renderState.markerColor;
        matrices.pushPose();
        renderState.blockModel.submitOnlyOutline(matrices, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, outlineColor);
        matrices.popPose();
    }

    @Override
    public OreEsRenderState createRenderState() {
        return new OreEsRenderState();
    }
}
