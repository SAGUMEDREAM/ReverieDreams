package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NonNullList.class)
public class NonNullListMixin<E> {
    @Inject(method = {"set"}, at=@At("HEAD"))
    public void reverie_dreams$modifyListSet(int index, E element, CallbackInfoReturnable<E> cir) {
        if (element instanceof ItemStack itemStack) {
            ItemUtils.updateItemStackTag(itemStack);
        }
    }
    @Inject(method = {"add"}, at=@At("HEAD"))
    public void reverie_dreams$modifyListAdd(int index, E element, CallbackInfo ci) {
        if (element instanceof ItemStack itemStack) {
            ItemUtils.updateItemStackTag(itemStack);
        }
    }
}
