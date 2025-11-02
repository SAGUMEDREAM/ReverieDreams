package cc.thonly.reverie_dreams.damage;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.OwnerBinding;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

@Setter
@Getter
public class DanmakuDamageType implements CodecStep<DanmakuDamageType>, OwnerBinding<DanmakuDamageType>, BuiltinObject {
    public static final Codec<DanmakuDamageType> CODEC = ResourceKey.codec(Registries.DAMAGE_TYPE)
            .xmap(DanmakuDamageType::new, DanmakuDamageType::getRegistryKey);
    public static final ResourceLocation DEFAULT_ID = ReverieDreams.id("generic");
    private ResourceLocation id;
    private final ResourceKey<DamageType> registryKey;
    private IntrinsicalRegister<DanmakuDamageType> owner;

    public DanmakuDamageType(ResourceKey<DamageType> registryKey) {
        this.registryKey = registryKey;
    }

    public DamageSource mapToSource(DamageSources sources) {
        if (this.registryKey == null) {
            return null;
        }
        if (this.registryKey == DamageTypes.GENERIC) {
            return sources.generic();
        }
        if (this.registryKey == DamageTypes.MAGIC) {
            return sources.magic();
        }
        return sources.generic();
    }

    public ResourceKey<DamageType> getType() {
        return this.registryKey;
    }

    public DamageType getValue(RegistryAccess registryManager) {
        if (this.registryKey == null) return null;
        Registry<DamageType> registry = registryManager.lookupOrThrow(Registries.DAMAGE_TYPE);
        return registry.getValue(this.registryKey);
    }

    @Override
    public Codec<DanmakuDamageType> getCodec() {
        return CODEC;
    }

    @FunctionalInterface
    public interface SourceGetter {
        DamageSource getSource();
    }
}
