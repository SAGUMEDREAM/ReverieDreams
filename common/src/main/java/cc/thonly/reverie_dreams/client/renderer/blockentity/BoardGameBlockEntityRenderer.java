package cc.thonly.reverie_dreams.client.renderer.blockentity;

import cc.thonly.reverie_dreams.board.BoardDisplays;
import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
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
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BoardGameBlockEntityRenderer implements BlockEntityRenderer<BoardGameBlockEntity, BoardGameBlockEntityRenderer.State> {
    private final ItemModelResolver resolver;

    public BoardGameBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.itemModelResolver();
    }

    public static final class State extends BlockEntityRenderState {
        public final List<ItemStackRenderState> models = new ArrayList<>();
        public List<BoardDisplays.Part> parts = List.of();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BoardGameBlockEntity host, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(host, state, partialTick, camera, breaking);
        state.parts = BoardDisplays.parts(host);
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
            pose.scale(part.scale(), part.scale(), part.scale());
            state.models.get(i).submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
