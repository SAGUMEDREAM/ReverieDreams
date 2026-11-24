package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;

final class Stage06 {
    private Stage06() {}

    // 6 面 · 咲夜（道中）/ 蕾米莉亚
    static List<TouhouSpell> spells() {
        return List.of(
                // 道中奇术：Sub14(d6) L301-321。半圆随机 2/4/6/9 再叠另一半圆 12——随机，阻塞。
                catalogOnly("touhou/th06/s06_040_normal", "奇术「Eternal Meek」", 900),
                // 天罚：Sub30(d6) L840-879。EX7 两组各 8 路三段预警激光网络——可碰撞激光缺失，阻塞。
                catalogOnly("touhou/th06/s06_041_normal", "天罚「Star of David」", 2400),
                // 冥符：Sub32(d6) L930-970。24 路与 16 路反转旋环交替（ω ±0.0245 rad/帧，相位 +5.625°/环）。
                implemented("touhou/th06/s06_042_normal", "冥符「红色的冥界」", 2400, () -> crimsonNetherworld()),
                // 呪詛：Sub34(d6) L1023-1066。EX8 在已有弹处播种零速小弹、EX9 定向加速——播种子波缺失，阻塞。
                catalogOnly("touhou/th06/s06_043_normal", "诅咒「弗拉德·特佩斯的诅咒」", 2400),
                // 紅符：Sub38(d6) L1157-1192。固定高速弹 + 随机带 3/5 路——随机，阻塞。
                catalogOnly("touhou/th06/s06_044_normal", "红符「Scarlet Shoot」", 1800),
                // 红魔：Sub43(d6) L1392-1453。固定环叠 EX9 已有弹重定向——重定向缺失，阻塞。
                catalogOnly("touhou/th06/s06_045_normal", "「Red Magic」", 7200),
                catalogOnly("touhou/th06/s06_040_hard", "奇术「Eternal Meek」", 900),
                // 神罚：Sub31(d6) L887-923。EX7 激光网络 + 混合环——激光缺失，阻塞。
                catalogOnly("touhou/th06/s06_046_hard", "神罚「年幼的恶魔领主」", 2400),
                // 獄符：Sub33(d6) L974-1013。基础双 16 路旋环 + H/L 12/32 路可反弹环（flags 2560）——反弹缺失，阻塞。
                catalogOnly("touhou/th06/s06_047_hard", "狱符「千根针的针山」", 2400),
                // 神術：Sub35(d6) L1074-1111。狙扇 + EX8 播种 + EX9 加速——播种缺失，阻塞。
                catalogOnly("touhou/th06/s06_048_hard", "神术「吸血鬼幻想」", 2400),
                // 紅符：Sub39(d6) L1249-1293。高速弹 + 随机带——随机，阻塞。
                catalogOnly("touhou/th06/s06_049_hard", "红符「Scarlet Meister」", 1800),
                // 紅色幻想乡：Sub44(d6) L1471-1525。固定环 + EX11 改色运动 + EX9 重定向——重定向缺失，阻塞。
                catalogOnly("touhou/th06/s06_050_hard", "「红色的幻想乡」", 8400)
        );
    }

    /**
     * 冥符「紅色の冥界」：24 路与 16 路反转旋环交替（速度 1.8/2.2 px/f，ω ±4.2°/tick，
     * 相位 +5.625°/环）。原 4 帧一环降频至每环 20 tick。ECL Sub32(d6) L930-970。
     */
    private static DanmakuPattern crimsonNetherworld() {
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        int[] counts = {24, 24, 16, 16};
        float[] speeds = {1.8f, 1.8f, 2.2f, 2.2f};
        for (int i = 0; i < 4; i++) {
            float dir = i % 2 == 0 ? 4.2f : -4.2f;
            DanmakuBulletSpec spec = bullet(BALL, speeds[i], 55)
                    .withMotion(DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(MotionSegment.rotate(0, dir))));
            emitters.add(stream(EmitShape.ring(counts[i], 0f, true), spec, i * 5, 120, 20,
                    EmitAim.FIXED, 0f, 0f, KeyframeRange.empty(0f, 34f)));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }
}
