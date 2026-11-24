package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BUBBLE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.DARK_BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.PINK;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.PURPLE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RICE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.WHITE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

final class Stage06 {
    private static final Identifier BUTTERFLY = ReverieDreams.id("butterfly");
    private Stage06() {}

    private static final Identifier STAR = ReverieDreams.id("star");

    // 六面（妖梦道中 + 幽幽子关底六阶段，各难度共 7 张；反魂蝶为生存卡）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th07/s06_001_normal", "六道剑「一念无量劫」", 2400, () -> singleThought(8, 2, 0.36f)),
                catalogOnly("touhou/th07/s06_002_normal", "亡乡「亡我乡 -宿罪-」", 3900),
                implemented("touhou/th07/s06_003_normal", "亡舞「生者必灭之理 -死蝶-」", 3900, () -> mortalityButterflies(false)),
                implemented("touhou/th07/s06_004_normal", "華霊「燕尾蝶」", 3900, () -> swallowtail(false)),
                catalogOnly("touhou/th07/s06_005_normal", "幽曲「弘川之藏 -亡灵-」", 3900),
                implemented("touhou/th07/s06_006_normal", "桜符「完全的墨染樱花 -亡我-」", 5940, () -> sumizomeSakura(4, 0.4f)),
                catalogOnly("touhou/th07/s06_007_normal", "「反魂蝶 -参分咲-」", 4020),
                implemented("touhou/th07/s06_001_hard", "六道剑「一念无量劫 -Hard-」", 2400, () -> singleThought(8, 2, 0.42f)),
                catalogOnly("touhou/th07/s06_002_hard", "亡乡「亡我乡 -无道之道-」", 3900),
                implemented("touhou/th07/s06_003_hard", "亡舞「生者必灭之理 -毒蛾-」", 3900, () -> mortalityButterflies(true)),
                implemented("touhou/th07/s06_004_hard", "華霊「深根蝴蝶」", 3900, () -> swallowtail(true)),
                catalogOnly("touhou/th07/s06_005_hard", "幽曲「弘川之藏 -幻灵-」", 3900),
                implemented("touhou/th07/s06_006_hard", "桜符「完全的墨染樱花 -春眠-」", 5940, () -> sumizomeSakura(5, 0.44f)),
                catalogOnly("touhou/th07/s06_007_hard", "「反魂蝶 -伍分咲-」", 4020)
        );
    }

    /**
     * 六面道中妖梦 #89/#90（ecldata6 Sub23）：八芒星斩位静态弹型。
     * 斩击演出与 EX13 每帧回调未还原；八向双环静态图案近似。
     */
    private static DanmakuPattern singleThought(int count, int rings, float speed) {
        DanmakuEmitter star = fire(EmitShape.multiRing(rings, count, 0.8f, true, 22.5f, 0f, 0f),
                shot(ARROWHEAD, speed, PURPLE),
                0, 340, 42, EmitAim.FIXED, 0f, 0f);
        return DanmakuPattern.of(star);
    }

    /**
     * 六面幽幽子 #97/#98（Sub50）：中途右转的蝴蝶弹 + 扇 aura 泡弹螺旋。
     * 蝴蝶转向用 ROTATE 段还原（原作紫蝶转向时机见 EnemyEclInstr 弹幕指令）。
     */
    private static DanmakuPattern mortalityButterflies(boolean hard) {
        float speed = hard ? 0.54f : 0.5f;
        DanmakuEmitter butterflies = fire(EmitShape.fan(2, 30f),
                shot(BUTTERFLY, speed, PURPLE, DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                        List.of(MotionSegment.rotate(28, 1.3f)))),
                0, 380, 22, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter bubbles = fire(EmitShape.ring(6, 1.0f, true), shot(BUBBLE, 0.24f, PINK),
                10, 380, hard ? 34 : 40, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(0.0D, 0.7D, 0.0D), KeyframeRange.empty(0f, hard ? -220f : -180f));
        return DanmakuPattern.of(butterflies, bubbles);
    }

    // Sub52 starts with eight butterflies. EX16 fires eight reverse five-shot fans;
    // only the transition to the moving Sub53/54 emitters is simplified here.
    private static DanmakuPattern swallowtail(boolean hard) {
        List<MotionSegment> motion = new java.util.ArrayList<>();
        for (int pulse = 0; pulse < 8; pulse++) {
            int at = SpellTiming.framesToTicks(pulse * 10);
            motion.add(MotionSegment.turn(at, 180, new Vec3(0, 1, 0)));
            motion.add(MotionSegment.split(at, MotionPayload.split(RICE, 0xEAE8FF, 1,
                    5, 90, hard ? 0.24f : 0.18f, 70)));
            motion.add(MotionSegment.turn(at, -180, new Vec3(0, 1, 0)));
        }
        var butterflies = shot(BUTTERFLY, 0.15f, hard ? DARK_BLUE : BLUE)
                .withMotion(DanmakuMotion.of(Vec3.ZERO, 0, 0, motion)).withLifetime(30);
        var seed = fire(EmitShape.fan(8, 210), butterflies, 60, 0, 1, EmitAim.TARGET, 0, 0);
        return DanmakuPattern.of(seed).withTiming(new SpellTiming(1300, 60, 85));
    }

    /**
     * 六面幽幽子 #109/#110（Sub58）：旋转樱花螺旋 + 瞄准米弹点缀。
     * 原作散花由 ins_81(7, 25) 触发，是视觉效果；不能替换为有伤害的花瓣弹。
     * 79 移动指令未还原，螺旋臂以旋转发射器近似。
     */
    private static DanmakuPattern sumizomeSakura(int arms, float speed) {
        DanmakuEmitter spiral = fire(EmitShape.ring(arms, 0.9f, true), shot(STAR, speed, PINK),
                0, 400, 8, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(0.0D, 0.6D, 0.0D), KeyframeRange.empty(0f, 560f));
        DanmakuEmitter aimed = fire(EmitShape.fan(5, 40f), shot(RICE, 0.3f, WHITE),
                30, 370, 50, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(spiral, aimed);
    }
}
