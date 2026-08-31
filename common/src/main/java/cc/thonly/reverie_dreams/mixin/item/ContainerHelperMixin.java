package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerHelper.class)
public class ContainerHelperMixin {
    @Inject(
            method= "saveAllItems(Lnet/minecraft/world/level/storage/ValueOutput;Lnet/minecraft/core/NonNullList;Z)V",
            at = @At("HEAD")
    )
    private static void reverie_dreams$modifySaveAllItems(ValueOutput output, NonNullList<ItemStack> itemStacks, boolean alsoWhenEmpty, CallbackInfo ci) {
        for (ItemStack itemStack : itemStacks) {
            ItemUtils.updateItemStackTag(itemStack);
        }
    }

    @Inject(
            method = "loadAllItems",
            at = @At("RETURN")
    )
    private static void reverie_dreams$modifyLoadAllItems(ValueInput input, NonNullList<ItemStack> itemStacks, CallbackInfo ci) {
        for (ItemStack itemStack : itemStacks) {
            ItemUtils.updateItemStackTag(itemStack);
        }
    }
}
