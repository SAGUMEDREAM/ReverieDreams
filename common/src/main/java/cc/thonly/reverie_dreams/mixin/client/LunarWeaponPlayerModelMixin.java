package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class LunarWeaponPlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void reverieDreams$alignGunWithView(AvatarRenderState state, CallbackInfo ci) {
        boolean right = state.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD
                && state.rightHandItemStack.getItem() instanceof WeaponOfTheMoon;
        boolean left = state.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD
                && state.leftHandItemStack.getItem() instanceof WeaponOfTheMoon;
        if ((!right && !left) || state.swimAmount > 0 || state.isFallFlying)
            return;

        PlayerModel model = (PlayerModel) (Object) this;
        var firingArm = right ? model.rightArm : model.leftArm;
        var supportArm = right ? model.leftArm : model.rightArm;
        // Follow view pitch without the crouch offset that would dip the barrel.
        firingArm.xRot = model.head.xRot - Mth.HALF_PI;
        firingArm.yRot = model.head.yRot + (right ? -0.16f : 0.16f);
        firingArm.zRot = 0;
        supportArm.xRot = firingArm.xRot + 0.05f;
        supportArm.zRot = 0;
    }
}
