package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08.Th08PatternSupport.sweep;

final class FinalB {
    private FinalB() {}

    private static final Identifier TYPE_RICE = ReverieDreams.id("rice");

    // Final B · 八意永琳（先战）+ 蓬莱山辉夜（ecldata7.ecl，路线路径 s06b）
    static List<TouhouSpell> spells() {
        return List.of(
                // ecldata7 是 Final B：永琳先战一张，随后辉夜 5 组难题 + 5 段永夜返（各难度独立命名）
                catalogOnly("touhou/th08/s06b_001_normal", "药符「壶中大银河」", 2400),
                catalogOnly("touhou/th08/s06b_002_normal", "难题「龙颈之玉　-五色弹丸-」", 3600),
                catalogOnly("touhou/th08/s06b_003_normal", "难题「佛之御石钵　-不碎意志-」", 4620),
                catalogOnly("touhou/th08/s06b_004_normal", "难题「火鼠裘衣　-不焦之心-」", 3720),
                implemented("touhou/th08/s06b_005_normal", "难题「燕之子安贝　-永命线-」", 4200, () -> lifespring()),
                catalogOnly("touhou/th08/s06b_006_normal", "难题「蓬莱弹之枝　-虹色弹幕-」", 5400),
                // 永夜返：五段生存阶段（ins_155 超时型标记），生存条件缺失，仅保留目录
                catalogOnly("touhou/th08/s06b_007_normal", "「永夜返还　-新月-」", 1200),
                catalogOnly("touhou/th08/s06b_008_normal", "「永夜返还　-子之二刻-」", 1200),
                catalogOnly("touhou/th08/s06b_009_normal", "「永夜返还　-丑之二刻-」", 1200),
                catalogOnly("touhou/th08/s06b_010_normal", "「永夜返还　-寅之二刻-」", 1200),
                catalogOnly("touhou/th08/s06b_011_normal", "「永夜返还　-黎明-」", 1800),

                catalogOnly("touhou/th08/s06b_001_hard", "药符「壶中大银河」", 2400),
                catalogOnly("touhou/th08/s06b_002_hard", "神宝「辉煌龙形发饰」", 3600),
                catalogOnly("touhou/th08/s06b_003_hard", "神宝「佛门钻石」", 4620),
                catalogOnly("touhou/th08/s06b_004_hard", "神宝「火蜥蜴护盾」", 3720),
                implemented("touhou/th08/s06b_005_hard", "神宝「生命之泉无限」", 4200, () -> lifespring()),
                catalogOnly("touhou/th08/s06b_006_hard", "神宝「蓬莱玉枝　-梦色之乡-」", 5400),
                catalogOnly("touhou/th08/s06b_007_hard", "「永夜返还　-上弓之月-」", 1200),
                catalogOnly("touhou/th08/s06b_008_hard", "「永夜返还　-子之三刻-」", 1200),
                catalogOnly("touhou/th08/s06b_009_hard", "「永夜返还　-丑时三刻-」", 1200),
                catalogOnly("touhou/th08/s06b_010_hard", "「永夜返还　-寅之三刻-」", 1200),
                catalogOnly("touhou/th08/s06b_011_hard", "「永夜返还　-启明星-」", 1800)
        );
    }

    /**
     * 難題「燕の子安貝 -永命線-」 / 神宝「ライフスプリングインフィニティ」
     * （ecldata7.ecl Sub67，ecldata7.ecl.raw L2156-2193，卡 #164 F2 / #165 F4）。
     *
     * <p>原作（Sub67_1340，每 150 帧循环，L2186-2192）：ins_91(68,0,0,1500,-2,1000) 创建使魔后，
     * etFan(1, 2, 80, 2, 3.0, 1.5, -π/2, 0.05609987, 514)：80 波、波间 2 帧、每波 +0.0561 rad（3.21°）、
     * 起始向下 -90°、双层速度 3.0/4.5 px/f 的持续扫射；N/H 脚本参数完全一致（!EN/!HL 的 ins_2(140) 相同），
     * 难度差异仅在于血量与计时。mechanics.md L27 亦记录该指令 N=80、b≈0.05610。
     *
     * <p>MC 适配：速度 3.0/4.5 px/f → 0.5/0.75 格/tick（multiRing ringSpeedStep=0.5）；80 波÷2 帧 →
     * 26 tick×1 tick，总扫角 80×3.21°≈257° 用 yawRange 线性关键帧实现；150 帧循环 → 由 Boss 阶段
     * 调度重复施放（单图案为一轮突发）。
     *
     * <p>遗漏：中央使魔（ins_91 type=68，供弹/装饰载体）未复刻；弹色位（旗标 514）取类型默认色。
     */
    private static DanmakuPattern lifespring() {
        EmitShape doubleLayer = EmitShape.multiRing(2, 2, 0f, true, 0f, 0f, 0.5f);
        DanmakuEmitter sweepEmitter = sweep(DanmakuEmitter.of(doubleLayer,
                        DanmakuBulletSpec.of(TYPE_RICE, 0.5f).withLifetime(70)),
                0, 26, 1, 257f);
        return DanmakuPattern.of(sweepEmitter);
    }
}
