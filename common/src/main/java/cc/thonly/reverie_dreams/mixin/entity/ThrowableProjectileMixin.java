package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.inf.IThrowableProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin extends Projectile implements IThrowableProjectile {
    protected ThrowableProjectileMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }
}
