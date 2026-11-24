package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.scope.LunarWeaponPoseClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class LunarWeaponAvatarMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL"))
    private void reverieDreams$holdGun(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        InteractionHand hand = LunarWeaponPoseClient.getPoseHand(avatar);
        if (hand == null)
            return;

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
        state.rightArmPose = arm == HumanoidArm.RIGHT ? HumanoidModel.ArmPose.CROSSBOW_HOLD : HumanoidModel.ArmPose.EMPTY;
        state.leftArmPose = arm == HumanoidArm.LEFT ? HumanoidModel.ArmPose.CROSSBOW_HOLD : HumanoidModel.ArmPose.EMPTY;
        state.isUsingItem = true;
        state.useItemHand = hand;
        state.attackTime = 0;
    }
}
