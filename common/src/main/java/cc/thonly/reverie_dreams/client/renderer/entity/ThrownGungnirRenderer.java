package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.projectile.ThrownTouhouTrident;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownGungnirRenderer extends EntityRenderer<ThrownTouhouTrident, ThrownGungnirRenderer.State> {
    private static final float SPIN_DEGREES_PER_TICK = 22.5F;
    private final ItemModelResolver itemModelResolver;

    public ThrownGungnirRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ThrownTouhouTrident entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.inFlight = entity.isInFlight();

        ItemStack stack = RDItems.GUNGNIR.createStack();
        stack.set(DataComponents.ITEM_MODEL, ReverieDreams.id("gungnir_throwing"));
        this.itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.NONE, entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        // Align local +X with the flight direction and place the tip near the projectile origin.
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        poseStack.translate(-0.85F, 0.0F, 0.0F);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        if (state.inFlight) {
            poseStack.mulPose(Axis.XP.rotationDegrees(state.ageInTicks * SPIN_DEGREES_PER_TICK % 360.0F));
            submitBlade(state, poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            submitBlade(state, poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT);
        } else {
            submitBlade(state, poseStack, submitNodeCollector, state.lightCoords);
        }
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private void submitBlade(State state, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        // The texture runs from the lower right to the upper left. Turn its shaft onto +X.
        poseStack.mulPose(Axis.ZP.rotationDegrees(-135.0F));
        state.item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    public static class State extends ThrownTridentRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean inFlight;
    }
}
