package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Distinguishes a firearm hit while preserving its damage type and attacker attribution.
 */
public final class LunarBulletDamageSource extends DamageSource {
    public LunarBulletDamageSource(RegistryAccess registries, ResourceKey<DamageType> type, @Nullable Entity owner) {
        super(registries.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type), owner);
    }
}
