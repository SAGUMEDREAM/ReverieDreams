package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * STG 子弹运动模型。
 *
 * <p>与旧版 {@code ThrowableItemProjectile} 物理不同，新引擎中子弹的运动完全由本模型驱动：
 * <ul>
 *     <li>{@link #velocity()} 初始速度向量（格/tick），由发射器在生成时写入；</li>
 *     <li>{@link #acceleration()} 沿自身方向的持续加速度（可为负）；</li>
 *     <li>{@link #spin()} 视觉自转（度/tick）；</li>
 *     <li>{@link #origin()} 出膛点（由生成器写入），供 {@code center_mode=origin} 的公转段使用；</li>
 *     <li>{@link #segments()} 分段时间线，见 {@link MotionSegment}。</li>
 * </ul>
 *
 * <p>该模型随实体同步（EntityData），服务端与客户端使用同一套确定性步进，
 * 因此不需要额外的位置同步即可保持子弹轨迹一致。
 */
public record DanmakuMotion(Vec3 velocity, float acceleration, float spin, Vec3 origin, List<MotionSegment> segments) {
    public static final Codec<DanmakuMotion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.optionalFieldOf("velocity", Vec3.ZERO).forGetter(DanmakuMotion::velocity),
            Codec.FLOAT.optionalFieldOf("acceleration", 0f).forGetter(DanmakuMotion::acceleration),
            Codec.FLOAT.optionalFieldOf("spin", 0f).forGetter(DanmakuMotion::spin),
            Vec3.CODEC.optionalFieldOf("origin", Vec3.ZERO).forGetter(DanmakuMotion::origin),
            MotionSegment.CODEC.listOf().optionalFieldOf("segments", List.of()).forGetter(DanmakuMotion::segments)
    ).apply(instance, DanmakuMotion::new));
    public static final StreamCodec<ByteBuf, DanmakuMotion> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    public static final EntityDataSerializer<DanmakuMotion> SERIALIZER = EntityDataSerializer.forValueType(STREAM_CODEC);
    public static final DanmakuMotion STRAIGHT = new DanmakuMotion(Vec3.ZERO, 0f, 0f, Vec3.ZERO, List.of());

    public DanmakuMotion {
        segments = List.copyOf(segments.stream()
                .sorted(Comparator.comparingInt(MotionSegment::at))
                .toList());
    }

    public static DanmakuMotion straight(Vec3 velocity) {
        return new DanmakuMotion(velocity, 0f, 0f, Vec3.ZERO, List.of());
    }

    public static DanmakuMotion of(Vec3 velocity, float acceleration, float spin, List<MotionSegment> segments) {
        return new DanmakuMotion(velocity, acceleration, spin, Vec3.ZERO, segments);
    }

    public DanmakuMotion withVelocity(Vec3 velocity) {
        return new DanmakuMotion(velocity, this.acceleration, this.spin, this.origin, this.segments);
    }

    public DanmakuMotion withOrigin(Vec3 origin) {
        return new DanmakuMotion(this.velocity, this.acceleration, this.spin, origin, this.segments);
    }

    public DanmakuMotion withAcceleration(float acceleration) {
        return new DanmakuMotion(this.velocity, acceleration, this.spin, this.origin, this.segments);
    }

    public boolean isStraight() {
        return this.segments.isEmpty() && this.acceleration == 0f && this.spin == 0f;
    }
}
