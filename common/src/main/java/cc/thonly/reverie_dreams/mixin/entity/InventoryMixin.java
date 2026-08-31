package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$add$appendItemData(int slot, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        ItemUtils.updateItemStackTag(itemStack);
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$setItem$appendItemData(int slot, ItemStack itemStack, CallbackInfo ci) {
        ItemUtils.updateItemStackTag(itemStack);
    }

    @Inject(method = "addAndPickItem", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$addAndPickItem$appendItemData(ItemStack itemStack, CallbackInfo ci) {
        ItemUtils.updateItemStackTag(itemStack);
    }

    @Inject(method = "addResource(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$addResource$appendItemData(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        ItemUtils.updateItemStackTag(itemStack);
    }

    @Inject(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$addResource$appendItemData(int slot, ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        ItemUtils.updateItemStackTag(itemStack);
    }
}
