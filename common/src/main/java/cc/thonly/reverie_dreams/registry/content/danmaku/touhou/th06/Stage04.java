package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.RICE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.layerStep;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;

final class Stage04 {
    private Stage04() {}

    private static final Identifier BUBBLE = ReverieDreams.id("bubble");
    private static final Identifier FIREBALL = ReverieDreams.id("fireball");

    // 4 面 · 小恶魔（道中无符）/ 帕秋莉
    static List<TouhouSpell> spells() {
        return List.of(
                // 开卡为自机×机体分支（Sub37 分派器 L1401-1549、Sub39/40/41 组合链 L1584-1680；
                // engine.md §4：灵梦A 27→30→28、灵梦B 29→30→31、魔理沙A 28→31→27、魔理沙B 31→29→30；
                // E/N/HL 分别经过 1/2/3 段组合）。N 首单 #14-17、次单 #21/20/19/18；H 首单 #19/23/20/21、次单 #25/24/22/26。
                // 火符：Sub42 L1684-1726。三重反转旋环（11/12/13 路随轮循环，速度 2.2/1.5/0.7 px/f，ω ±0.0245 rad/帧）。
                implemented("touhou/th06/s04_014_normal", "火符「Agni Shine」", 1800, () -> agniShine()),
                // 水符：Sub45 L1821-1874。16×2 狙扇 + 三束自机狙激光（束速 4 宽 6，L1862 附近 ins_85）——激光缺失，阻塞。
                catalogOnly("touhou/th06/s04_015_normal", "水符「Princess Undine」", 2100),
                // 木符：Sub47，主波和两个连续边缘源见 Stage04Wood。
                implemented("touhou/th06/s04_016_normal", "木符「Sylphy Horn」", 2100, () -> Stage04Wood.horn(false, false)),
                // 土符：Sub50 L2097-2124。随机半圆 E/N 7/10——随机，阻塞。
                catalogOnly("touhou/th06/s04_017_normal", "土符「Lazy Trilithon」", 2100),
                // 金符：Sub53 L2198-2225。8 路固定环速度 4.0，逐波相位 +45°、定时重设角度（flags 257）→ TURN 段近似。
                implemented("touhou/th06/s04_018_normal", "金符「Metal Fatigue」", 2100, () -> metalFatigue(8)),
                // 火符上級：Sub43 L1730-1774。双层旋环（N 8-10 路，层速 2.0/1.6 px/f，层间角 π/24，ω ±0.0245）。
                implemented("touhou/th06/s04_019_normal", "火符「Agni Shine上级」", 1800, () -> agniLayered(9, 2f, 1.2f)),
                // 木符上級：Sub48，变换加速方向与一侧发射位置。
                implemented("touhou/th06/s04_020_normal", "木符「Sylphy Horn上级」", 2100, () -> Stage04Wood.horn(true, false)),
                // 土符上級：Sub51 L2128-2156。随机半圆 N 10——随机，阻塞。
                catalogOnly("touhou/th06/s04_021_normal", "土符「Lazy Trilithon上级」", 2100),
                // 火&土符：Sub55 L2259-2306。火固定环 12 路 ×2 层（2.5/1.6 px/f，层间角 π/24）；
                // 土的随机半圆（L2301 ins_75）省略并标注。
                implemented("touhou/th06/s04_027_normal", "火&土符「Lava Cromlech」", 2400, () -> lavaCromlech(12)),
                // 木&火符：Sub56 L2310-2364。12/22/32/22 路全角随机主波——随机，阻塞。
                catalogOnly("touhou/th06/s04_028_normal", "木&火符「Forest Blaze」", 2400),
                // 水&木符：Sub57 L2368-2419。狙环 24 路 ×2 层（2.8/2.0 px/f，层间角 20°）+ 10 路狙扇（180°）交替。
                implemented("touhou/th06/s04_029_normal", "水&木符「Water Elf」", 2400, () -> waterElf(24, 20, 10)),
                // 金&水符：Sub58 L2423-2462。双向曲线狙环（15+轮路，2 层 1.5/1.15 px/f，ω ±0.0123 rad/帧 → 曲线弹）。
                implemented("touhou/th06/s04_030_normal", "金&水符「Mercury Poison」", 2400, () -> mercuryPoison(18)),
                // 土&金符：Sub59 L2466-2508。全角随机主波（N 12 路 ins_75）——随机，阻塞。
                catalogOnly("touhou/th06/s04_031_normal", "土&金符「Emerald Megalith」", 2400),
                // Hard：首单 #19/23/20/21、次单 #25/24/22/26、组合 #27-31。
                implemented("touhou/th06/s04_019_hard", "火符「Agni Shine上级」", 1800, () -> agniLayered(9, 2f, 1.2f)),
                implemented("touhou/th06/s04_020_hard", "木符「Sylphy Horn上级」", 2100, () -> Stage04Wood.horn(true, true)),
                catalogOnly("touhou/th06/s04_021_hard", "土符「Lazy Trilithon上级」", 2100),
                // 火符辐射：Sub44 L1778-1817。双层反转旋环（7-9 路，层速 1.5/1.0 px/f）；随机 2 路叠加省略并标注。
                implemented("touhou/th06/s04_022_hard", "火符「Agni Radiance」", 1800, () -> agniLayered(8, 1.5f, 1f)),
                // 水符：Sub46 L1878-1924。24/30×2 狙环 + 2 条预警狙激光 + 10 路扇——激光缺失，阻塞。
                catalogOnly("touhou/th06/s04_023_hard", "水符「Bury in Lake」", 2100),
                // 木符：Sub49，间歇主波与交替的左右边源。
                implemented("touhou/th06/s04_024_hard", "木符「Green Storm」", 2100, () -> Stage04Wood.greenStorm()),
                // 土符：Sub52 L2160-2195。随机半圆 H 12——随机，阻塞。
                catalogOnly("touhou/th06/s04_025_hard", "土符「Trilithon Shake」", 2100),
                // 金符：Sub54 L2229-2255。固定环 8 路 ×3 层（6.0/4.17/2.33 px/f），定时重设角度 → TURN 段近似。
                implemented("touhou/th06/s04_026_hard", "金符「Silver Dragon」", 2100, () -> silverDragon(8)),
                implemented("touhou/th06/s04_027_hard", "火&土符「Lava Cromlech」", 2400, () -> lavaCromlech(16)),
                catalogOnly("touhou/th06/s04_028_hard", "木&火符「Forest Blaze」", 2400),
                implemented("touhou/th06/s04_029_hard", "水&木符「Water Elf」", 2400, () -> waterElf(30, 20, 10)),
                implemented("touhou/th06/s04_030_hard", "金&水符「Mercury Poison」", 2400, () -> mercuryPoison(22)),
                catalogOnly("touhou/th06/s04_031_hard", "土&金符「Emerald Megalith」", 2400)
        );
    }

