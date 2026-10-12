package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * 发射器使用的子弹规格。
 *
 * @param type        弹幕类型（{@code reverie_dreams:star} 等，见 danmaku_type 注册表）
 * @param color       染色，{@code -1} 表示使用类型默认颜色
 * @param randomColor 是否随机着色（每发独立随机）
 * @param speed       出膛速度（格/tick），{@code <= 0} 表示使用类型默认速度
 * @param scale       缩放倍率，{@code <= 0} 表示 1
 * @param damage      伤害覆盖，{@code <= 0} 表示使用类型默认伤害
 * @param lifetime    寿命（tick），{@code <= 0} 表示默认 200
 * @param motion      出膛后的运动模型（速度在生成时填入）
 */
public record DanmakuBulletSpec(
        Identifier type,
        int color,
        boolean randomColor,
        float speed,
        float scale,
        float damage,
        int lifetime,
        DanmakuMotion motion
) {
    public static final int DEFAULT_COLOR = -1;

    public static final Codec<DanmakuBulletSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("type").forGetter(DanmakuBulletSpec::type),
            Codec.INT.optionalFieldOf("color", DEFAULT_COLOR).forGetter(DanmakuBulletSpec::color),
            Codec.BOOL.optionalFieldOf("random_color", false).forGetter(DanmakuBulletSpec::randomColor),
            Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(DanmakuBulletSpec::speed),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(DanmakuBulletSpec::scale),
            Codec.FLOAT.optionalFieldOf("damage", 0f).forGetter(DanmakuBulletSpec::damage),
            Codec.INT.optionalFieldOf("lifetime", 0).forGetter(DanmakuBulletSpec::lifetime),
            DanmakuMotion.CODEC.optionalFieldOf("motion", DanmakuMotion.STRAIGHT).forGetter(DanmakuBulletSpec::motion)
    ).apply(instance, DanmakuBulletSpec::new));

    public static DanmakuBulletSpec of(Identifier type) {
        return new DanmakuBulletSpec(type, DEFAULT_COLOR, false, 0f, 1f, 0f, 0, DanmakuMotion.STRAIGHT);
    }

    public static DanmakuBulletSpec of(Identifier type, float speed) {
        return new DanmakuBulletSpec(type, DEFAULT_COLOR, false, speed, 1f, 0f, 0, DanmakuMotion.STRAIGHT);
    }

    public static DanmakuBulletSpec of(Identifier type, float speed, DanmakuMotion motion) {
        return new DanmakuBulletSpec(type, DEFAULT_COLOR, false, speed, 1f, 0f, 0, motion);
    }

    public DanmakuBulletSpec withColor(int color) {
        return new DanmakuBulletSpec(this.type, color, this.randomColor, this.speed, this.scale, this.damage, this.lifetime, this.motion);
    }

    public DanmakuBulletSpec withRandomColor() {
        return new DanmakuBulletSpec(this.type, this.color, true, this.speed, this.scale, this.damage, this.lifetime, this.motion);
    }

    public DanmakuBulletSpec withMotion(DanmakuMotion motion) {
        return new DanmakuBulletSpec(this.type, this.color, this.randomColor, this.speed, this.scale, this.damage, this.lifetime, motion);
    }

    public DanmakuBulletSpec withLifetime(int lifetime) {
        return new DanmakuBulletSpec(this.type, this.color, this.randomColor, this.speed, this.scale, this.damage, lifetime, this.motion);
    }
}
