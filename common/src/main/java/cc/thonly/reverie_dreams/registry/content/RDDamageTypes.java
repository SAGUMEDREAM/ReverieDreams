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
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class RDDamageTypes {
    public static final ResourceKey<DamageType> DANMAKU_GENERIC = getOrCreateKey("danmaku_generic");
    public static final ResourceKey<DamageType> DANMAKU_REAL = getOrCreateKey("danmaku_real");

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(DANMAKU_GENERIC, new DamageType("danmaku", DamageScaling.NEVER, 0.1f, DamageEffects.HURT));
        context.register(DANMAKU_REAL, new DamageType("danmaku", DamageScaling.NEVER, 0.1f, DamageEffects.HURT));

    }

    public static DamageSource create(RegistryAccess registryAccess, ResourceKey<DamageType> key) {
        return new DamageSource(registryAccess.getOrThrow(Registries.DAMAGE_TYPE).value().getOrThrow(key));
    }

    public static DamageSource create(RegistryAccess registryAccess, ResourceKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key), attacker);
    }

    public static DamageSource create(RegistryAccess registryAccess, ResourceKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key), source, attacker);
    }

    public static ResourceKey<DamageType> getOrCreateKey(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ReverieDreams.id(name));
    }
}
