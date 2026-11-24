package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuOp;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.layerStep;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.volley;

final class Stage02 {
    private static final Identifier ICICLE = ReverieDreams.id("icicle");
    private Stage02() {}

    // 2 面 · 大妖精（道中无符）/ 琪露诺
    static List<TouhouSpell> spells() {
        return List.of(
                // 冰符：Sub29 L614-676。镜像冰列：11 循环、间隔 27 帧，单列三速度层 6.5/4.5/2.5 px/f，
                // 列角 −11.25°+5.625k° 走动（镜像对称化）；NHL 每 120 帧补 5 路自机狙扇（L669）。
                implemented("touhou/th06/s02_003_normal", "冰符「Icicle Fall」", 1800, () -> icicleFall()),
                // 冻符：Sub31 L726-795。随机角速、EX0 冻结变白后向随机方向加速。
                implemented("touhou/th06/s02_005_normal", "冻符「Perfect Freeze」", 2400, () -> perfectFreeze(false)),
                // 雪符：Sub32 L807-834。EX1 在随机区发射，Normal 10 路、Hard 14 路。
                implemented("touhou/th06/s02_006_normal", "雪符「Diamond Blizzard」", 1980, () -> diamondBlizzard(false)),
                // 雹符：Sub30 L680-722。固定环 3 速度层 5.0/3.83/2.67 px/f，每 20 帧一波、8 波一组，
                // H 起始 8 路随轮次增加（L704）；随机相位省略，轮次增路取 8/12/16 三段。
                implemented("touhou/th06/s02_004_hard", "雹符「Hailstorm」", 2100, () -> hailstorm()),
                implemented("touhou/th06/s02_005_hard", "冻符「Perfect Freeze」", 2400, () -> perfectFreeze(true)),
                implemented("touhou/th06/s02_006_hard", "雪符「Diamond Blizzard」", 1980, () -> diamondBlizzard(true))
        );
    }

    // Sub31: EX0 at script frames 205/415. Only this cast's bullets are whitened/stopped/restarted.
    private static DanmakuPattern perfectFreeze(boolean hard) {
        int[] colors = {0x79C1FF, 0xAB8AFF, 0x7DE8AA, 0xF29ABB, 0xFDBB64};
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int wave = 0; wave < 30; wave++) {
            int tick = 40 + SpellTiming.framesToTicks((wave + 1) * 5);
            emitters.add(volley(EmitShape.ring(hard ? 10 : 7, 0, true),
                    bullet(ARROWHEAD, 4f, 200).withColor(colors[wave % colors.length]), tick, EmitAim.FIXED)
                    .withScatter(new DanmakuScatter(0, 0, 180, 0.25f)));
        }
        // The six mixed-speed pulses are grouped into alternating aimed fans for Minecraft.
        emitters.add(stream(EmitShape.fan(hard ? 5 : 3, hard ? 90 : 45),
                bullet(ARROWHEAD, 4f, 125), 154, 17, 7, EmitAim.TARGET, 0, 0, null));
        // The five-frame burst is looped six times before EX0. Its elapsed time is not the raw time label.
        int freeze = 40 + SpellTiming.framesToTicks(150 + 60);
        int restart = freeze + SpellTiming.framesToTicks(60 + 60 + 120);
        return new DanmakuPattern(emitters).withOps(List.of(
                DanmakuOp.stopAndWhiten(freeze),
                DanmakuOp.scatterAcceleration(restart, sp(0.03f), SpellTiming.framesToTicks(220))
        )).withTiming(new SpellTiming(800, 40, 210));
    }

    // Sub32 / EX1: one point within 128 x 96 playfield units per volley; 12 volleys per 120 frames.
    private static DanmakuPattern diamondBlizzard(boolean hard) {
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int wave = 1; wave <= 12; wave++) {
            emitters.add(volley(EmitShape.ring(hard ? 14 : 10, 0, true), bullet(ARROWHEAD, 2f, 85)
                    .withColor(0x90D4FF), 40 + SpellTiming.framesToTicks(wave * 10), EmitAim.FIXED)
                    .withScatter(new DanmakuScatter(8f, 6f, 180f, 0.2f)));
        }
        return new DanmakuPattern(emitters).withTiming(new SpellTiming(660, 41, 40));
    }

    /**
     * 氷符「アイシクルフォール」：镜像冰列两轮（11 循环 × 9 tick，列角由 ±100° 收拢到 ±17°），
     * 单列三速度层 6.5/4.5/2.5 px/f；NHL 每 40 tick 补 5 路自机狙扇。ECL Sub29 L614-676。
     */
    private static DanmakuPattern icicleFall() {
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int pass = 0; pass < 2; pass++) {
            for (int k = 0; k < 11; k++) {
                int delay = pass * 105 + k * 9;
                float yaw = 100f - 8.44f * k;
                emitters.add(volley(EmitShape.multiRing(3, 1, 0f, false, 0f, 0f, -0.3077f),
                        bullet(ICICLE, 6.5f, 55).withColor(0x65CEFF), delay, EmitAim.FIXED)
                        .withAim(EmitAim.FIXED, 0f, yaw));
                emitters.add(volley(EmitShape.multiRing(3, 1, 0f, false, 0f, 0f, -0.3077f),
                        bullet(ICICLE, 6.5f, 55).withColor(0x65CEFF), delay, EmitAim.FIXED)
                        .withAim(EmitAim.FIXED, 0f, -yaw));
            }
        }
        emitters.add(stream(EmitShape.fan(5, 60f), bullet(BALL, 2f, 60).withColor(0xF9DE71), 0, 210, 40,
                EmitAim.TARGET, 0f, 0f, null));
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }

    /**
     * 雹符「ヘイルストーム」：固定环三速度层（5.0/3.83/2.67 px/f），每 7 tick 一波，
     * 路数随轮次 8→12→16 三段推进（原版 8+轮）。ECL Sub30 L680-722。
     */
    private static DanmakuPattern hailstorm() {
        float step = layerStep(5f, 1.5f, 3);
        return DanmakuPattern.of(
                stream(EmitShape.multiRing(3, 8, 0f, true, 0f, 0f, step),
                        bullet(BALL, 5f, 60), 0, 52, 7, EmitAim.FIXED, 0f, 0f,
                        KeyframeRange.empty(0f, 90f)),
                stream(EmitShape.multiRing(3, 12, 0f, true, 0f, 0f, step),
                        bullet(BALL, 5f, 60), 56, 52, 7, EmitAim.FIXED, 0f, 0f,
                        KeyframeRange.empty(0f, 90f)),
                stream(EmitShape.multiRing(3, 16, 0f, true, 0f, 0f, step),
                        bullet(BALL, 5f, 60), 112, 52, 7, EmitAim.FIXED, 0f, 0f,
                        KeyframeRange.empty(0f, 90f))
        );
    }
}
