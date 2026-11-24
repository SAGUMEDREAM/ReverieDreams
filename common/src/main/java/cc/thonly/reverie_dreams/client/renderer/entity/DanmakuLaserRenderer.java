package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.client.renderer.entity.state.DanmakuLaserRenderState;
import cc.thonly.reverie_dreams.entity.misc.DanmakuLaserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Native beam geometry: a narrow warning line, then a colored sheath with a bright core.
 */
public class DanmakuLaserRenderer extends EntityRenderer<DanmakuLaserEntity, DanmakuLaserRenderState> {
    private static final int DEFAULT_COLOR = 0x7DDFFF;
    private static final Identifier SHORT_CORE = ReverieDreams.id("textures/item/danmaku/short_laser.png");
    private static final Identifier SHORT_SHEATH = ReverieDreams.id("textures/item/danmaku/short_laser_overlay.png");

    public DanmakuLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(DanmakuLaserEntity entity, Frustum frustum, double x, double y, double z) {
        // Cull against the whole beam even when the emitter is outside the camera frustum.
        Vec3 ray = entity.beamDirection();
        Vec3 toCamera = new Vec3(x - entity.getX(), y - entity.getY(), z - entity.getZ());
        double along = Math.clamp(toCamera.dot(ray), 0.0, entity.getBeamLength());
        return entity.shouldRenderAtSqrDistance(toCamera.subtract(ray.scale(along)).lengthSqr())
                && frustum.isVisible(entity.beamVolume().bounds());
    }

    @Override
    public void extractRenderState(DanmakuLaserEntity entity, DanmakuLaserRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.pitch = entity.getPitch();
        state.yaw = entity.getYaw();
        state.beamLength = entity.getBeamLength();
        state.beamWidth = entity.getBeamWidth();
        state.active = entity.getState() == DanmakuLaserEntity.STATE_ACTIVE;
        state.traveling = entity.hasMovingBeamArt();
        var dye = entity.getDisplayItem().get(DataComponents.DYED_COLOR);
        state.color = dye == null ? DEFAULT_COLOR : dye.rgb();
    }

    @Override
    public void submit(DanmakuLaserRenderState state,
                       PoseStack matrices,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState
    ) {
        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        matrices.mulPose(Axis.XP.rotationDegrees(state.pitch));
        if (state.traveling) {
            submitTraveling(matrices, nodeCollector, state.beamWidth * 0.5f, state.beamLength, state.color);
            matrices.popPose();
            return;
        }
        float radius = state.beamWidth * (state.active ? 0.5f : 0.11f);
        float length = state.beamLength;
        int color = state.color;
        boolean active = state.active;
        nodeCollector.submitCustomGeometry(matrices, RenderTypes.lightning(), (pose, vertices) -> {
            prism(pose, vertices, radius, length, color, active ? 170 : 155);
            if (active) {
                prism(pose, vertices, radius * 0.37f, length, 0xFFFFFF, 255);
            }
        });
        matrices.popPose();
    }

    /**
     * Two intersecting strips give the sprite its original taper from any side without billboarding its heading.
     */
    private static void submitTraveling(PoseStack matrices, SubmitNodeCollector collector, float radius,
                                        float length, int color) {
        collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(SHORT_SHEATH), (pose, vertices) ->
                travelingStrips(pose, vertices, radius, length, color));
        collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(SHORT_CORE), (pose, vertices) ->
                travelingStrips(pose, vertices, radius, length, 0xFFFFFF));
    }

    private static void travelingStrips(PoseStack.Pose pose, VertexConsumer vertices, float r, float length, int color) {
        // The active band of the 32px item texture is rows 12 through 19.
        for (int face : new int[]{1, -1}) {
            float v0 = face > 0 ? 12f / 32f : 20f / 32f;
            float v1 = face > 0 ? 20f / 32f : 12f / 32f;
            float lo = -face * r;
            float hi = face * r;
            travelingVertex(vertices, pose, lo, 0, 0, 0, v0, color, 0, face);
            travelingVertex(vertices, pose, hi, 0, 0, 0, v1, color, 0, face);
            travelingVertex(vertices, pose, hi, 0, length, 1, v1, color, 0, face);
            travelingVertex(vertices, pose, lo, 0, length, 1, v0, color, 0, face);
            travelingVertex(vertices, pose, 0, lo, 0, 0, v0, color, face, 0);
            travelingVertex(vertices, pose, 0, hi, 0, 0, v1, color, face, 0);
            travelingVertex(vertices, pose, 0, hi, length, 1, v1, color, face, 0);
            travelingVertex(vertices, pose, 0, lo, length, 1, v0, color, face, 0);
        }
    }

    private static void travelingVertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float z,
                                        float u, float v, int rgb, float nx, float ny) {
        vertices.addVertex(pose, x, y, z).setColor(rgb | 0xFF000000).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, nx, ny, 0);
    }

    private static void prism(PoseStack.Pose pose, VertexConsumer vertices, float r, float length, int color, int alpha) {
        for (int side = 0; side < 4; side++) {
            float x = side == 0 || side == 3 ? -r : r;
            float y = side < 2 ? -r : r;
            int next = (side + 1) & 3;
            float nx = next == 0 || next == 3 ? -r : r;
            float ny = next < 2 ? -r : r;
            vertex(pose, vertices, x, y, 0, color, alpha);
            vertex(pose, vertices, nx, ny, 0, color, alpha);
            vertex(pose, vertices, nx, ny, length, color, alpha);
            vertex(pose, vertices, x, y, length, color, alpha);
        }
        vertex(pose, vertices, -r, r, 0, color, alpha);
        vertex(pose, vertices, r, r, 0, color, alpha);
        vertex(pose, vertices, r, -r, 0, color, alpha);
        vertex(pose, vertices, -r, -r, 0, color, alpha);
        vertex(pose, vertices, -r, -r, length, color, alpha);
        vertex(pose, vertices, r, -r, length, color, alpha);
        vertex(pose, vertices, r, r, length, color, alpha);
        vertex(pose, vertices, -r, r, length, color, alpha);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float z, int color, int alpha) {
        vertices.addVertex(pose, x, y, z).setColor(color | (alpha << 24));
    }

    @Override
    public DanmakuLaserRenderState createRenderState() {
        return new DanmakuLaserRenderState();
    }
}
