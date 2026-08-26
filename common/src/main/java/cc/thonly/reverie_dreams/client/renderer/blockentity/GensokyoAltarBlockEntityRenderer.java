package cc.thonly.reverie_dreams.client.renderer.blockentity;

import cc.thonly.reverie_dreams.block.props.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.client.renderer.blockentity.state.GensokyoAltarBlockEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class GensokyoAltarBlockEntityRenderer implements BlockEntityRenderer<GensokyoAltarBlockEntity, GensokyoAltarBlockEntityRenderState> {

    private final ItemModelResolver itemModelResolver;

    public GensokyoAltarBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public GensokyoAltarBlockEntityRenderState createRenderState() {
        return new GensokyoAltarBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(GensokyoAltarBlockEntity blockEntity,
                                   GensokyoAltarBlockEntityRenderState state,
                                   float partialTick,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {

        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        if (blockEntity.getLevel() == null) {
            return;
        }
        SimpleContainer inventory = blockEntity.getInventory();

        // ✅ 时间（用于动画）
        state.gameTime = blockEntity.getLevel().getGameTime();
        state.partialTick = partialTick;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);

            // 空物品处理
            if (item.isEmpty()) {
                state.itemStates[i] = null;
                state.ingredients[i] = ItemStack.EMPTY;
                continue;
            }

            state.ingredients[i] = item.copy();

            // 初始化 RenderState（只 new 一次）
            if (state.itemStates[i] == null) {
                state.itemStates[i] = new ItemStackRenderState();
            }

            this.itemModelResolver.updateForTopItem(
                    state.itemStates[i],
                    item,
                    ItemDisplayContext.GROUND,
                    blockEntity.getLevel(),
                    null,
                    (int) blockEntity.getBlockPos().asLong()
            );
        }
    }

    @Override
    public void submit(GensokyoAltarBlockEntityRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {

        int[][] offsets = GensokyoAltarBlock.OFFSETS;

        for (int i = 0; i < offsets.length; i++) {
            ItemStackRenderState renderState = state.itemStates[i];
            if (renderState == null) continue;

            int[] offset = offsets[i];

            Vec3 pos = new Vec3(offset[0], 0, offset[1]);

            boolean isCore = (i == 8);

            this.renderItem(renderState, poseStack, nodeCollector, state, pos, isCore, i);
        }
    }

    private void renderItem(ItemStackRenderState renderState,
                            PoseStack poseStack,
                            SubmitNodeCollector nodeCollector,
                            GensokyoAltarBlockEntityRenderState state,
                            Vec3 offset,
                            boolean isCore,
                            int index) {

        poseStack.pushPose();

        float time = (state.gameTime + state.partialTick);
        double floatY = Math.sin((time + index * 5) * 0.1) * 0.05;

        poseStack.translate(
                offset.x + 0.5,
                offset.y + (isCore ? 1 - 0.2 : 3) + 0.2 + floatY,
                offset.z + 0.5
        );

        float angle = (time * (isCore ? 2f : 4f) + index * 20) % 360;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        float scale = 1f;
        poseStack.scale(scale, scale, scale);

        renderState.submit(
                poseStack,
                nodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(GensokyoAltarBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

}