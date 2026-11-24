package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.scope.LunarFirstPersonRenderer;
import cc.thonly.reverie_dreams.client.scope.LunarScopeClient;
import cc.thonly.reverie_dreams.client.scope.LunarWeaponPoseClient;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public abstract class LunarScopeHandMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow @Final private ItemModelResolver itemModelResolver;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void reverieDreams$renderGun(AbstractClientPlayer player, float partialTick, float pitch,
                                             InteractionHand hand, float swingProgress, ItemStack stack,
                                             float equipProgress, PoseStack poseStack, SubmitNodeCollector collector,
                                             int light, CallbackInfo ci) {
        if (LunarScopeClient.hidesHeldGun(hand, stack)) {
            ci.cancel();
            return;
        }
        if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND
                && offHandItem.getItem() instanceof WeaponOfTheMoon
                && player.getOffhandItem().getItem() instanceof WeaponOfTheMoon) {
            ci.cancel();
            return;
        }
        if (!(stack.getItem() instanceof WeaponOfTheMoon weapon) || player.isScoping())
            return;

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean mainHandGun = hand == InteractionHand.MAIN_HAND;
        boolean freeHand = mainHandGun ? offHandItem.isEmpty() && player.getOffhandItem().isEmpty()
                : mainHandItem.isEmpty() && player.getMainHandItem().isEmpty();
        LunarFirstPersonRenderer.render((ItemInHandRenderer) (Object) this, this.itemModelResolver, this.entityRenderDispatcher,
                player, stack, weapon.getWeaponType(), arm, freeHand, equipProgress,
                LunarWeaponPoseClient.shotKick(player, hand, partialTick),
                LunarWeaponPoseClient.reloadProgress(player, hand, partialTick), poseStack, collector, light);
        ci.cancel();
    }

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void reverieDreams$keepGunRaisedOnUse(InteractionHand hand, CallbackInfo ci) {
        ItemStack visible = hand == InteractionHand.MAIN_HAND ? mainHandItem : offHandItem;
        if (visible.getItem() instanceof WeaponOfTheMoon && minecraft.player != null
                && ItemStack.isSameItem(visible, minecraft.player.getItemInHand(hand))) {
            // Gun recoil and reload have their own motion; vanilla lowers the entire held item on use.
            ci.cancel();
        }
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void reverieDreams$keepGunRaisedOnAmmoSync(ItemStack before, ItemStack after, CallbackInfoReturnable<Boolean> cir) {
        if (before.getItem() instanceof WeaponOfTheMoon && ItemStack.matchesIgnoringComponents(before, after,
                component -> component == RDDataComponentTypes.MAGAZINE_ROUNDS.get()
                        || component == RDDataComponentTypes.LUNAR_TRIGGER_HELD.get())) {
            cir.setReturnValue(true);
        }
    }
}
