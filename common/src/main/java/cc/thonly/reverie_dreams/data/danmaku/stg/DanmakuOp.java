package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 图案级操作（STG 弹幕引擎）：在 {@code at} tick 对阶段持有的所有子弹做一次性改写。
 *
 * <ul>
 *     <li>{@link #REDIRECT} —— 把所有 owned 子弹重定向到当前目标眼位方向；
 *         {@code value > 0} 时作为新速度（格/tick），否则保持各弹当前速度；无目标时跳过</li>
 *     <li>{@link #SPEED_SCALE} —— 所有 owned 子弹速度乘以 {@code value}</li>
 *     <li>{@link #FREEZE_OWNED} —— 所有 owned 子弹冻结 {@code duration} tick（位移 0、不加速度）</li>
 * </ul>
 *
 * <p>仅在 phaseOwned（阶段持有子弹）的图案运行时中生效；每个操作只应用一次。
 * 所有改写均发生在服务端并通过 motion 同步恢复双端确定性。
 */
public record DanmakuOp(int at, String type, float value, int duration) {
    public static final String REDIRECT = "redirect";
    public static final String SPEED_SCALE = "speed_scale";
    public static final String FREEZE_OWNED = "freeze_owned";
    public static final String STOP_AND_WHITEN = "stop_and_whiten";
    public static final String SCATTER_ACCELERATION = "scatter_acceleration";

    public static final Codec<DanmakuOp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("at", 0).forGetter(DanmakuOp::at),
            Codec.STRING.optionalFieldOf("type", REDIRECT).forGetter(DanmakuOp::type),
            Codec.FLOAT.optionalFieldOf("value", 0f).forGetter(DanmakuOp::value),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(DanmakuOp::duration)
    ).apply(instance, DanmakuOp::new));

    public static DanmakuOp redirect(int at) {
        return new DanmakuOp(at, REDIRECT, 0f, 0);
    }

    public static DanmakuOp redirect(int at, float speed) {
        return new DanmakuOp(at, REDIRECT, speed, 0);
    }

    public static DanmakuOp speedScale(int at, float scale) {
        return new DanmakuOp(at, SPEED_SCALE, scale, 0);
    }

    public static DanmakuOp freezeOwned(int at, int duration) {
        return new DanmakuOp(at, FREEZE_OWNED, 0f, duration);
    }

    public static DanmakuOp stopAndWhiten(int at) {
        return new DanmakuOp(at, STOP_AND_WHITEN, 0, 0);
    }

    public static DanmakuOp scatterAcceleration(int at, float acceleration, int duration) {
        return new DanmakuOp(at, SCATTER_ACCELERATION, acceleration, duration);
    }

    public boolean is(String opType) {
        return this.type.equalsIgnoreCase(opType);
    }
}
