package cc.thonly.reverie_dreams.mixin.enchantment;

import cc.thonly.reverie_dreams.CommonEventHandlers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "modifyDamage", at = @At("RETURN"), cancellable = true)
    private static void reverie_dreams$modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity victim, DamageSource damageSource, float damage, CallbackInfoReturnable<Float> cir) {
        Float base = cir.getReturnValue();
        float byMoonEnchantment = CommonEventHandlers.onPostHitByMoonEnchantment(serverLevel, itemStack, victim);
        float cal = base + byMoonEnchantment;
        cir.setReturnValue(cal);
    }
}
