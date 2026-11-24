package cc.thonly.reverie_dreams.client.renderer.blockentity;

import cc.thonly.reverie_dreams.block.entity.FumoBlockEntity;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.client.audio.fumo.FumoAudioClient;
import cc.thonly.reverie_dreams.client.audio.fumo.FumoBeatDetector;
import cc.thonly.reverie_dreams.client.renderer.blockentity.state.FumoBlockEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FumoBlockEntityRenderer implements BlockEntityRenderer<FumoBlockEntity, FumoBlockEntityRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FumoBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FumoBlockEntityRenderState createRenderState() {
        return new FumoBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(FumoBlockEntity blockEntity, FumoBlockEntityRenderState state, float partialTick,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.yaw = blockEntity.getYaw();
        float base = blockEntity.getGrowthScale();
        float pulse = blockEntity.getBlockState().getBlock() instanceof BaseFumoBlock block && block.supportsGrowth()
                ? FumoAudioClient.pulse() : 0;
        state.scaleXZ = FumoBeatDetector.horizontalScale(base, pulse);
        state.scaleY = FumoBeatDetector.verticalScale(base, pulse);
        this.itemModelResolver.updateForTopItem(state.item, blockEntity.getBlockState().getBlock().asItem().getDefaultInstance(),
                ItemDisplayContext.NONE, blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
    }

    @Override
    public void submit(FumoBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poseStack.scale(state.scaleXZ, state.scaleY, state.scaleXZ);
        poseStack.translate(0, 0.5, 0);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
