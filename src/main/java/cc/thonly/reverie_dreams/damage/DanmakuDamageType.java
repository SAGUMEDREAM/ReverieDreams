package cc.thonly.reverie_dreams.damage;

import cc.thonly.mystias_izakaya.component.CraftingConflict;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

@Setter
@Getter
public class DanmakuDamageType implements RegistrableObject<DanmakuDamageType> {
    public static final Codec<DanmakuDamageType> CODEC = RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
            .xmap(DanmakuDamageType::new, DanmakuDamageType::getRegistryKey);
    private Identifier id;
    private final RegistryKey<DamageType> registryKey;

    public DanmakuDamageType() {
        this.registryKey = null;
    }

    public DanmakuDamageType(RegistryKey<DamageType> registryKey) {
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

    public RegistryKey<DamageType> getType() {
        return this.registryKey;
    }

    public DamageType getValue(DynamicRegistryManager registryManager) {
        if (this.registryKey == null) return null;
        Registry<DamageType> registry = registryManager.getOrThrow(RegistryKeys.DAMAGE_TYPE);
        return registry.get(this.registryKey);
    }

    @Override
    public StandaloneRegistry<DanmakuDamageType> getRegistryRef() {
        return RegistryManager.DANMAKU_DAMAGE_TYPE;
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
