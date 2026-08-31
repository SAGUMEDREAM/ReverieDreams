package cc.thonly.reverie_dreams.mixin.rrv;

import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.thonly.reverie_dreams.item.other.GuiPlaceholderItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(RecipeViewScreen.class)
public abstract class RecipeViewScreenMixin {
    @Inject(method = "getTooltipFromContainerItem", at=@At("HEAD"), cancellable = true)
    public void reverie_dreams$getTooltipFromContainerGuiItem(@NonNull ItemStack itemStack, CallbackInfoReturnable<List<Component>> cir) {
        if (itemStack.getItem() instanceof GuiPlaceholderItem) {
            cir.setReturnValue(new ArrayList<>(List.of()));
        }
    }
}
