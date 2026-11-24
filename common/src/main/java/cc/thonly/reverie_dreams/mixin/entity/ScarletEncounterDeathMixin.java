package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ScarletEncounterDeathMixin {
    @Inject(method = "die", at = @At("TAIL"))
    private void reverie$scarletDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level() instanceof ServerLevel level && level.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator)
            generator.authoredStructures(level).adventureDeath(entity);
    }
}
