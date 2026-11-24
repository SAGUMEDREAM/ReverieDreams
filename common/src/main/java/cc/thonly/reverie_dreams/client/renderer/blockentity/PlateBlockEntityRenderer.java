package cc.thonly.reverie_dreams.client.renderer.blockentity;

import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.api.registry.block.CuisineBlockBindings;
import cc.thonly.reverie_dreams.block.kitchen.CuisineBlock;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import cc.thonly.reverie_dreams.client.renderer.blockentity.state.PlateBlockEntityRenderState;
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

public class PlateBlockEntityRenderer implements BlockEntityRenderer<PlateBlockEntity, PlateBlockEntityRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final BlockModelResolver blockModelResolver;
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    public PlateBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
        this.blockModelResolver = ctx.blockModelResolver();
    }

    @Override
    public PlateBlockEntityRenderState createRenderState() {
        return new PlateBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(PlateBlockEntity be, PlateBlockEntityRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPosition, breakProgress);
        state.ingredientStack = be.getItem();
        state.yaw = be.getYaw();
        var cuisine = CuisineBlockBindings.find(be.getItem().typeHolder());
        state.hasCuisineModel = !be.getItem().isEmpty() && cuisine.isPresent();
        state.cuisineRenderState.clear();
        if (state.hasCuisineModel) {
            this.blockModelResolver.update(state.cuisineRenderState, cuisine.orElseThrow().value().defaultBlockState(), DISPLAY_CONTEXT);
        }
        if (be.getLevel() != null) {
            state.bobOffset = (float) Math.sin((be.getLevel().getGameTime() + partialTick) * 0.1f) * 0.05f;
        }
        if (!be.getItem().isEmpty() && !state.hasCuisineModel) {
            this.itemModelResolver.updateForTopItem(state.itemRenderState, state.ingredientStack.build(), ItemDisplayContext.GROUND, be.getLevel(), null, (int) (be.getBlockPos().asLong()));
        }
    }

    @Override
    public void submit(PlateBlockEntityRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState
    ) {
        if (state.ingredientStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (state.hasCuisineModel) {
            poseStack.translate(0.5, CuisineBlock.PLATE_SURFACE, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-CuisineBlock.displayYaw(state.yaw)));
            poseStack.translate(-0.5, 0, -0.5);
            state.cuisineRenderState.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
            return;
        }
        poseStack.translate(0.5, 0.2, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poseStack.scale(1f, 1f, 1f);
        state.itemRenderState.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
