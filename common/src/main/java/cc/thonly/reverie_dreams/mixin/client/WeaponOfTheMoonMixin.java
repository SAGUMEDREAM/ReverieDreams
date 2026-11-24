package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.util.item.PolymerMagazineData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeaponOfTheMoon.class)
public class WeaponOfTheMoonMixin {
    @Inject(method = "getRounds", at = @At("HEAD"), cancellable = true)
    private void polymer$readClientRounds(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!Minecraft.getInstance().isSameThread())
            return;
        Integer rounds = PolymerMagazineData.encodedRounds(stack);
        if (rounds != null)
            cir.setReturnValue(rounds);
    }
}
