package cc.thonly.reverie_dreams.client.renderer.blockentity;

import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.client.renderer.blockentity.state.KitchenBlockEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
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

public class KitchenBlockEntityRenderer implements BlockEntityRenderer<KitchenwareBlockEntity, KitchenBlockEntityRenderState> {
    private final ItemModelResolver itemModelResolver;

    public KitchenBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public void extractRenderState(KitchenwareBlockEntity blockEntity, KitchenBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.ingredientStack = blockEntity.getPreOutput();
        state.tickLeft = blockEntity.getTickLeft();
        if (state.ingredientStack.isEmpty()) {
            return;
        }
        this.itemModelResolver.updateForTopItem(state.itemRenderState,
                state.ingredientStack.build(),
                ItemDisplayContext.NONE,
                blockEntity.getLevel(),
                null,
                (int) (blockEntity.getBlockPos().asLong())
        );
    }

    @Override
    public void submit(KitchenBlockEntityRenderState renderState,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.8, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        renderState.itemRenderState.submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();

        poseStack.pushPose();
        Font font = Minecraft.getInstance().font;

        float seconds = (float) (renderState.tickLeft / 20.0f);
        if (seconds > 0) {
            String timeText = String.format("%.1fs", seconds);

            float x = -font.width(timeText) / 2f;
            float y = 0;

            if (Minecraft.getInstance()
                    .getEntityRenderDispatcher()
                    .camera != null) {
                poseStack.mulPose(Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .camera.rotation());
            }

            poseStack.scale(0.02f, -0.02f, 0.02f);

            MultiBufferSource.BufferSource buffer =
                    Minecraft.getInstance().renderBuffers().bufferSource();

            font.drawInBatch(
                    timeText,
                    x,
                    y,
                    0xFFFFFF,
                    false,
                    poseStack.last().pose(),
                    buffer,
                    Font.DisplayMode.NORMAL,
                    0,
                    renderState.lightCoords
            );
        }
        poseStack.popPose();
    }

    @Override
    public KitchenBlockEntityRenderState createRenderState() {
        return new KitchenBlockEntityRenderState();
    }
}
