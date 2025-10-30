package cc.thonly.minecraft.mixin;

import cc.thonly.minecraft.api.ItemStackTooltipCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "appendTooltip", at= @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;", ordinal = 0))
    public void appendTooltipCallback(Item.TooltipContext context, TooltipDisplay displayComponent, Player player, TooltipFlag type, Consumer<Component> textConsumer, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        ItemStackTooltipCallback.EVENT.invoker().appendTooltip(itemStack, context, displayComponent, player, textConsumer, type);
    }
}
