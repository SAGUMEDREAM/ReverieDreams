package cc.thonly.reverie_dreams.data.danmaku.stg.shape;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 发射形状类型。
 *
 * <ul>
 *     <li>{@link #AIM} —— 单发（沿基准方向，实现资料中的“假追踪”/直射）</li>
 *     <li>{@link #FAN} —— 扇形弹幕</li>
 *     <li>{@link #RING} —— 圆形弹幕（可朝外或保持同一方向）</li>
 *     <li>{@link #MULTI_RING} —— 多重圆环（花型弹幕的基础）</li>
 *     <li>{@link #MATRIX} —— 字符矩阵图案（星形/心形等）</li>
 * </ul>
 */
public enum ShapeType implements StringRepresentable {
    AIM,
    FAN,
    RING,
    MULTI_RING,
    MATRIX;

    public static final com.mojang.serialization.Codec<ShapeType> CODEC = StringRepresentable.fromEnum(ShapeType::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
