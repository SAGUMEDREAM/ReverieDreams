package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.BaguaFurnaceRenderState;
import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.misc.spark.MasterSparkBeam;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BaguaFurnaceRenderer extends EntityRenderer<BaguaFurnaceEntity, BaguaFurnaceRenderState> {
    private final ItemModelResolver itemModelResolver;

    public BaguaFurnaceRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void extractRenderState(BaguaFurnaceEntity entity, BaguaFurnaceRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.xRot = entity.getRenderPitch(partialTick);
        state.yRot = entity.getRenderYaw(partialTick);
        state.bob = (float) Math.sin((entity.tickCount + partialTick) * 0.15F) * 0.06F;
        state.pulse = 1.0F + (float) Math.sin((entity.tickCount + partialTick) * 0.3F) * 0.04F;
        state.chargeProgress = entity.getChargeProgress();
        state.age = entity.tickCount + partialTick;
        state.beamLength = entity.getBeamLength();
        state.beamStrength = entity.getBeamStrength(partialTick);
        state.lookAngle = entity.getLookAngle();
        this.itemModelResolver.updateForNonLiving(state.itemRenderState, RDItems.BAGUA_FURNACE.createStack(), ItemDisplayContext.NONE, entity);
    }

    @Override
    public void submit(BaguaFurnaceRenderState state, PoseStack matrices, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, matrices, nodeCollector, cameraRenderState);
        matrices.pushPose();

        float yaw = state.yRot;
        float pitch = state.xRot;

        matrices.translate(0, MasterSparkBeam.MOUNT_HEIGHT, 0);
        matrices.mulPose(Axis.YP.rotationDegrees(-yaw));
        matrices.mulPose(Axis.XP.rotationDegrees(pitch + 90));
        float scale = 2 * state.pulse * (1 + state.chargeProgress * 0.08f);
        matrices.scale(scale, scale, scale);
        matrices.translate(0, 0.5, 0);

        state.itemRenderState.submit(
                matrices,
                nodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, MasterSparkBeam.MOUNT_HEIGHT, 0);
        matrices.mulPose(Axis.YP.rotationDegrees(-yaw));
        matrices.mulPose(Axis.XP.rotationDegrees(pitch));
        matrices.translate(0, 0, MasterSparkBeam.MUZZLE_OFFSET);
        MasterSparkRenderer.submit(matrices, nodeCollector, state.age, state.chargeProgress, state.beamStrength, state.beamLength);
        matrices.popPose();
    }

    @Override
    protected AABB getBoundingBoxForCulling(BaguaFurnaceEntity entity) {
        if (entity.isFiring()) {
            AABB bounds = entity.getBoundingBox();
            Vec3 mount = entity.position().add(0, MasterSparkBeam.MOUNT_HEIGHT, 0);
            // Include both interpolated poses so a sweeping beam stays visible
            // even when the latest server heading is already outside the camera.
            for (int endpoint = 0; endpoint <= 1; endpoint++) {
                Vec3 direction = Vec3.directionFromRotation(entity.getRenderPitch(endpoint), entity.getRenderYaw(endpoint));
                Vec3 muzzle = mount.add(direction.scale(MasterSparkBeam.MUZZLE_OFFSET));
                bounds = bounds.minmax(MasterSparkBeam.bounds(muzzle, direction, entity.getBeamLength()));
            }
            return bounds.inflate(0.25);
        }
        return super.getBoundingBoxForCulling(entity).inflate(1.5);
    }

    @Override
    public BaguaFurnaceRenderState createRenderState() {
        return new BaguaFurnaceRenderState();
    }
}
