package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.misc.DreamCocoonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DreamCocoonRenderer extends EntityRenderer<DreamCocoonEntity, EntityRenderState> {
    private static final Identifier SILK = ReverieDreams.id("textures/entity/dream_cocoon.png");
    private static final float[] HEIGHTS = {0, 0.19F, 0.81F, 1};
    private static final float[] RADII = {0.31F, 0.5F, 0.5F, 0.31F};

    public DreamCocoonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.scale(state.boundingBoxWidth, state.boundingBoxHeight, state.boundingBoxWidth);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(SILK), (matrix, vertices) -> {
            for (int side = 0; side < 4; side++) {
                float leftX = side == 0 || side == 3 ? -1 : 1;
                float leftZ = side < 2 ? -1 : 1;
                float rightX = side < 2 ? 1 : -1;
                float rightZ = side == 0 || side == 3 ? -1 : 1;
                float nx = (leftX + rightX) / 2;
                float nz = (leftZ + rightZ) / 2;
                for (int i = 0; i < HEIGHTS.length - 1; i++) {
                    float low = HEIGHTS[i];
                    float high = HEIGHTS[i + 1];
                    float inner = RADII[i];
                    float outer = RADII[i + 1];
                    vertex(vertices, matrix, rightX * inner, low, rightZ * inner, 1, 1 - low, nx, 0, nz, state.lightCoords);
                    vertex(vertices, matrix, leftX * inner, low, leftZ * inner, 0, 1 - low, nx, 0, nz, state.lightCoords);
                    vertex(vertices, matrix, leftX * outer, high, leftZ * outer, 0, 1 - high, nx, 0, nz, state.lightCoords);
                    vertex(vertices, matrix, rightX * outer, high, rightZ * outer, 1, 1 - high, nx, 0, nz, state.lightCoords);
                }
            }
            for (int end = 0; end <= 1; end++) {
                float r = RADII[0];
                float normal = 2 * end - 1;
                vertex(vertices, matrix, -r, end, -r, 0.2F, 0.38F, 0, normal, 0, state.lightCoords);
                vertex(vertices, matrix, r, end, -r, 0.8F, 0.38F, 0, normal, 0, state.lightCoords);
                vertex(vertices, matrix, r, end, r, 0.8F, 0.62F, 0, normal, 0, state.lightCoords);
                vertex(vertices, matrix, -r, end, r, 0.2F, 0.62F, 0, normal, 0, state.lightCoords);
            }
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz, int light) {
        vertices.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
