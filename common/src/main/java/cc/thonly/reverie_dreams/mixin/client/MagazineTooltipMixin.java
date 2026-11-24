package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.util.item.PolymerMagazineData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(InitTooltips.class)
public class MagazineTooltipMixin {
    @Inject(method = "appendMagazineTooltip", at = @At("HEAD"), cancellable = true)
    private static void polymer$avoidDuplicateAmmoLine(ItemStack stack, Item.TooltipContext context,
                                                       TooltipDisplay display, Player player, Consumer<Component> consumer, TooltipFlag flag, CallbackInfo ci) {
        if (Minecraft.getInstance().isSameThread() && PolymerMagazineData.hasGeneratedMagazineTooltip(stack))
            ci.cancel();
    }
}
