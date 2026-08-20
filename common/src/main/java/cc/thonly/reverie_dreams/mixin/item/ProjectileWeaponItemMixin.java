package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.entity.ThrownCuisineItem;
import cc.thonly.reverie_dreams.util.item.ProjectileItemHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @Inject(method = "createProjectile", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$processAdditionalSupportItem(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit, CallbackInfoReturnable<Projectile> cir) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!ProjectileItemHelper.isThrowableFood(projectile)) {
            return;
        }
        ThrownCuisineItem throwable = Projectile.spawnProjectileFromRotation(ThrownCuisineItem::new, serverLevel, projectile, shooter, 0.0F, 1.5F, 1.0F);
        cir.setReturnValue(throwable);
    }
}
