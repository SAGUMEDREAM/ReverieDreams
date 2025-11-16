package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.function.Supplier;

@Value
@With
@AllArgsConstructor
@Builder(toBuilder = true)
public class DanmakuProperties {
    public static final ResourceLocation DEFAULT_TEMPLATE_ID = ReverieDreams.id("single");
    public static final Supplier<DanmakuProperties> EMPTY = () -> new DanmakuProperties(
            DEFAULT_TEMPLATE_ID,
            1,
            2,
            RDDamageTypes.DANMAKU_GENERIC,
            1,
            0.5f,
            0,
            false,
            false
    );

    public ResourceLocation templateId;
    public int count;
    public float damage;
    public ResourceKey<DamageType> damageType;
    public float scale;
    public float speed;
    public float acceleration;
    public boolean tile;
    public boolean infinite;

    public static final Codec<DanmakuProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("templateId").forGetter(DanmakuProperties::getTemplateId),
                    Codec.INT.fieldOf("count").forGetter(DanmakuProperties::getCount),
                    Codec.FLOAT.fieldOf("damage").forGetter(DanmakuProperties::getDamage),
                    ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type", RDDamageTypes.DANMAKU_GENERIC).forGetter(DanmakuProperties::getDamageType),
                    Codec.FLOAT.fieldOf("scale").forGetter(DanmakuProperties::getScale),
                    Codec.FLOAT.fieldOf("speed").forGetter(DanmakuProperties::getSpeed),
                    Codec.FLOAT.fieldOf("acceleration").forGetter(DanmakuProperties::getAcceleration),
                    Codec.BOOL.fieldOf("tile").forGetter(DanmakuProperties::isTile),
                    Codec.BOOL.fieldOf("infinite").forGetter(DanmakuProperties::isInfinite)
            ).apply(instance, DanmakuProperties::new)
    );

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DanmakuProperties clone() {
        return this.toBuilder().build();
    }

    public static DanmakuProperties ofDefault() {
        return EMPTY.get();
    }
}