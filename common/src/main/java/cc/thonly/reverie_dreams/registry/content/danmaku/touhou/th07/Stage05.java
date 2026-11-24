package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

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

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.CYAN;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RICE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

final class Stage05 {
    private Stage05() {}

    private static final int GREEN = 0x58E058;

    // 五面（魂魄妖梦道中 + 关底，各难度共 5 张）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th07/s05_001_normal", "幽鬼剑「妖童饿鬼的断食」", 3000, () -> youmuSlash(0.016f, 10)),
                catalogOnly("touhou/th07/s05_002_normal", "狱界剑「二百由旬的一闪」", 3300),
                implemented("touhou/th07/s05_003_normal", "畜趣剑「无为无策的冥罚」", 3600, () -> karmicPunishment()),
                catalogOnly("touhou/th07/s05_004_normal", "人界剑「悟入幻想」", 3960),
                implemented("touhou/th07/s05_005_normal", "天上剑「天人的五衰」", 3960, () -> devaSigns(12, 105f, 0.5f, 58)),
                implemented("touhou/th07/s05_001_hard", "饿鬼剑「饿鬼道草纸」", 3000, () -> youmuSlash(0.019f, 12)),
                catalogOnly("touhou/th07/s05_002_hard", "狱炎剑「业风闪影阵」", 3300),
                catalogOnly("touhou/th07/s05_003_hard", "修罗剑「现世妄执」", 3600),
                catalogOnly("touhou/th07/s05_004_hard", "人世剑「大悟显晦」", 3960),
                implemented("touhou/th07/s05_005_hard", "天界剑「七魄忌讳」", 3960, () -> devaSigns(14, 120f, 0.54f, 52))
        );
    }

    /**
     * 五面妖梦 #69/#70（ecldata5 Sub42）：横斩位置的扩张圆环弹，左右交替。
     * 横斩演出、斩击转弹与 EX10 全局减速不在图案内，已标注；环扩张用 ACCELERATE 段还原。
     */
    private static DanmakuPattern youmuSlash(float expand, int ringCount) {
        DanmakuEmitter left = fire(EmitShape.ring(ringCount, 0.6f, true),
                shot(ARROWHEAD, 0.16f, BLUE, DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                        List.of(MotionSegment.accelerate(2, expand)))),
                0, 380, 85, EmitAim.FIXED, 20f, 0f, new Vec3(-3.0D, 0.0D, 0.0D), KeyframeRange.empty(0f, 0f));
        DanmakuEmitter right = fire(EmitShape.ring(ringCount, 0.6f, true),
                shot(ARROWHEAD, 0.16f, CYAN, DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                        List.of(MotionSegment.accelerate(2, expand)))),
                42, 380, 85, EmitAim.FIXED, 20f, 0f, new Vec3(3.0D, 0.0D, 0.0D), KeyframeRange.empty(0f, 0f));
        return DanmakuPattern.of(left, right);
    }

    /**
     * 五面妖梦 #77（Sub66）：数道纵向裂隙横向射出绿/蓝弹线。
     * 裂隙实体与“准备-时间减速-齐射”节奏未还原；弹线以窄扇近似。
     */
    private static DanmakuPattern karmicPunishment() {
        DanmakuEmitter[] rends = new DanmakuEmitter[3];
        float[] xs = {-2.6f, 0.0f, 2.6f};
        for (int i = 0; i < rends.length; i++) {
            rends[i] = fire(EmitShape.fan(6, 7f),
                    shot(RICE, 0.42f, i % 2 == 0 ? GREEN : BLUE),
                    i * 8, 380, 70, EmitAim.TARGET, 0f, 0f,
                    new Vec3(xs[i], 0.4D, 0.0D), KeyframeRange.empty(0f, 0f));
        }
        return DanmakuPattern.of(rends[0], rends[1], rends[2]);
    }

    /**
     * 五面妖梦 #85/#86（Sub75）：瞄准玩家的随机彩箭扇（五/七色分区）。
     * 每发随机着色近似五色分区排布（DanmakuSpawner.randomColor），分区排布未还原。
     */
    private static DanmakuPattern devaSigns(int count, float spread, float speed, int interval) {
        DanmakuEmitter fan = fire(EmitShape.fan(count, spread),
                DanmakuBulletSpec.of(ARROWHEAD, speed).withRandomColor(),
                0, 380, interval, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(fan);
    }
}
