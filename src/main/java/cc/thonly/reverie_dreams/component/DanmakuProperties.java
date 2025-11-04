package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.ReverieDreams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

@Value
@With
@AllArgsConstructor
@Builder(toBuilder = true)
public class DanmakuProperties {
    public static final ResourceLocation DEFAULT_TEMPLATE_ID = ReverieDreams.id("single");
    public static final ResourceLocation DEFAULT_DAMAGE_TYPE = ReverieDreams.id("generic");
    public static final Supplier<DanmakuProperties> EMPTY = () -> new DanmakuProperties(
            DEFAULT_TEMPLATE_ID,
            1,
            2,
            DEFAULT_DAMAGE_TYPE,
            1,
            0.5f,
            0,
            false,
            false
    );

    public ResourceLocation templateId;
    public int count;
    public float damage;
    public ResourceLocation damageType;
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
                    ResourceLocation.CODEC.fieldOf("damageType").forGetter(DanmakuProperties::getDamageType),
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