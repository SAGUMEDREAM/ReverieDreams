package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.client.renderer.entity.state.GapPortalRenderState;
import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GapPortalRenderer extends EntityRenderer<GapPortalEntity, GapPortalRenderState> {
    private static final Identifier TEXTURE = ReverieDreams.id("textures/entity/gap_portal.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public GapPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public GapPortalRenderState createRenderState() {
        return new GapPortalRenderState();
    }

    @Override
    public void extractRenderState(GapPortalEntity entity, GapPortalRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.age = entity.tickCount + partialTick;
        state.lifetime = entity.getLifetime();
        float lifetime = Math.max(1.0F, state.lifetime);
        float fadeIn = Math.min(1.0F, state.age / 10.0F);
        float fadeOut = Math.min(1.0F, (lifetime - state.age) / 20.0F);
        state.open = Math.max(0.0F, Math.min(fadeIn, fadeOut));
    }

    @Override
    public void submit(GapPortalRenderState state, PoseStack matrices, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (state.open <= 0.01F) {
            return;
        }

        matrices.pushPose();
        matrices.translate(0.0F, 1.05F, 0.0F);
        if (this.entityRenderDispatcher.camera != null) {
            matrices.mulPose(this.entityRenderDispatcher.camera.rotation());
        }
        matrices.mulPose(Axis.ZP.rotationDegrees(state.age * 1.7F));

        float pulse = 1.0F + Mth.sin(state.age * 0.25F) * 0.06F;
        matrices.scale(1.55F * state.open * pulse, 2.35F * state.open * pulse, 1.0F);
        int alpha = (int) (210.0F * state.open);
        this.submitPortalQuad(matrices, collector, 255, 255, 255, alpha);

        matrices.pushPose();
        matrices.mulPose(Axis.ZP.rotationDegrees(-state.age * 2.3F));
        matrices.scale(0.72F, 0.86F, 1.0F);
        this.submitPortalQuad(matrices, collector, 255, 178, 234, (int) (170.0F * state.open));
        matrices.popPose();

        matrices.popPose();
    }

    private void submitPortalQuad(PoseStack matrices, SubmitNodeCollector collector,
                                  int red, int green, int blue, int alpha) {
        // Use the regular entity-translucent pipeline instead of the emissive one:
        // the emissive variant disables depth writes, so water/clouds rendered
        // afterwards could incorrectly show through the rift.
        collector.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(TEXTURE, false), (pose, vertices) -> {
            // Draw both faces; the rift is a flat billboard and must stay visible
            // regardless of which side the camera ends up on.
            this.vertex(vertices, pose, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F, red, green, blue, alpha);
            this.vertex(vertices, pose, 0.5F, -0.5F, 0.0F, 1.0F, 1.0F, red, green, blue, alpha);
            this.vertex(vertices, pose, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F, red, green, blue, alpha);
            this.vertex(vertices, pose, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F, red, green, blue, alpha);

            this.vertex(vertices, pose, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F, red, green, blue, alpha);
            this.vertex(vertices, pose, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F, red, green, blue, alpha);
            this.vertex(vertices, pose, 0.5F, -0.5F, 0.0F, 1.0F, 1.0F, red, green, blue, alpha);
            this.vertex(vertices, pose, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F, red, green, blue, alpha);
        });
    }

    private void vertex(VertexConsumer vertices, PoseStack.Pose pose,
                        float x, float y, float z, float u, float v,
                        int red, int green, int blue, int alpha) {
        vertices.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}