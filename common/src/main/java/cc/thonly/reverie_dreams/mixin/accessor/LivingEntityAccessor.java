package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("SLEEPING_POS_ID")
    static EntityDataAccessor<Optional<BlockPos>> getSleepingPosition() {
        throw new UnsupportedOperationException();
    }

    @Accessor("jumping")
    boolean reverie_dreams$keyJump();

    @Invoker("actuallyHurt")
    void reverie_dreams$actuallyHurt(ServerLevel level, DamageSource source, float dmg);

    @Invoker("getDamageAfterMagicAbsorb")
    float reverie_dreams$getDamageAfterMagicAbsorb(DamageSource damageSource, float damage);

    @Invoker("getDamageAfterArmorAbsorb")
    float reverie_dreams$getDamageAfterArmorAbsorb(DamageSource damageSource, float damage);
}
