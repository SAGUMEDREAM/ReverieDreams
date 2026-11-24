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
    /**
     * 容器/玩家背包读档时清理旧版本自动写入的冗余属性组件。
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    public void reverie_dreams$normalizeStack(int slot, ItemStack stack, CallbackInfo ci) {
        ItemUtils.normalizeItemStackTag(stack);
    }
}
