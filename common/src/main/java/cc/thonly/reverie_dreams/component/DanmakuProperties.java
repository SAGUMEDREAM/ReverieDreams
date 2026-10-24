package cc.thonly.reverie_dreams.component;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import lombok.With;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.function.Supplier;

/**
 * 弹幕的通用属性（物品组件 / 实体同步数据）。
 *
 * <p><b>速度语义迁移：</b>历史版本里 {@link #speed()} 存的是“组件语义速度”，
 * 实际飞行速度是它的一半；新版本统一为“实际格/tick”。
 * 为了兼容旧物品与旧存档，新增 {@link #speedVersion()}：
 * <ul>
 *     <li>{@code 0}（缺省）：旧数据，实际速度 = {@code speed / 2}；</li>
 *     <li>{@code 1}：新数据，{@code speed} 本身就是实际速度。</li>
 * </ul>
 * 新代码请使用 {@link #actualSpeed()} 读取实际速度，并通过 {@code withSpeedVersion(1)} 标记新数据。
 */
@With
@Builder(toBuilder = true)
public record DanmakuProperties(Identifier templateId, int count, float damage, ResourceKey<DamageType> damageType,
                                float scale, float speed, float acceleration, boolean tile, boolean infinite,
                                int speedVersion) {
    public static final Identifier DEFAULT_TEMPLATE_ID = ReverieDreams.id("single");
    public static final int CURRENT_SPEED_VERSION = 1;
    public static final Supplier<DanmakuProperties> EMPTY = () -> new DanmakuProperties(
            DEFAULT_TEMPLATE_ID,
            1,
            2,
            RDDamageTypes.DANMAKU_GENERIC,
            1,
            0.5f,
            0,
            false,
            false,
            CURRENT_SPEED_VERSION
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
                    Codec.BOOL.fieldOf("infinite").forGetter(DanmakuProperties::infinite),
                    Codec.INT.optionalFieldOf("speed_version", 0).forGetter(DanmakuProperties::speedVersion)
            ).apply(instance, DanmakuProperties::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf,DanmakuProperties> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final EntityDataSerializer<DanmakuProperties> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);

    /**
     * 实际飞行速度（格/tick），自动兼容旧数据。
     */
    public float actualSpeed() {
        return this.speedVersion >= CURRENT_SPEED_VERSION ? this.speed : this.speed * 0.5f;
    }

    public boolean isLegacySpeed() {
        return this.speedVersion < CURRENT_SPEED_VERSION;
    }

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
