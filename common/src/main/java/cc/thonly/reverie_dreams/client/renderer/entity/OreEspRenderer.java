package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.OreEsRenderState;
import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OreEspRenderer extends EntityRenderer<OreEspEntity, OreEsRenderState> {
    private static final BlockState STATE = Blocks.BARRIER.defaultBlockState();

    public OreEspRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(OreEspEntity entity, OreEsRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
    }

    @Override
    public void submit(OreEsRenderState renderState, PoseStack matrices, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(renderState, matrices, nodeCollector, cameraRenderState);
        matrices.pushPose();
        nodeCollector.submitBlock(matrices, STATE, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }

    @Override
    public OreEsRenderState createRenderState() {
        return new OreEsRenderState();
    }
}
