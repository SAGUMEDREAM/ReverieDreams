package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BUBBLE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.CYAN;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.DARK_BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.PINK;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RICE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

final class Stage01 {
    private Stage01() {}

    // 一面（琪露诺道中 Normal 无符卡；Hard 道中霜符；蕾蒂关底）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th07/s01_001_normal", "寒符「不散的余寒」", 3000, () -> lingeringCold(5, 22f, 0.42f, 0.17f)),
                implemented("touhou/th07/s01_002_normal", "冬符「凋零之花」", 3000, () -> flowerWitherAway(2, 0.32f)),
                implemented("touhou/th07/s01_001_hard", "霜符「冰霜之柱」", 2280, () -> frostColumns()),
                implemented("touhou/th07/s01_002_hard", "寒符「不散的余寒 -Hard-」", 3000, () -> lingeringCold(7, 26f, 0.46f, 0.2f)),
                catalogOnly("touhou/th07/s01_003_hard", "白符「波动光线」", 3000)
        );
    }

    /**
     * 一面蕾蒂 #3/#4（ecldata1 Sub42，raw L1190-1332）：大型瞄准弹 +
     * 其后逐层展开的小弹线 + 环绕雾团慢弹。TH07 速度 s1→s2 以 3 帧 = 1 tick 折算为格/tick。
     * Hard 两层线（THBWiki 卡 005 描述），密度与弹速为 MC 适配。
     */
    private static DanmakuPattern lingeringCold(int lineCount, float spread, float bigSpeed, float lineSpeed) {
        DanmakuEmitter big = fire(EmitShape.fan(1, 0f), shot(BUBBLE, bigSpeed, CYAN),
                0, 340, 55, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter lines = fire(EmitShape.fan(lineCount, spread),
                shot(RICE, lineSpeed, DARK_BLUE, DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                        List.of(MotionSegment.accelerate(2, 0.018f)))),
                4, 340, 55, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter mist = fire(EmitShape.ring(5, 1.4f, true), shot(BALL, 0.28f, DARK_BLUE),
                24, 320, 16, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(0.0D, 0.6D, 0.0D), KeyframeRange.empty(0f, 150f));
        return DanmakuPattern.of(big, lines, mist);
    }

    /**
     * 一面蕾蒂 #7（Sub48）：环绕的灵体留下米弹轨迹组成花瓣，Boss 偶发慢瞄准环。
     * 花瓣轨迹以绕锚点旋转的贴身发射器近似（低弹速留痕）；灵体实体未还原，图案辨识度保留。
     */
    private static DanmakuPattern flowerWitherAway(int arms, float petalSpeed) {
        DanmakuEmitter[] arms_ = new DanmakuEmitter[arms];
        for (int i = 0; i < arms; i++) {
            arms_[i] = fire(EmitShape.fan(1, 0f), shot(RICE, petalSpeed, PINK),
                    0, 360, 3, EmitAim.OUTWARD, -8f, 0f,
                    new Vec3(1.7D, 0.2D, 0.0D), KeyframeRange.empty(130f * i, 130f * i + 520f));
        }
        DanmakuEmitter bossRing = fire(EmitShape.ring(10, 0.5f, true), shot(BALL, 0.3f, DARK_BLUE),
                60, 300, 110, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(arms_[0], arms_[1], bossRing);
    }

    /**
     * 一面道中琪露诺 #0（Sub29，raw L734-801，仅 Hard/Lunatic 到达）：
     * 多方向的蓝碎弹线；原作中线弹随后倍增为随机方向冰柱（分裂子波），此处仅保留多向线弹，
     * 分裂缺失已标注。
     */
    private static DanmakuPattern frostColumns() {
        DanmakuEmitter sweep = fire(EmitShape.fan(3, 12f),
                shot(ARROWHEAD, 0.55f, CYAN),
                0, 380, 26, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter sweepAlt = fire(EmitShape.fan(3, 12f),
                shot(ARROWHEAD, 0.5f, BLUE),
                13, 380, 26, EmitAim.TARGET, 0f, 0f);
        sweep = sweep.withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, 90f));
        sweepAlt = sweepAlt.withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(0f, -90f));
        return DanmakuPattern.of(sweep, sweepAlt);
    }
}
