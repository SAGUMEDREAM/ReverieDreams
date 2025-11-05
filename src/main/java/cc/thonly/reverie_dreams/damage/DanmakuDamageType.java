package cc.thonly.reverie_dreams.damage;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;

@Setter
@Getter
public class DanmakuDamageType implements CodecStep<DanmakuDamageType>, OwnerBinding<DanmakuDamageType>, BuiltinObject {
    public static final Codec<DanmakuDamageType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(DanmakuDamageType::getId)
    ).apply(instance, DanmakuDamageType::getOrCreate));
    public static final ResourceLocation DEFAULT_ID = ReverieDreams.id("generic");
    private final ResourceLocation id;
    private RegistryHandler<DanmakuDamageType> owner;

    public DanmakuDamageType(ResourceLocation id) {
        this.id = id;
    }

    public static DanmakuDamageType getOrCreate(ResourceLocation id) {
        DanmakuDamageType value = RegistryHandlers.DANMAKU_DAMAGE_TYPE.getValue(id);
        if (value == null) {
            return RegistryHandlers.register(RegistryHandlers.DANMAKU_DAMAGE_TYPE, id, new DanmakuDamageType(id));
        }
        return value;
    }

    public DamageSource mapToSource(DamageSources sources) {
        if (this.id == DanmakuDamageTypes.REAL.getId()) {
            return sources.magic();
        }
        return sources.generic();
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
