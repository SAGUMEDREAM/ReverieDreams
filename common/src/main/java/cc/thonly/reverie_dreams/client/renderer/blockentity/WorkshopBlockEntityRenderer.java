package cc.thonly.reverie_dreams.client.renderer.blockentity;

import cc.thonly.reverie_dreams.workshop.WorkshopDisplays;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class WorkshopBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, WorkshopBlockEntityRenderer.State> {
    private final ItemModelResolver resolver;

    public WorkshopBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.itemModelResolver();
    }

    public static final class State extends BlockEntityRenderState {
        public List<WorkshopDisplays.Part> parts = List.of();
        public final List<ItemStackRenderState> models = new ArrayList<>();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T host, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(host, state, partialTick, camera, breaking);
        state.parts = WorkshopDisplays.parts(host, partialTick);
        while (state.models.size() < state.parts.size())
            state.models.add(new ItemStackRenderState());
        for (int i = 0; i < state.parts.size(); i++)
            resolver.updateForTopItem(state.models.get(i), state.parts.get(i).stack(), ItemDisplayContext.NONE, host.getLevel(), null, i);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.parts.size(); i++) {
            var part = state.parts.get(i);
            pose.pushPose();
            pose.translate(part.x(), part.y(), part.z());
            pose.mulPose(Axis.YP.rotationDegrees(-part.yaw()));
            pose.mulPose(Axis.XP.rotationDegrees(part.pitch()));
            pose.scale(part.scale(), part.scale(), part.scale());
            state.models.get(i).submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
