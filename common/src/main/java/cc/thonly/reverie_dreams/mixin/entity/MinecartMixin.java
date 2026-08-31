package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.api.entity.CartSignal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {Minecart.class, MinecartChest.class, MinecartFurnace.class})
public class MinecartMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void onUseNameTag(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = player.level();
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            Component custom = itemStack.getCustomName();
            if ((Object) this instanceof CartSignal cartSignal && itemStack.getItem() instanceof NameTagItem && custom != null) {
                cartSignal.reverie_dreams$setSignName(custom.getString());
                player.swing(hand);
                cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
                cir.cancel();
            }
        }
    }
}
