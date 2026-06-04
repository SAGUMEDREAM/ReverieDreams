package cc.thonly.reverie_dreams.mixin.effect;

import cc.thonly.reverie_dreams.item.armor.CrownOfTheUnderworldItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.HealOrHarmMobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"SpellCheckingInspection", "BooleanMethodIsAlwaysInverted"})
@Mixin(HealOrHarmMobEffect.class)
public class HealOrHarmMobEffectMixin {
    @Shadow
    @Final
    private boolean isHarm;

    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$swapEffectBySomeConditions(ServerLevel level, LivingEntity mob, int amplification, CallbackInfoReturnable<Boolean> cir) {
        if (mob == null) {
            return;
        }
        if (!this.reverie_dreams$hasCrown(mob)) {
            return;
        }
        if (this.isHarm == mob.isInvertedHealAndHarm()) {
            mob.hurtServer(level, mob.damageSources().magic(), (float)(6 << amplification));
        } else {
            mob.heal((float)Math.max(4 << amplification, 0));
        }
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "applyInstantenousEffect", at=@At("HEAD"), cancellable = true)
    public void reverie_dreams$swapEffectBySomeConditions(ServerLevel serverLevel, @Nullable Entity source,@Nullable Entity owner, LivingEntity mob, int amplification, double scale, CallbackInfo ci) {
        if (mob == null) {
            return;
        }
        if (!this.reverie_dreams$hasCrown(mob)) {
            return;
        }
        if (this.isHarm == mob.isInvertedHealAndHarm()) {
            int amount = (int)(scale * (double)(6 << amplification) + (double)0.5F);
            if (source == null) {
                mob.hurtServer(serverLevel, mob.damageSources().magic(), (float)amount);
            } else {
                mob.hurtServer(serverLevel, mob.damageSources().indirectMagic(source, owner), (float)amount);
            }
        } else {
            int amount = (int)(scale * (double)(4 << amplification) + (double)0.5F);
            mob.heal((float)amount);
        }
        ci.cancel();
    }

    @Unique
    private boolean reverie_dreams$hasCrown(LivingEntity mob) {
        ItemStack stack = mob.getItemBySlot(EquipmentSlot.HEAD);
        return !stack.isEmpty() && stack.getItem() instanceof CrownOfTheUnderworldItem;
    }
}
