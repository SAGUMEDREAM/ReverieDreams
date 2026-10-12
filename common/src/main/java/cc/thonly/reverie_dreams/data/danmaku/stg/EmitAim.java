package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 发射器基准朝向的确定方式。
 *
 * <ul>
 *     <li>{@link #TARGET} —— 每次齐射瞬间瞄准目标（资料中的“假追踪弹幕”）</li>
 *     <li>{@link #FIXED} —— 使用发射器配置的固定 pitch/yaw</li>
 *     <li>{@link #SOURCE_FACING} —— 跟随发射源实体当前的视线方向</li>
 *     <li>{@link #OUTWARD} —— 由发射口指向环外的方向（螺旋/旋转弹幕常用）</li>
 * </ul>
 */
public enum EmitAim implements StringRepresentable {
    TARGET,
    FIXED,
    SOURCE_FACING,
    OUTWARD;

    public static final Codec<EmitAim> CODEC = StringRepresentable.fromEnum(EmitAim::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
