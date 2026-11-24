package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th16.Th16PatternSupport.*;

/** TH16 st01bs.ecl: Eternity Larva's stage-1 cards. */
final class Stage01 {
    private static final String LARVA = "eternity_larva";
    private static final int CYAN = 0x28FFFF;
    private static final int PINK = 0xFD8BFF;
    private static final int MAGENTA = 0xF600E1;

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(1, 0, false, LARVA, "蝶符「分钟鳞粉」", 1500, () -> minuteScales(false)),
                card(1, 0, true, LARVA, "蝶符「凤蝶的鳞粉」", 1500, () -> minuteScales(true)),
                card(1, 4, false, LARVA, "蝶符「翩跹之夏」", 1500, () -> flutteringSummer(false)),
                card(1, 4, true, LARVA, "蝶符「盛夏振翅」", 1500, () -> flutteringSummer(true)));
    }

    // st01bs BossCard1 (318-469): 7/10/11-volley fans of 6-count butterfly wings (sprite 8/10,
    // speed 1.5/2.5 px/f, spin 40 deg/f, heading drifting -7 deg per volley, at 376-408) while
    // 1-3 larva familiars fly around the player spraying 1/1/2/3-count bullets (at2 410-469).
    private static DanmakuPattern minuteScales(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var scale = shot(BUTTERFLY, hard ? 0.25f : 0.15f, CYAN, 180, MotionSegment.rotate(0, 1.33f));
        for (int v = 0; v < (hard ? 11 : 7); v++) {
            var cue = fire(EmitShape.fan(6, 20f), scale, 120 + v * 10, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, -7f * v, -7f * v));
        }
        var familiars = new ArrayList<DanmakuFamiliar>();
        var spray = shot(BALL, hard ? 0.2f : 0.1f, PINK, 150, MotionSegment.rotate(0, 0.12f));
        var sprayPattern = DanmakuPattern.of(fire(EmitShape.fan(hard ? 3 : 1, 8f), spray, 0, 200, hard ? 10 : 20)
                .withAim(EmitAim.TARGET, 0, 0));
        familiars.add(DanmakuFamiliar.of(new Vec3(0, 0.2, -3), sprayPattern)
                .withOrbit(3f, 1.4f, 0f).withDelay(ticks(120)).withDuration(ticks(1300)));
        if (hard) {
            familiars.add(DanmakuFamiliar.of(new Vec3(4, 0.2, -2), sprayPattern)
                    .withOrbit(3f, -1.6f, 120f).withDelay(ticks(180)).withDuration(ticks(1300)));
        }
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(1500), ticks(120), ticks(110)));
    }

    // st01bs BossCard2 (471-660): the midsummer variant — same fan skeleton with faster cadence
    // and the larvae spraying from player-relative offsets (at2 605+).
    private static DanmakuPattern flutteringSummer(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var scale = shot(BUTTERFLY, hard ? 0.3f : 0.2f, CYAN, 170, MotionSegment.rotate(0, -1.33f));
        for (int v = 0; v < (hard ? 12 : 8); v++) {
            var cue = fire(EmitShape.fan(6, 20f), scale, 120 + v * 8, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, 7f * v, 7f * v));
        }
        var familiars = List.of(DanmakuFamiliar.of(new Vec3(0, 0.2, -3.5),
                DanmakuPattern.of(fire(EmitShape.fan(hard ? 2 : 1, 10f),
                        shot(BALL, hard ? 0.25f : 0.12f, MAGENTA, 140, MotionSegment.rotate(0, -0.12f)),
                        0, 240, hard ? 8 : 18).withAim(EmitAim.TARGET, 0, 0)))
                .withOrbit(3.5f, 1.8f, 90f).withDelay(ticks(120)).withDuration(ticks(1300)));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(1500), ticks(120), ticks(100)));
    }
}
