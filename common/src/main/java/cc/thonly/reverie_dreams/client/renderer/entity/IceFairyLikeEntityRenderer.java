package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class IceFairyLikeEntityRenderer<NPCEntity extends BaseNPCLikeEntity> extends BaseNPCLikeEntityRenderer<NPCEntity> {
    private static final LazySupplier<ItemStack> WING_HOLDER = LazySupplier.of(RDEntityHolderItems.ICE_FAIRY_WINGS::createStack);
    private final ItemModelResolver itemModelResolver;

    public IceFairyLikeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, true);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void extractRenderState(NPCEntity entity, AvatarRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.bodyRot = entity.yBodyRot;
        if (state instanceof NPCAvatarRenderState rs && entity.isAlive()) {
            this.itemModelResolver.updateForLiving(
                    rs.wingHolderRenderState,
                    WING_HOLDER.get(),
                    ItemDisplayContext.GUI, entity
            );
        }
    }

    @Override
    public void submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(1.2f, 1.2f, 1.2f);
        poseStack.translate(0, 0.75, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.bodyRot + 180));
        poseStack.translate(0, 0, 0.2f);
        if (state instanceof NPCAvatarRenderState rs) {
            rs.wingHolderRenderState.submit(
                    poseStack,
                    submitNodeCollector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
        }
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public boolean shouldRender(NPCEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
