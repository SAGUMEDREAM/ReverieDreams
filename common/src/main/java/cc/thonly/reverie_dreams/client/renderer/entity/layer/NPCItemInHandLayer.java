package cc.thonly.reverie_dreams.client.renderer.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;

/**
 * Vanilla held-item transforms for NPCs, which reuse AvatarRenderState without a
 * player animation manager. Keep this independent of ItemInHandLayer: player
 * animation mixins (notably Bendable Cuboids) may assume that manager is present.
 */
public class NPCItemInHandLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public NPCItemInHandLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       AvatarRenderState state, float yRot, float xRot) {
        this.submitArmWithItem(state, state.rightHandItemState, state.rightHandItemStack,
                HumanoidArm.RIGHT, poseStack, collector, lightCoords);
        this.submitArmWithItem(state, state.leftHandItemState, state.leftHandItemStack,
                HumanoidArm.LEFT, poseStack, collector, lightCoords);
    }

    private void submitArmWithItem(AvatarRenderState state, ItemStackRenderState item, ItemStack itemStack,
                                   HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
        if (item.isEmpty()) {
            return;
        }
        InteractionHand hand = arm == state.mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (state.isUsingItem && state.useItemHand == hand && state.attackTime < 1.0E-5F && !state.heldOnHead.isEmpty()) {
            this.submitItemHeldToEye(state, arm, poseStack, collector, lightCoords);
            return;
        }

        poseStack.pushPose();
        this.getParentModel().translateToHand(state, arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean babyOffset = state.isBaby && state.entityType != EntityType.ARMOR_STAND;
        float offsetX = babyOffset ? 0.0F : 1.0F;
        float offsetY = babyOffset ? 1.0F : 2.0F;
        float offsetZ = babyOffset ? -4.5F : -10.0F;
        poseStack.translate((arm == HumanoidArm.LEFT ? -1 : 1) * offsetX / 16.0F, offsetY / 16.0F, offsetZ / 16.0F);
        if (state.attackTime > 0.0F && state.attackArm == arm && state.swingAnimationType == SwingAnimationType.STAB) {
            SpearAnimations.thirdPersonAttackItem(state, poseStack);
        }
        float ticksUsingItem = state.ticksUsingItem(arm);
        if (ticksUsingItem != 0.0F) {
            (arm == HumanoidArm.RIGHT ? state.rightArmPose : state.leftArmPose)
                    .animateUseItem(state, poseStack, ticksUsingItem, arm, itemStack);
        }
        item.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private void submitItemHeldToEye(AvatarRenderState state, HumanoidArm arm, PoseStack poseStack,
                                     SubmitNodeCollector collector, int lightCoords) {
        poseStack.pushPose();
        this.getParentModel().root().translateAndRotate(poseStack);
        ModelPart head = this.getParentModel().getHead();
        float previousXRot = head.xRot;
        head.xRot = Mth.clamp(head.xRot, (float) (-Math.PI / 6), (float) (Math.PI / 2));
        head.translateAndRotate(poseStack);
        head.xRot = previousXRot;
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
        poseStack.translate((arm == HumanoidArm.LEFT ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        state.heldOnHead.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
