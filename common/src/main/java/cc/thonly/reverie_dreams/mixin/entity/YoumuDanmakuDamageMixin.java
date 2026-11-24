package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.item.weapon.YoumuSwordCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Supplies incoming velocity without changing shared danmaku damage/ownership semantics.
 */
@Mixin(BaseDanmakuEntity.class)
public abstract class YoumuDanmakuDamageMixin {
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void youmu$projectileDirection(Entity target, DamageSource source, float damage, Operation<Void> original) {
        YoumuSwordCombat.projectileDamage((BaseDanmakuEntity) (Object) this, () -> original.call(target, source, damage));
    }
}