    /**
     * 火符「アグニシャイン」：三重反转旋环（11/12/13 路随轮循环取静态三环，速度 2.2/1.5/0.7 px/f，
     * ω ±0.0245 rad/帧 → ±4.2°/tick 的 ROTATE 段）。ECL Sub42 L1684-1726。固定环随机初相省略。
     */
    private static DanmakuPattern agniShine() {
        float[] speeds = {2.2f, 1.5f, 0.7f};
        int[] counts = {11, 12, 13};
        int[] lifetimes = {60, 70, 80};
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            float dir = i % 2 == 0 ? 4.2f : -4.2f;
            DanmakuBulletSpec spec = bullet(FIREBALL, speeds[i], lifetimes[i])
                    .withMotion(DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(MotionSegment.rotate(0, dir))));
            emitters.add(stream(EmitShape.ring(counts[i], 0f, true), spec, 0, 120, 3,
                    EmitAim.FIXED, 0f, 0f, null));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }

    /**
     * 火符上級／火符辐射：双层反转旋环（层间角 π/24，ω ±4.2°/tick）。
     * 上級 ECL Sub43 L1730-1774：层速 2.0/1.6 px/f（N 8-10 路，取 9）；
     * 辐射 ECL Sub44 L1778-1817：层速 1.5/1.0 px/f（H 7-9 路，取 8），其随机 2 路叠加省略并标注。
     */
    private static DanmakuPattern agniLayered(int count, float s1, float s2) {
        float step = layerStep(s1, s2, 2);
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            float dir = i == 0 ? 4.2f : -4.2f;
            DanmakuBulletSpec spec = bullet(FIREBALL, s1, 65)
                    .withMotion(DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(MotionSegment.rotate(0, dir))));
            emitters.add(stream(EmitShape.multiRing(2, count, 0f, true, 7.5f, 0f, step),
                    spec, 0, 120, 3, EmitAim.FIXED, 0f, 0f, null));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }

    /**
     * 金符「メタルファティーグ」：8 路固定环速度 4.0 px/f，逐波相位 +45°、定时重设角度（flags 257）
     * → 每波独立发射器、飞行中 TURN。原波间隔 120 帧压缩至 25 tick。ECL Sub53 L2198-2225。
     */
    private static DanmakuPattern metalFatigue(int count) {
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int k = 0; k < 8; k++) {
            DanmakuBulletSpec spec = bullet(BALL, 4f, 55)
                    .withMotion(DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                            List.of(MotionSegment.turn(12, 45f * k, new Vec3(0, 1, 0)))));
            emitters.add(stream(EmitShape.ring(count, 0f, true), spec, k * 25, 0, 1,
                    EmitAim.FIXED, 0f, 0f, null));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }

    /**
     * 金符「シルバードラゴン」：固定环 8 路 ×3 速度层（6.0/4.17/2.33 px/f），起速 6、定时重设角度
     * （flags 257）→ 飞行中 TURN 段近似；原波间隔 50 帧取 25 tick。ECL Sub54 L2229-2255。
     */
    private static DanmakuPattern silverDragon(int count) {
        float step = layerStep(6f, 0.5f, 3);
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int k = 0; k < 6; k++) {
            DanmakuBulletSpec spec = bullet(BALL, 6f, 55)
                    .withMotion(DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                            List.of(MotionSegment.turn(15, 22.5f, new Vec3(0, 1, 0)))));
            emitters.add(stream(EmitShape.multiRing(3, count, 0f, true, 0f, 0f, step),
                    spec, k * 25, 0, 1, EmitAim.FIXED, 0f, 0f, null));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }

    /**
     * 火＆土符「ラーヴァクロムレク」：火固定环 ×2 速度层（2.5/1.6 px/f，层间角 π/24）；
     * 土的随机半圆省略并标注。ECL Sub55 L2259-2306。
     */
    private static DanmakuPattern lavaCromlech(int count) {
        float step = layerStep(2.5f, 0.7f, 2);
        return DanmakuPattern.of(
                stream(EmitShape.multiRing(2, count, 0f, true, 7.5f, 0f, step),
                        bullet(FIREBALL, 2.5f, 60), 0, 120, 6,
                        EmitAim.FIXED, 0f, 0f, null)
        );
    }

    /**
     * 水＆木符「ウォーターエルフ」：交替狙环（主环 24/30 路 ×2 层 2.8/2.0 px/f、层间角 20°；
     * 次环 20 路）+ 10 路狙扇（180°）。原周期 20 帧降频至每环 20 tick。ECL Sub57 L2368-2419。
     */
    private static DanmakuPattern waterElf(int mainCount, int subCount, int fanCount) {
        float step = layerStep(2.8f, 1.2f, 2);
        return DanmakuPattern.of(
                stream(EmitShape.multiRing(2, mainCount, 0f, true, 20f, 0f, step),
                        bullet(BUBBLE, 2.8f, 60), 0, 160, 20, EmitAim.TARGET, 0f, 0f, null),
                stream(EmitShape.multiRing(2, subCount, 0f, true, 20f, 0f, step),
                        bullet(BUBBLE, 2.8f, 60), 10, 160, 20, EmitAim.TARGET, 0f, 0f, null),
                stream(EmitShape.fan(fanCount, 180f), bullet(RICE, 1.8f, 55),
                        0, 160, 20, EmitAim.TARGET, 0f, 0f, null)
        );
    }

    /**
     * 金＆水符「マーキュリポイズン」：双向曲线狙环（ω ±0.7°/帧 → ±2.1°/tick 的 ROTATE 段，
     * 层间角 ±20°，2 速度层 1.5/1.15 px/f）。ECL Sub58 L2423-2462。
     */
    private static DanmakuPattern mercuryPoison(int count) {
        float step = layerStep(1.5f, 0.8f, 2);
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        for (int i = 0; i < 2; i++) {
            float dir = i == 0 ? 2.1f : -2.1f;
            DanmakuBulletSpec spec = bullet(BUBBLE, 1.5f, 70)
                    .withMotion(DanmakuMotion.of(Vec3.ZERO, 0.001f, 0f,
                            List.of(MotionSegment.rotate(0, dir))));
            emitters.add(stream(EmitShape.multiRing(2, count, 0f, true, i == 0 ? 20f : -20f, 0f, step),
                    spec, 0, 160, 13, EmitAim.TARGET, 0f, 0f, null));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }
}
