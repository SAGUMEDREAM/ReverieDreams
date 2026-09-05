package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.entity.NPCFishingHook;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;

public class NPCFishingHookRenderer extends EntityRenderer<NPCFishingHook, FishingHookRenderState> {
    public static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/fishing_hook.png");
    public static final RenderType RENDER_TYPE;
    public static final double VIEW_BOBBING_SCALE = (double) 960.0F;

    public NPCFishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public boolean shouldRender(NPCFishingHook entity, Frustum culler, double camX, double camY, double camZ) {
        return super.shouldRender(entity, culler, camX, camY, camZ) && entity.getEntityOwner() != null;
    }

    public void submit(FishingHookRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(cameraRenderState.orientation);
        nodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (p_434943_, p_432878_) -> {
            vertex(p_432878_, p_434943_, state.lightCoords, 0.0F, 0, 0, 1);
            vertex(p_432878_, p_434943_, state.lightCoords, 1.0F, 0, 1, 1);
            vertex(p_432878_, p_434943_, state.lightCoords, 1.0F, 1, 1, 0);
            vertex(p_432878_, p_434943_, state.lightCoords, 0.0F, 1, 0, 0);
        });
        poseStack.popPose();
        float f = (float)state.lineOriginOffset.x;
        float f1 = (float)state.lineOriginOffset.y;
        float f2 = (float)state.lineOriginOffset.z;
        float f3 = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (p_454362_, p_454363_) -> {
            int i = 16;

            for (int j = 0; j < 16; j++) {
                float f4 = fraction(j, 16);
                float f5 = fraction(j + 1, 16);
                stringVertex(f, f1, f2, p_454363_, p_454362_, f4, f5, f3);
                stringVertex(f, f1, f2, p_454363_, p_454362_, f5, f4, f3);
            }
        });
        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
    }

    public static HumanoidArm getHoldingArm(BaseNPCLikeEntity owner) {
        return owner.getMainHandItem().getItem() instanceof FishingRodItem ? owner.getMainArm() : owner.getMainArm().getOpposite();
    }

    private Vec3 getHandPos(BaseNPCLikeEntity npc, float handAngle, float partialTick) {
        int i = getHoldingArm(npc) == HumanoidArm.RIGHT ? 1 : -1;
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) {
            double d4 = VIEW_BOBBING_SCALE / this.entityRenderDispatcher.options.fov().get();
            Vec3 vec3 = null;
            if (this.entityRenderDispatcher
                    .camera != null) {
                vec3 = this.entityRenderDispatcher
                        .camera
                        .getNearPlane()
                        .getPointOnPlane(i * 0.525F, -0.1F)
                        .scale(d4)
                        .yRot(handAngle * 0.5F)
                        .xRot(-handAngle * 0.7F);
                return npc.getEyePosition(partialTick).add(vec3);
            }
        } else {
            float f = Mth.lerp(partialTick, npc.yBodyRotO, npc.yBodyRot) * (float) (Math.PI / 180.0);
            double d0 = Mth.sin(f);
            double d1 = Mth.cos(f);
            float f1 = npc.getScale();
            double d2 = i * 0.35 * f1;
            double d3 = 0.8 * f1;
            float f2 = npc.isCrouching() ? -0.1875F : 0.0F;
            return npc.getEyePosition(partialTick).add(-d1 * d2 - d0 * d3, f2 - 0.45 * f1, -d0 * d2 + d1 * d3);
        }
        return Vec3.ZERO;
    }

    private static float fraction(int i, int steps) {
        return (float) i / (float) steps;
    }

    private static void vertex(VertexConsumer builder, PoseStack.Pose pose, int lightCoords, float x, int y, int u, int v) {
        builder.addVertex(pose, x - 0.5F, (float) y - 0.5F, 0.0F).setColor(-1).setUv((float) u, (float) v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(float xa, float ya, float za, VertexConsumer stringBuffer, PoseStack.Pose stringPose, float aa, float nexta, float width) {
        float x = xa * aa;
        float y = ya * (aa * aa + aa) * 0.5F + 0.25F;
        float z = za * aa;
        float nx = xa * nexta - x;
        float ny = ya * (nexta * nexta + nexta) * 0.5F + 0.25F - y;
        float nz = za * nexta - z;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= length;
        ny /= length;
        nz /= length;
        stringBuffer.addVertex(stringPose, x, y, z).setColor(-16777216).setNormal(stringPose, nx, ny, nz).setLineWidth(width);
    }

    public FishingHookRenderState createRenderState() {
        return new FishingHookRenderState();
    }

    public void extractRenderState(NPCFishingHook entity, FishingHookRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        BaseNPCLikeEntity owner = entity.getEntityOwner();
        if (owner == null) {
            state.lineOriginOffset = Vec3.ZERO;
        } else {
            float swing = owner.getAttackAnim(partialTicks);
            float swing2 = Mth.sin((double) (Mth.sqrt(swing) * (float) Math.PI));
            Vec3 playerPos = this.getHandPos(owner, swing2, partialTicks);
            Vec3 hookPos = entity.getPosition(partialTicks).add((double) 0.0F, (double) 0.25F, (double) 0.0F);
            state.lineOriginOffset = playerPos.subtract(hookPos);
        }

    }

    @Override
    protected boolean affectedByCulling(NPCFishingHook display) {
        return false;
    }

    static {
        RENDER_TYPE = RenderTypes.entityCutout(TEXTURE_LOCATION);
    }
}
