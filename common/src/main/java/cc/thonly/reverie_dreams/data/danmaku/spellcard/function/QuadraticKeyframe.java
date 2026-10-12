package cc.thonly.reverie_dreams.data.danmaku.spellcard.function;

/**
 * 二次曲线关键帧：{@code start + (end - start) * t²}。
 *
 * <p>用于匀加速旋转弹幕（角速度随时间线性增长 ⇒ 角度随时间二次增长）。
 */
public class QuadraticKeyframe implements KeyframeFunction {
    private final float start;
    private final float end;

    public QuadraticKeyframe(float start, float end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public float sample(float t) {
        return start + (end - start) * t * t;
    }
}
