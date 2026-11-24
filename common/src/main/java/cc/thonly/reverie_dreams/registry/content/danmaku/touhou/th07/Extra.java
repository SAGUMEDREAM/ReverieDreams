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
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.DARK_BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.PURPLE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RED;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

final class Extra {
    private Extra() {}

    private static final int DARK_GREEN = 0x28A050;

    // Extra（道中橙两张 + 八云蓝十张；Phantasm 见 Phantasm.java）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th07/s07_001_extra", "鬼符「青鬼赤鬼」", 3300, () -> blueOniRedOni()),
                catalogOnly("touhou/th07/s07_002_extra", "鬼神「飞翔毗沙门天」", 3300),
                implemented("touhou/th07/s07_003_extra", "式神「仙狐思念」", 3600, () -> senkoShinen()),
                catalogOnly("touhou/th07/s07_004_extra", "式神「十二神将的宴席」", 3900),
                catalogOnly("touhou/th07/s07_005_extra", "式輝「狐狸妖怪激光」", 4500),
                catalogOnly("touhou/th07/s07_006_extra", "式輝「四面楚歌魅惑」", 3900),
                catalogOnly("touhou/th07/s07_007_extra", "式輝「天狐公主 -Illusion-」", 4500),
                catalogOnly("touhou/th07/s07_008_extra", "式弾「究极佛教徒」", 4800),
                catalogOnly("touhou/th07/s07_009_extra", "式弾「单方面接触」", 4800),
                catalogOnly("touhou/th07/s07_010_extra", "式神「橙」", 4800),
                catalogOnly("touhou/th07/s07_011_extra", "「狐狗狸大人的契约」", 4560),
                catalogOnly("touhou/th07/s07_012_extra", "幻神「饭纲权现降临」", 7200)
        );
    }

    /**
     * Extra 道中橙 #116（ecldata7 Sub58，raw L998-1046）：左右两侧的大泡柱，
     * 泡弹减速停顿后下坠并留下弹云，云中约半数瞄准玩家。
     * 泡弹爆散子波缺失，以 SET_SPEED+ACCELERATE 还原“冲出-停顿-下坠”轨迹与瞄准弹。
     */
    private static DanmakuPattern blueOniRedOni() {
        DanmakuMotion settle = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.setSpeed(16, 0.05f),
                MotionSegment.accelerate(16, 0.016f)));
        DanmakuEmitter blue = fire(EmitShape.fan(1, 0f), shot(BUBBLE, 0.4f, BLUE, settle),
                0, 380, 46, EmitAim.FIXED, 25f, 0f, new Vec3(-2.4D, 1.2D, 0.0D), KeyframeRange.empty(0f, 0f));
        DanmakuEmitter red = fire(EmitShape.fan(1, 0f), shot(BUBBLE, 0.4f, RED, settle),
                23, 380, 46, EmitAim.FIXED, 25f, 0f, new Vec3(2.4D, 1.2D, 0.0D), KeyframeRange.empty(0f, 0f));
        DanmakuEmitter aimed = fire(EmitShape.fan(2, 28f), shot(BALL, 0.5f, DARK_BLUE),
                70, 310, 40, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(blue, red, aimed);
    }

    /**
     * Extra 蓝 #118（Sub85，raw L920-967）：泡弹命中点炸开为方阵箭弹，中心弹瞄准。
     * 方阵用 MATRIX 形状还原；三色混排与“仅中心弹瞄准”未还原（方阵整体同色同向）。
     */
    private static DanmakuPattern senkoShinen() {
        DanmakuEmitter bubble = fire(EmitShape.fan(1, 0f), shot(BUBBLE, 0.55f, PURPLE),
                0, 380, 55, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter square = fire(EmitShape.matrix(List.of(
                "##.##",
                "#####",
                "##.##")),
                shot(ARROWHEAD, 0.4f, DARK_GREEN),
                14, 380, 55, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(bubble, square);
    }
}
