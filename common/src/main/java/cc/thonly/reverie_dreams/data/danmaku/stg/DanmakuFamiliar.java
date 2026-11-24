package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * 使魔/辅助发射源（STG 弹幕引擎）：一个携带子图案的漂浮实体。
 *
 * <p>使魔在 {@code delay} tick 后生成于“源位置 + {@code offset}”：
 * <ul>
 *     <li>{@code duration <= 0} —— 随所在阶段存活，阶段取消时一起移除；</li>
 *     <li>{@code duration > 0} —— 到期自行消失；</li>
 *     <li>{@code orbitRadius > 0} —— 绕源实体水平公转（{@code orbitDegreesPerTick} 度/tick，
 *         初始角 {@code orbitStartAngle}），否则保持静止偏移。</li>
 * </ul>
 *
 * <p>使魔实体作为子图案 {@link #pattern()} 的发射源；子图案 phaseOwned，
 * 使魔实体消失时其子运行时因发射源失效自动清理。
 */
public record DanmakuFamiliar(int delay, int duration, Vec3 offset, float orbitRadius,
                              float orbitDegreesPerTick, float orbitStartAngle, DanmakuPattern pattern) {
    /**
     * 延迟初始化：{@link DanmakuPattern#CODEC} 引用本 CODEC，直接静态内嵌会产生类初始化环。
     */
    public static final Codec<DanmakuFamiliar> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("delay", 0).forGetter(DanmakuFamiliar::delay),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(DanmakuFamiliar::duration),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(DanmakuFamiliar::offset),
            Codec.FLOAT.optionalFieldOf("orbit_radius", 0f).forGetter(DanmakuFamiliar::orbitRadius),
            Codec.FLOAT.optionalFieldOf("orbit_degrees_per_tick", 0f).forGetter(DanmakuFamiliar::orbitDegreesPerTick),
            Codec.FLOAT.optionalFieldOf("orbit_start_angle", 0f).forGetter(DanmakuFamiliar::orbitStartAngle),
            DanmakuPattern.CODEC.fieldOf("pattern").forGetter(DanmakuFamiliar::pattern)
    ).apply(instance, DanmakuFamiliar::new)));

    public static DanmakuFamiliar of(Vec3 offset, DanmakuPattern pattern) {
        return new DanmakuFamiliar(0, 0, offset, 0f, 0f, 0f, pattern);
    }

    public DanmakuFamiliar withDelay(int delay) {
        return new DanmakuFamiliar(delay, this.duration, this.offset, this.orbitRadius,
                this.orbitDegreesPerTick, this.orbitStartAngle, this.pattern);
    }

    public DanmakuFamiliar withOrbit(float radius, float degreesPerTick, float startAngle) {
        return new DanmakuFamiliar(this.delay, this.duration, this.offset, radius,
                degreesPerTick, startAngle, this.pattern);
    }

    public DanmakuFamiliar withDuration(int duration) {
        return new DanmakuFamiliar(this.delay, duration, this.offset, this.orbitRadius,
                this.orbitDegreesPerTick, this.orbitStartAngle, this.pattern);
    }
}
