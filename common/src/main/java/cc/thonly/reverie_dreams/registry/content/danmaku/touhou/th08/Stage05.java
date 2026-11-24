package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.ShapeType;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08.Th08PatternSupport.sweep;

final class Stage05 {
    private Stage05() {}

    private static final Identifier TYPE_STAR = ReverieDreams.id("star");

    // Stage 5 · 因幡帝（道中）/ 铃仙·优昙华院·因幡（ecldata5.ecl）
    static List<TouhouSpell> spells() {
        return List.of(

                // 按规则先登记道中，卡名与触发口径见报告

                implemented("touhou/th08/s05_002_normal", "波符「赤眼催眠(精神震撼)」", 3000, () -> mindShaker(false)),
                catalogOnly("touhou/th08/s05_003_normal", "懒符「生神停止(怠速波动)」", 3000),
                catalogOnly("touhou/th08/s05_004_normal", "狂符「幻视调律(幻想调律)」", 4620),
                catalogOnly("touhou/th08/s05_005_normal", "散符「真实之月(隐形满月)」", 3000),
                catalogOnly("touhou/th08/s05_006_normal", "月眼「月兔远隔催眠术(遥距催眠)」", 2640),


                implemented("touhou/th08/s05_002_hard", "幻波「赤眼催眠(精神破坏)」", 3000, () -> mindShaker(true)),
                catalogOnly("touhou/th08/s05_003_hard", "懒惰「生神停止(精神静止)」", 3000),
                catalogOnly("touhou/th08/s05_004_hard", "狂视「狂视调律(幻象追寻)」", 4620),
                catalogOnly("touhou/th08/s05_005_hard", "散符「真实之月(隐形满月)」", 3000),
                catalogOnly("touhou/th08/s05_006_hard", "月眼「月兔远隔催眠术(遥距催眠)」", 2640)
        );
    }

    /**
     * 波符/幻波「赤眼催眠」（ecldata5.ecl Sub62，ecldata5.ecl.raw L1493-1558，卡 #101 F2 / #102 F4）。
     *
     * <p>原作主段（Sub62_1220，循环）：两路 etRing(16, 2, 48, 1, 1.0, 1.0, 随机角, 0.09817 rad, 旗标双色)
     * 互差 0.0654 rad（3.75°）（L1515-1518），即每帧 2 连对、每波旋转 5.625° 的旋转双色双臂；
     * 后段 etRing(16, 2, I7, 2, 1.4, 0.8, …) 与 etRing(16, 6, I7, 2, 1.2, 0.6, …)
     * （L1546-1555），I7=E24/N32/H48/L64。
     *
     * <p>MC 适配：速度 1.0/1.4/1.2 px/f → 0.35/0.47/0.40 格/tick；48 波×1 帧 → 24 tick×1 tick，
     * 总扫角 48×5.625°≈270° 用 yawRange 线性关键帧实现；Hard 波数更多（48 vs 32）→ 六角段时长加长。
     *
     * <p>遗漏：funcCall(12,…) 经 funcSet 的 aimed 波形段（L1526-1541）与异步 Sub85 未解码；
     * Normal 的「幻视错向」表现（卡面注释：直线弹看起来偏向别处）无弹幕层实现；etEx 变换（若生效于
     * 双臂速度）未复刻。局部还原。
     */
    private static DanmakuPattern mindShaker(boolean hard) {
        int hexTicks = hard ? 20 : 16;
        DanmakuEmitter armA = sweep(DanmakuEmitter.of(ring(2, 0f),
                        DanmakuBulletSpec.of(TYPE_STAR, 0.35f).withColor(0xFF5A5A).withLifetime(70)),
                0, 24, 1, 270f);
        DanmakuEmitter armB = sweep(DanmakuEmitter.of(ring(2, 3.75f),
                        DanmakuBulletSpec.of(TYPE_STAR, 0.35f).withColor(0x5A8CFF).withLifetime(70)),
                0, 24, 1, 270f);
        DanmakuEmitter hexA = sweep(DanmakuEmitter.of(ring(6, 0f),
                        DanmakuBulletSpec.of(TYPE_STAR, 0.47f).withColor(0xFF5A5A).withLifetime(60)),
                28, hexTicks, 2, hexTicks * 5.625f);
        DanmakuEmitter hexB = sweep(DanmakuEmitter.of(ring(6, 0f),
                        DanmakuBulletSpec.of(TYPE_STAR, 0.40f).withColor(0x5A8CFF).withLifetime(60)),
                28, hexTicks, 2, hexTicks * 5.625f);
        return DanmakuPattern.of(armA, armB, hexA, hexB);
    }

    /** 指定起始角的环形（count=2 时为一对对置弹）。 */
    private static EmitShape ring(int count, float startAngle) {
        return new EmitShape(ShapeType.RING, count, 0f, startAngle, 0f, true, 1, 0f, 0f, 0f, List.of());
    }
}
