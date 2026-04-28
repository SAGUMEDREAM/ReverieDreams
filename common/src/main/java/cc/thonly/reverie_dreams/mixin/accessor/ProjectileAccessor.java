package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Projectile.class)
public interface ProjectileAccessor {

    @Accessor("hasBeenShot")
    boolean reverie_dreams$getHasBeenShot();

    @Accessor("hasBeenShot")
    void reverie_dreams$setHasBeenShot(boolean value);

    @Accessor("leftOwnerChecked")
    boolean reverie_dreams$getLeftOwnerChecked();

    @Accessor("leftOwnerChecked")
    void reverie_dreams$setLeftOwnerChecked(boolean value);
}
