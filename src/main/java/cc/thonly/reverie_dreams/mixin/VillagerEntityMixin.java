package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.item.FumoLicenseItem;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    @Inject(method = "interactMob", at= @At("HEAD"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        World world = player.getWorld();
        ItemStack itemStack = player.getStackInHand(hand);
        if (!world.isClient() && itemStack.getItem() instanceof FumoLicenseItem) {
            cir.setReturnValue(ActionResult.PASS);
        }
        if (!world.isClient() && itemStack.getItem() == Items.BARREL) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
