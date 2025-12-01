package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class RDDamageTypes {
    public static final ResourceKey<DamageType> DANMAKU_GENERIC = getOrCreateKey("danmaku_generic");
    public static final ResourceKey<DamageType> DANMAKU_REAL = getOrCreateKey("danmaku_real");

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(DANMAKU_GENERIC, new DamageType("danmaku", DamageScaling.NEVER,0.1f, DamageEffects.HURT));
        context.register(DANMAKU_REAL, new DamageType("danmaku", DamageScaling.NEVER,0.1f, DamageEffects.HURT));

    }

    public static DamageSource gerSource(RegistryAccess registryAccess, ResourceKey<DamageType> key) {
        return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(key)
        );
    }

    public static ResourceKey<DamageType> getOrCreateKey(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ReverieDreams.id(name));
    }
}
