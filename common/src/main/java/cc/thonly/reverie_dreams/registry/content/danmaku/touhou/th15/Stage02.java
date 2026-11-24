package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15PatternSupport.*;

/** TH15 st02bs.ecl: Ringo's Normal and Hard spell-practice entries. */
final class Stage02 {
    private static final String RINGO = "ringo";
    private static final Identifier BALL = ReverieDreams.id("ball");
    private static final Identifier RICE = ReverieDreams.id("rice");
    private static final Identifier BULLET = ReverieDreams.id("bullet");
    private static final int ROSE = 0xE46E9F;
    private static final int GOLD = 0xDDBC61;
    private static final int VIOLET = 0x9A60D7;
    private static final int BLUE = 0x648DDD;
    private static final int LIME = 0x9ACD5A;
    private static final int CYAN = 0x5FC8C0;
    private static final int WHITE = 0xEDEDF2;

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(2, 10, false, RINGO, "兔符「草莓团子」", 2400, () -> dangoShower(false)),
                card(2, 10, true, RINGO, "兔符「浆果团子」", 2400, () -> dangoShower(true)),
                card(2, 14, false, RINGO, "兔符「团子引力」", 2400, () -> dangoInfluence(false)),
                card(2, 14, true, RINGO, "兔符「团子引力」", 2400, () -> dangoInfluence(true)),
                card(2, 18, false, RINGO, "赏月「九月满月」", 3000, () -> septemberFullMoon(false)),
                card(2, 18, true, RINGO, "赏月「九月满月」", 3000, () -> septemberFullMoon(true)));
    }

    // st02bs.ecl BossCard1_at (575-681): golden spirals — the heading advances by
    // 44.5°·i²/2 plus 1 rad/frame emitter drift, and the radius grows 0.7px per
    // shot; et pairs share the heading at half distance. The volley restarts
    // every 200f (BossCard1 559-566); Hard widens each shot to eight dango.
    private static DanmakuPattern dangoShower(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var dango = shot(BALL, hard ? 0.30f : 0.25f, ROSE, 110, MotionSegment.accelerate(20, 0.002f));
        for (int i = 0; i < 60; i++) {
            float yaw = (44.5f * i * (i - 1) / 2f + 57.3f * i) % 360f;
            double radius = (i % 2 == 0 ? 1 : 0.5) * i * 0.7 / 30.0;
            Vec3 offset = DanmakuMath.direction(0, yaw).scale(radius).add(0, 0.2, 0);
            var cue = sweep(fire(EmitShape.fan(hard ? 8 : 4, 6f), dango, 40 + i, 0, 1), 0, yaw, yaw);
            emitters.add(cue.withOffset(offset));
        }
        // ECL restarts the spiral every 200f with the first volley at 40f; delays
        // must sit inside [loopStart, loopStart+loopLength) so the loop replays them.
        return loop(emitters, 2400, 40, 200);
    }

    // st02bs.ecl BossCard2_at (796-856): three (N) / four (H) 140-way arc rings
    // whose turn rate flips every 60 frames, then a 3-way (N) / 5-way (H) kunai
    // fan curving at 45°/frame. The cycle repeats every 190f (795-861).
    private static DanmakuPattern dangoInfluence(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int i = 0; i < (hard ? 4 : 3); i++) {
            float turn = (i % 2 == 0 ? 1 : -1) * 2.06f;
            var ring = shot(BALL, 0.06f, GOLD, 120,
                    MotionSegment.rotate(0, turn), MotionSegment.rotate(20, -turn));
            var cue = sweep(fire(EmitShape.fan(140, 280f), ring, 40 + i * 5, 0, 1), 0, 0f, 0f);
            emitters.add(cue.withOffset(new Vec3(0, 0.2, 0)));
        }
        var kunai = shot(BULLET, hard ? 0.15f : 0.20f, ROSE, 40, MotionSegment.rotate(0, 15f));
        emitters.add(sweep(fire(EmitShape.fan(hard ? 5 : 3, hard ? 120f : 60f), kunai, 62, 0, 1), 0, 0, 0));
        // ECL cycle repeats every 190f with rings from 40f and kunai at 62f; the
        // delays above must stay inside [40, 230) to be replayed by the loop.
        return loop(emitters, 2400, 40, 190);
    }

    // st02bs.ecl BossCard3_at (1012-1067): nine star volleys. Seven sources sit
    // on a 32px circle rotating 40° per volley; each fires a 16 (N) / 20 (H)
    // 2°-spaced line of stationary stars that dash at 3.0 after 40f with a mild
    // chase, then settle to 0.2 (N) / 0.3 (H) (1131-1140).
    private static DanmakuPattern septemberFullMoon(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        int[] colors = {ROSE, GOLD, VIOLET, BLUE, LIME, CYAN, WHITE};
        for (int volley = 0; 63 + volley * 330 < 2990; volley++) {
            for (int j = 0; j < 7; j++) {
                float base = 90 + volley * 40f + j * 51.43f;
                Vec3 offset = DanmakuMath.direction(0, base).scale(1.07).add(0, 0.35, 0);
                var star = shot(STAR, 0.05f, colors[j], 90,
                        MotionSegment.setSpeed(13, 0.30f),
                        MotionSegment.homing(13, 6f, 30),
                        MotionSegment.setSpeed(46, hard ? 0.30f : 0.20f));
                var cue = sweep(fire(EmitShape.fan(hard ? 20 : 16, hard ? 38f : 32f), star,
                        63 + volley * 330, 0, 1), 0, 0, 0);
                emitters.add(cue.withOffset(offset));
            }
        }
        return timeline(emitters, 3000);
    }
}
