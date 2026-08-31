package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackWithSlot.class)
public class ItemStackWithSlotMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void reverie_dreams$byInitStack(int slot, ItemStack stack, CallbackInfo ci) {
        ItemUtils.updateItemStackTag(stack);
    }
}
