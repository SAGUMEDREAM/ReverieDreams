package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 发射器的位置锚点。
 *
 * <ul>
 *     <li>{@link #SOURCE} —— 默认：attached 取实体眼位，否则用图案原点（保留启动时指定的相对偏移）</li>
 *     <li>{@link #ORIGIN} —— 图案原点（有实体时跟随；实体离开后停在最后位置）</li>
 *     <li>{@link #TARGET} —— 跟随目标眼位（目标消失时回退现有行为）</li>
 * </ul>
 */
public enum EmitAnchor implements StringRepresentable {
    SOURCE,
    ORIGIN,
    TARGET;

    public static final Codec<EmitAnchor> CODEC = StringRepresentable.fromEnum(EmitAnchor::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
