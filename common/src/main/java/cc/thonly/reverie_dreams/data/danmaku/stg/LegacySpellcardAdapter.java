package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 旧版符卡（{@link SpellCardFrameConfig}）到新引擎 {@link DanmakuPattern} 的转换工具。
 *
 * <p>旧引擎保留可用，这里提供一种“迁移到新引擎”的近似映射：
 * <ul>
 *     <li>{@code density / interval} 近似转换为每次齐射的扇形发数；</li>
 *     <li>{@code pitch_range / yaw_range} 直接作为固定朝向的角度偏移函数；</li>
 *     <li>{@code sync} 的异步错峰在近似映射中不做表达（如需可用相邻发射器延迟模拟）。</li>
 * </ul>
 */
public final class LegacySpellcardAdapter {
    private LegacySpellcardAdapter() {
    }

    public static DanmakuPattern toPattern(List<List<SpellCardFrameConfig>> frames) {
        List<DanmakuEmitter> emitters = new ArrayList<>();
        for (List<SpellCardFrameConfig> wave : frames) {
            for (SpellCardFrameConfig config : wave) {
                DanmakuEmitter emitter = toEmitter(config);
                if (emitter != null) {
                    emitters.add(emitter);
                }
            }
        }
        return new DanmakuPattern(emitters);
    }

    public static DanmakuEmitter toEmitter(SpellCardFrameConfig config) {
        Identifier typeId = BuiltInRegistryProviders.DANMAKU_TYPE.getKey(config.getType());
        if (typeId == null) {
            return null;
        }
        int interval = Math.max(1, config.getTickInterval());
        int perVolley = Math.max(1, config.getDensity() / interval);
        EmitShape shape = perVolley <= 1 ? EmitShape.aim() : EmitShape.fan(perVolley, 12f);

        DanmakuBulletSpec bullet = DanmakuBulletSpec.of(typeId, config.getSpeed() * 0.5f);
        if (config.isRandomColor()) {
            bullet = bullet.withRandomColor();
        } else if (config.getColor() >= 0) {
            bullet = bullet.withColor(config.getColor());
        }

        return new DanmakuEmitter(
                config.getTickDelay(),
                config.getTickDuration(),
                config.getTickInterval(),
                EmitAim.FIXED,
                true,
                Vec3.ZERO,
                KeyframeRange.empty(0f, 0f),
                0f,
                0f,
                config.getPitchRange(),
                config.getYawRange(),
                shape,
                bullet
        );
    }
}
