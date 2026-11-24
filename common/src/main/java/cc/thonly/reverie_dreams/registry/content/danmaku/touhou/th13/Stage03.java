package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/** TH13 st03mbs/st03bs.ecl: Kogasa's graveyard cameo and Yoshika's stage-3 boss cards. */
final class Stage03 {
    private static final String YOSHIKA = "miyako_yoshika";
    private static final String KOGASA = "tatara_kogasa";
    // Sampled from th13 bullet.anm: spr20 = 0xF600E1 magenta, spr0 = white, spr9 = green,
    // spr10 umbrella = 0x85FF9C green, spr4 shard = 0xFD8BFF pink.
    private static final int MAGENTA = 0xF600E1;
    private static final int WHITE = 0xFFFFFF;
    private static final int GREEN = 0x36FF36;
    private static final int UMBRELLA_GREEN = 0x85FF9C;
    private static final int SHARD_PINK = 0xFD8BFF;

    private Stage03() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(3, 30, true, KOGASA, "虹符「雨伞旋风」", 2700, Stage03::umbrellaCyclone),
                card(3, 32, false, YOSHIKA, "回复「凭欲治疗」", 1800, Stage03::healByDesire),
                card(3, 32, true, YOSHIKA, "回复「凭欲治疗」", 1800, Stage03::healByDesire),
                card(3, 36, false, YOSHIKA, "毒爪「毒爪涌起」", 3000, () -> poisonClaws(false, false)),
                card(3, 38, true, YOSHIKA, "毒爪「毒爪谋杀」", 3000, () -> poisonClaws(true, false)),
                extraCard(3, 121, YOSHIKA, "毒爪「不死的杀人鬼」", 3000, () -> poisonClaws(true, true)),
                card(3, 40, false, YOSHIKA, "欲符「稼欲灵招来」", 2520, () -> desireHarvest(false)),
                card(3, 42, true, YOSHIKA, "欲灵「点数欲望吞噬者」", 2520, () -> desireHarvest(true)));
    }

    // st03mbs MBossCard1 (278-376): every ~360f cycle 10/20/20/20 volleys of a seven-layer
    // spiral — pairs of umbrella (sprite 10) and shard (sprite 4) streams fired 1f apart with
    // per-stream spins 31/2/13 deg/f and a base heading drifting -9.1 deg per volley
    // (%C = -0.159 rad, 334-371). Bullets 1/1/2/3 per layer at 1.6-2.15 px/f aimed at the player.
    private static DanmakuPattern umbrellaCyclone() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var big = shot(BALL, 0.16f, UMBRELLA_GREEN, 220, MotionSegment.rotate(0, 1.8f));
        var small = shot(PELLET, 0.16f, SHARD_PINK, 200, MotionSegment.rotate(0, 0.12f));
        var mid = shot(RICE, 0.16f, SHARD_PINK, 200, MotionSegment.rotate(0, 0.76f));
        for (int v = 0; v < 20; v++) {
            int at = v * 7;
            float yaw = -9.1f * v;
            emitters.add(sweep(fire(EmitShape.ring(1, 0f, true), big, at, 0, 1), 0, yaw, yaw + 31f * 7));
            emitters.add(sweep(fire(EmitShape.ring(1, 0f, true), small, at + 1, 0, 1), 0, yaw, yaw + 2f * 7));
            emitters.add(sweep(fire(EmitShape.ring(v >= 5 ? 2 : 1, 0f, true), mid, at + 2, 0, 1), 0, yaw, yaw + 13f * 7));
            emitters.add(sweep(fire(EmitShape.ring(1, 0f, true), small, at + 3, 0, 1), 0, yaw, yaw + 2f * 7));
            emitters.add(sweep(fire(EmitShape.ring(1, 0f, true), mid, at + 4, 0, 1), 0, yaw, yaw + 13f * 7));
            emitters.add(sweep(fire(EmitShape.ring(1, 0f, true), small, at + 5, 0, 1), 0, yaw, yaw + 2f * 7));
            emitters.add(sweep(fire(EmitShape.ring(1, 0f, true), mid, at + 6, 0, 1), 0, yaw, yaw + 13f * 7));
        }
        return loop(emitters, 2700, 60, 120);
    }

    // st03bs BossCard1 (397-633): Yoshika eats desire phantoms to heal, gating her fire. Phantom
    // familiars (PhantomC1) spawn endlessly at field edges until 3 (E) / 5 (N) / 7 (HL) are alive
    // (BossCard1_at2* 510-633). Her attack: 24-count rings aimed at the player (BossCard1_at
    // 493-508), speed 0.2 -> ~2.2 px/f across frames 4-120, spin 6 deg/f, repeating as the heal
    // gates open (progressively faster: 200 -> 100 frame gates, 443-475).
    private static DanmakuPattern healByDesire() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var ring = shot(BALL, 0.02f, MAGENTA, 200,
                MotionSegment.setSpeed(ticks(120), 0.22f),
                MotionSegment.rotate(ticks(4), 0.35f));
        for (int volley = 0; volley < 12; volley++) {
            int at = 150 + volley * 90;
            var cue = fire(EmitShape.ring(24, 0f, true), ring, at, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, 6f * volley, 6f * volley));
        }
        var familiars = new ArrayList<DanmakuFamiliar>();
        Vec3[] spots = {new Vec3(-8, 0.2, 1), new Vec3(8, 0.2, 1),
                new Vec3(-6, 0.2, 4), new Vec3(6, 0.2, 4),
                new Vec3(-7, 0.2, 5), new Vec3(7, 0.2, 5),
                new Vec3(0, 0.2, 5)};
        for (int k = 0; k < spots.length; k++) {
            familiars.add(DanmakuFamiliar.of(spots[k], new DanmakuPattern(List.of()))
                    .withDelay(ticks(150) + k * ticks(40)).withDuration(ticks(1500)));
        }
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(1800), ticks(150), ticks(540)));
    }

    // st03bs BossCard2 (635-943): pairs of poison-claw entities (BossCard2Et 681-722) sweep across
    // the player's row, each spraying a rotating aimed stream for 290f — 1/2 bullets per frame at
    // 0.3 px/f accelerating +0.017 px/f^2 (frames 4-60) with a +-111 deg/f heading step (-9933).
    // N drops 8 claws at +-128 px (724-792), H/L 6 claws at +-192 px (794-856), Overdrive doubles
    // the H spawns (858-943). Ported as familiars with sweeping aim windows.
    private static DanmakuPattern poisonClaws(boolean hard, boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var clawBullet = shot(BACILLUS, 0.03f, MAGENTA, 120,
                MotionSegment.setSpeed(ticks(60), 0.16f));
        int claws = overdrive ? 8 : (hard ? 6 : 4);
        float span = hard ? 10f : 7f;
        for (int k = 0; k < claws; k++) {
            float x = span * (k % 2 == 0 ? -1 : 1) * (0.5f + 0.25f * (k / 2));
            float z = -4f + 2.2f * (k / 2);
            var stream = fire(EmitShape.fan(hard ? 2 : 1, 2f), clawBullet, 0, 290, 3)
                    .withAim(EmitAim.TARGET, 0, 0);
            var pattern = new DanmakuPattern(List.of(sweep(stream, 0, 0f, (k % 2 == 0 ? -1 : 1) * 700f)));
            familiars.add(DanmakuFamiliar.of(new Vec3(x, 0.2, z), pattern)
                    .withDelay(ticks(60) + (k / 2) * ticks(75)).withDuration(ticks(290)));
        }
        var cover = shot(PELLET, 0.12f, MAGENTA, 160);
        emitters.add(fire(EmitShape.ring(12, 0f, true), cover, 90, 0, 1));
        var pattern = new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(3000), ticks(60), ticks(378)));
        return pattern;
    }

    // st03bs BossCard3 (944-1116): every ~305f cycle fires a ring of growing laser count
    // (8 -> 24, ins_700/701/711 with 14 px width, 64f duration, -0.09 rad sweep, BossCard3_at
    // 1001-1023); H/L doubles the ring offset by half a step (at_h 1083-1116). Between cycles
    // 11 volleys of small accelerating aimed clusters rise from 32 px rings (BossCard3_at3
    // 1050-1081, counts 3/8/8/12 growing +1/+2). Desire-spirit item drops (@at2) are decorative.
    private static DanmakuPattern desireHarvest(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var lasers2 = new ArrayList<DanmakuLaser>();
        for (int cycle = 0; cycle < 5; cycle++) {
            int count = Math.min(8 + cycle * 4, 24);
            int at = ticks(90) + cycle * ticks(305);
            for (int k = 0; k < count; k++) {
                float yaw = 360f * k / count;
                lasers.add(laser(at, 0f, yaw, 12f, 0.9f, ticks(40), ticks(64), -1.7f, MAGENTA));
                if (hard && k + 1 <= count) {
                    lasers2.add(laser(at, 0f, yaw + 180f / count, 12f, 0.9f, ticks(40), ticks(64), -1.7f, WHITE));
                }
            }
            var cluster = shot(PELLET, 0.02f, WHITE, 140,
                    MotionSegment.setSpeed(ticks(120), 0.15f));
            for (int v = 0; v < 11; v++) {
                var cue = fire(EmitShape.ring(Math.min(3 + cycle * 2, 12), 0f, true), cluster,
                        at + v * 3, 0, 1).withAim(EmitAim.TARGET, 0, 0);
                emitters.add(cue.withOffset(new Vec3(0, 0.2, 0)));
            }
        }
        lasers.addAll(lasers2);
        return timeline(emitters, lasers, 2520);
    }
}
