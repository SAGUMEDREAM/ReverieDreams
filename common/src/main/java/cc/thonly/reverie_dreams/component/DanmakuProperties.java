package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageType;

import java.util.function.Supplier;

@With
@Builder(toBuilder = true)
public record DanmakuProperties(Identifier templateId, int count, float damage, ResourceKey<DamageType> damageType,
                                float scale, float speed, float acceleration, boolean tile, boolean infinite) {
    public static final Identifier DEFAULT_TEMPLATE_ID = ReverieDreams.id("single");
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
    public static final Codec<DanmakuProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("templateId").forGetter(DanmakuProperties::templateId),
                    Codec.INT.fieldOf("count").forGetter(DanmakuProperties::count),
                    Codec.FLOAT.fieldOf("damage").forGetter(DanmakuProperties::damage),
                    ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type", RDDamageTypes.DANMAKU_GENERIC).forGetter(DanmakuProperties::damageType),
                    Codec.FLOAT.fieldOf("scale").forGetter(DanmakuProperties::scale),
                    Codec.FLOAT.fieldOf("speed").forGetter(DanmakuProperties::speed),
                    Codec.FLOAT.fieldOf("acceleration").forGetter(DanmakuProperties::acceleration),
                    Codec.BOOL.fieldOf("tile").forGetter(DanmakuProperties::tile),
                    Codec.BOOL.fieldOf("infinite").forGetter(DanmakuProperties::infinite)
            ).apply(instance, DanmakuProperties::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,DanmakuProperties> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final EntityDataSerializer<DanmakuProperties> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DanmakuProperties clone() {
        return this.toBuilder().build();
    }

    public DanmakuProperties copy() {
        return this.clone();
    }

    public static DanmakuProperties ofDefault() {
        return EMPTY.get();
    }
}