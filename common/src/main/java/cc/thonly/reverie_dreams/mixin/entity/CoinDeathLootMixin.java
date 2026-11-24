package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.loot.CoinDrops;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class CoinDeathLootMixin {
    @Shadow
    protected abstract boolean shouldDropLoot(ServerLevel level);

    @Unique
    private boolean reverieDreams$coinLootHandled;

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void reverieDreams$dropCoins(ServerLevel level, DamageSource source, CallbackInfo ci) {
        var victim = (LivingEntity) (Object) this;
        if (reverieDreams$coinLootHandled || !victim.isDeadOrDying())
            return;
        reverieDreams$coinLootHandled = true;
        if (shouldDropLoot(level))
            CoinDrops.drop(level, victim, source);
    }
}
