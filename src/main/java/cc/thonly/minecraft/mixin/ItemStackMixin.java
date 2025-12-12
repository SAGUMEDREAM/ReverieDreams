package cc.thonly.minecraft.mixin;

import cc.thonly.minecraft.api.ItemStackTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltipLines", at= @At("RETURN"))
    public void appendTooltipCallback(Item.TooltipContext tooltipContext,
                                      Player player,
                                      TooltipFlag tooltipFlag,
                                      CallbackInfoReturnable<List<Component>> cir
    ) {
        List<Component> components = cir.getReturnValue();
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemStackTooltipCallback.EVENT.invoker().appendTooltip(itemStack, player, tooltipContext, components::add, tooltipFlag);
    }
}
