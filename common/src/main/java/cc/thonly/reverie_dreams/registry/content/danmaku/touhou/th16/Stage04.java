package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16PatternSupport.*;

/** TH16 st04bs.ecl: Yatadera Narumi's stage-4 cards. */
final class Stage04 {
    private static final String NARUMI = "yatadera_narumi";
    private static final int LIME = 0x9DFF2A;
    private static final int GREEN = 0x2DFF00;
    private static final int CYAN = 0x28FFFF;
    private static final int RED = 0xFF0000;

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 34, false, NARUMI, "魔符「即席菩提」", 1800, () -> instantBodhi(false)),
                card(4, 34, true, NARUMI, "魔符「即席菩提」", 1800, () -> instantBodhi(true)),
                card(4, 38, false, NARUMI, "魔符「子弹魔像」", 2280, Stage04::bulletGolem),
                card(4, 38, true, NARUMI, "魔符「宠物的巨大弹生命体」", 2280, Stage04::bulletGolem),
                card(4, 42, false, NARUMI, "地藏「罪业救赎」", 2460, () -> criminalSalvation(false)),
                card(4, 42, true, NARUMI, "地藏「业火救济」", 2460, () -> criminalSalvation(true)));
    }

    // st04bs BossCard1 (460-590): slow rising amulets (sprite 11/13, 0.1 px/f start with a
    // 0x80000000/120f speed ramp, 519-535) spawn around the player's column and grow.
    private static DanmakuPattern instantBodhi(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bodhi = shot(AMULET, 0.01f, LIME, 240, MotionSegment.setSpeed(ticks(120), hard ? 0.25f : 0.15f));
        for (int v = 0; v < 20; v++) {
            emitters.add(fire(EmitShape.aim(), bodhi, 120 + v * 12, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3((v % 9 - 4) * 1.6f, 0.2, -2.5f)));
        }
        return loop(emitters, 1800, 120, ticks(240));
    }

    // st04bs BossCard2 (605-690): bomb entities (BossCard2Bomb) roll across the field and burst
    // into bullets — ported as roaming bomb familiars.
    private static DanmakuPattern bulletGolem() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var burst = DanmakuPattern.of(fire(EmitShape.ring(8, 0f, true),
                shot(PELLET, 0.12f, GREEN, 120), 0, 40, 4).withAim(EmitAim.TARGET, 0, 0));
        for (int k = 0; k < 6; k++) {
            float z = -6f + 2f * k;
            familiars.add(DanmakuFamiliar.of(new Vec3(k % 2 == 0 ? -7f : 7f, 0.2, z), burst)
                    .withDelay(ticks(120) + k * ticks(30)).withDuration(ticks(200)));
        }
        var cover = shot(BULLET, 0.16f, CYAN, 150);
        emitters.add(fire(EmitShape.ring(10, 0f, true), cover, 120, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(2280), ticks(120), ticks(210)));
    }

    // st04bs BossCard3 (841-1050): jizo pillars — slow upward bullets (sprite 25/0, -90 deg,
    // 0.1 px/f ramping) spawn symmetrically around the player column (ins_628 offsets).
    private static DanmakuPattern criminalSalvation(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var jizo = shot(BALL, 0.01f, GREEN, 260, MotionSegment.setSpeed(ticks(120), hard ? 0.3f : 0.2f));
        for (int v = 0; v < 16; v++) {
            float x = (v % 8 - 4) * 1.8f;
            emitters.add(fire(EmitShape.ring(1, 0f, true), jizo, 120 + v * 14, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0)
                    .withOffset(new Vec3(x, 0.2, -2f)).withJitter(4f));
        }
        var pillar = shot(BULLET, 0.2f, RED, 150, MotionSegment.rotate(0, 0.4f));
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.aim(), pillar, 180 + v * 20, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0), 0, 0f, 0f));
        }
        return loop(emitters, 2460, 120, ticks(230));
    }
}
