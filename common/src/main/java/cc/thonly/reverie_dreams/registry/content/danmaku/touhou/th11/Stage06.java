package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/**
 * TH11 stage06mboss.ecl and stage06boss.ecl. Numbers are one-based practice entries.
 * Durations are the setInterrupt values: MBossCard1 2220 (Boss2/MBossDead),
 * BossCard1 3600 (Boss2), BossCard2 3600 (Boss3), BossCard3 4200 (Boss4),
 * BossCard4 4200 (Boss5), BossCard5 8400 (BossDead).
 */
final class Stage06 {
    private static final String RIN = "kaenbyou_rin";
    private static final String UTSUHO = "reiuji_utsuh";
    private static final Identifier FIREBALL = ReverieDreams.id("fireball");
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final int RED = 0xE05260;
    private static final int EMBER = 0xF08A2C;
    private static final int BLUE = 0x5D8ADB;
    private static final int GREEN = 0x62D958;
    private static final int WHITE = 0xF0EBF5;

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(6, 140, false, RIN, "妖怪「火焰的车轮」", 2220, () -> blazingWheel(false)),
                card(6, 141, true, RIN, "妖怪「火焰的车轮」", 2220, () -> blazingWheel(true)),
                card(6, 144, false, UTSUHO, "核热「Nuclear Fusion」（核聚变）", 3600, () -> nuclearFusion(false)),
                card(6, 145, true, UTSUHO, "核热「Nuclear Excursion」（核功率骤增）", 3600, () -> nuclearFusion(true)),
                card(6, 148, false, UTSUHO, "爆符「Mega-Flare」（百万耀斑）", 3600, () -> megaFlare(false)),
                card(6, 149, true, UTSUHO, "爆符「Giga-Flare」（十亿耀斑）", 3600, () -> megaFlare(true)),
                card(6, 152, false, UTSUHO, "焰星「Fixed Star」（恒星）", 4200, () -> fixedStar(false)),
                card(6, 153, true, UTSUHO, "焰星「Planetary Revolution」（行星公转）", 4200, () -> fixedStar(true)),
                card(6, 156, false, UTSUHO, "「地狱极乐熔毁」", 4200, () -> meltdown(false)),
                card(6, 157, true, UTSUHO, "「Hell's Tokamak」（地狱的托卡马克装置）", 4200, () -> meltdown(true)),
                card(6, 160, false, UTSUHO, "「地狱的人造太阳」", 8400, () -> subterraneanSun(false)),
                card(6, 161, true, UTSUHO, "「Subterranean Sun」（地底太阳）", 8400, () -> subterraneanSun(true)));
    }

    /**
     * MBossCard1 (stage06mboss L39): setInterrupt 2220 to Boss2, then MBossDead. After
     * a 90-frame intro (+30/+60), each cycle runs two async MBossCard1_at streams with
     * wait($E) between them. MBossCard1_at (L81) loops $D shots on an 8-frame cadence;
     * bullets spin at 1/60 rad per frame (etEx 0.016666668) with a 30-frame transform
     * (etEx 4096/30), so each stream rolls like a wheel. Hard tightens the cycle and
     * widens the rim.
     */
    private static DanmakuPattern blazingWheel(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int interval = hard ? 140 : 180;
        for (int cycle = 0, start = 90; start < 2220; cycle++, start += interval) {
            // rolling rim: rotated stacked rings stand in for the spinning wheel edge
            var rim = shot(BALL, hard ? 0.26f : 0.22f, hard ? EMBER : RED, 110);
            float rimPhase = cycle * 47f % 360f;
            shots.add(sweep(fire(EmitShape.multiRing(hard ? 5 : 4, hard ? 18 : 14, 1.8f, true,
                    9f, 0.35f, 0.05f), rim, start, 0, 3)
                    .withOffset(new Vec3(0, 1.15f, 0)), 0f, rimPhase, rimPhase));
            // two async MBossCard1_at streams: 8-frame spoke cadence, phase stepping
            // in place of the 1/60-rad-per-frame curvature
            for (int wave = 0; wave < 2; wave++) {
                for (int spoke = 0; spoke < (hard ? 10 : 7); spoke++) {
                    int frame = start + wave * 48 + spoke * 8;
                    float phase = (cycle * 47f + spoke * 12.5f + wave * 180f) % 360f;
                    var bullet = shot(BULLET, hard ? 0.26f : 0.22f, hard ? RED : EMBER, 105);
                    shots.add(sweep(fire(EmitShape.fan(3, 24f), bullet, frame, 0, 3)
                            .withOffset(new Vec3(wave == 0 ? 1.4f : -1.4f, 1.15f, 0)),
                            0f, phase, phase));
                }
            }
        }
        return timeline(shots, 2220);
    }

    /**
     * BossCard1 (stage06boss L489): setInterrupt 3600 to Boss2. The main stream comes
     * from BossCard1At (L598) and BossCard1At2 (L624): aimed 3-way volleys (etCount
     * 3,1) with 1/64-rad angle jitter and speeds sampled between 1.2 and 1.7, fired in
     * 210-shot blocks (diffI $C 35/210/210/210) on a 6/1-frame cadence with an 80-frame
     * gap (diffWait 80/80/80/75). BossCard1Bomb (L650) fuses pop below the boss (offset
     * 0,-32) every 120 frames: eight waves on Normal, four heavier ones on Hard
     * (diffI $E 8/8/4/8), each cleared by BossCard1BombErase after a 20-frame wait.
     */
    private static DanmakuPattern nuclearFusion(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int cadence = hard ? 3 : 6;
        int frame = 120;
        int blockIndex = 0;
        while (frame < 3600) {
            // one 210-shot block of aimed 3-way fire
            for (int i = 0; i < 210 && frame < 3600; i++, frame += cadence) {
                float phase = (blockIndex * 37f + i * 4.5f) % 360f;
                var bullet = shot(BULLET, 0.24f, RED, 100);
                var emitter = fire(EmitShape.fan(3, 14f), bullet, frame, 0, 3)
                        .withOffset(new Vec3(1.6f, 0.40f, 0))
                        .withScatter(new DanmakuScatter(0, 0, 0, 0.71f));
                shots.add(sweep(emitter, 0f, phase, phase));
            }
            frame += 80;
            blockIndex++;
        }
        // BossCard1Bomb fuses: enmCreate every 120 frames from the +120 cue
        int fuseWaves = hard ? 4 : 8;
        int fuseCount = hard ? 24 : 14;
        for (int fuse = 0; fuse < fuseWaves; fuse++) {
            int fuseFrame = 240 + fuse * 120;
            if (fuseFrame >= 3600) {
                break;
            }
            float fusePhase = fuse * 13.7f % 360f;
            var bullet = shot(FIREBALL, 0.30f, EMBER, 115);
            shots.add(sweep(fire(EmitShape.ring(fuseCount, 0.4f, true), bullet,
                    fuseFrame + 60, 0, 3).withOffset(new Vec3(0, 0.35f, 0)),
                    0f, fusePhase, fusePhase + 22.5f));
        }
        return timeline(shots, 3600);
    }

    /**
     * BossCard2 (L707): setInterrupt 3600 to Boss3. BossCard2at (L859) queues five cues
     * spaced diffWait(15,15,14,13) frames, and the card repeats after wait(60) plus
     * wait(340). BossCard2Bomb (L754) looses aimed shots at fully random angles
     * (etAngle %RANDRAD) in 1-frame $C loops paused 25 frames; two loops run per pause
     * on Hard. Mega-Flare reads the five-cue cadence with 16-way flares; Giga-Flare
     * tightens the cadence, widens the flares and doubles the bomb rain.
     */
    private static DanmakuPattern megaFlare(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int cue = hard ? 14 : 15;
        int count = hard ? 22 : 16;
        for (int cycle = 0, start = 120; start < 3600; cycle++, start += 400) {
            // five BossCard2at cues: nested flares one cue apart
            for (int burst = 0; burst < 5; burst++) {
                int frame = start + burst * cue;
                float phase = (cycle * 23f + burst * 72f) % 360f;
                var bullet = shot(FIREBALL, hard ? 0.34f : 0.28f, EMBER, 110);
                shots.add(sweep(fire(EmitShape.multiRing(2, count, 1.2f + burst * 0.5f, true,
                        11.25f, 0.45f, 0.08f), bullet, frame, 0, 3)
                        .withOffset(new Vec3(0, 1.2f, 0)), 0f, phase, phase));
            }
            // BossCard2Bomb rain: pseudo-random directions (%RANDRAD stand-in)
            int lanes = hard ? 2 : 1;
            for (int rain = 0; rain < (hard ? 12 : 8); rain++) {
                int frame = start + 200 + rain * 25;
                float phase = (cycle * 61f + rain * 97f) % 360f;
                var bullet = shot(BULLET, hard ? 0.22f : 0.18f, WHITE, 105);
                for (int lane = 0; lane < lanes; lane++) {
                    shots.add(sweep(fire(EmitShape.ring(6, 0.3f, true), bullet,
                            frame + lane * 8, 0, 3).withOffset(new Vec3(0, 2.0f, 0)),
                            0f, phase + lane * 30f, phase + lane * 30f));
                }
            }
        }
        return timeline(shots, 3600);
    }

    /**
     * BossCard3 (L878): setInterrupt 4200 to Boss4. The shared BossCard3At (L935) is a
     * star-sprite spiral (etSprite 1/13) offset (0,-32) whose branches curve at
     * +/-0.0062831854 rad per frame — two counter-rotating arms — on a 15-frame cadence
     * with 1/64-rad jitter; the port keeps every second volley to bound entities.
     * Normal seats its BossCard3At1 (L974) helpers at fixed points (Fixed Star), Hard
     * uses BossCard3At1h (L1007) with the helpers revolving (Planetary Revolution).
     * Both run four diffI $B loops of two BossCard3Bomb spawns with a 15-frame gap.
     */
    private static DanmakuPattern fixedStar(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int cadence = hard ? 24 : 30;
        for (int volley = 0, start = 240; start < 4200; volley++, start += cadence) {
            float step = hard ? 7.2f : 5.4f;
            for (int arm = 0; arm < 2; arm++) {
                float phase = arm == 0 ? volley * step : -volley * step;
                var bullet = shot(STAR, hard ? 0.28f : 0.24f, hard ? EMBER : RED, 110);
                shots.add(sweep(fire(EmitShape.ring(hard ? 12 : 8, 1.6f, true), bullet,
                        start, 0, 3).withOffset(new Vec3(0, 0.35f, 0)), 0f, phase, phase));
            }
        }
        // four $B loops x two BossCard3Bomb spawns; fixed seats on Normal, revolving
        // seats on Hard
        for (int loop = 0; loop < 4; loop++) {
            for (int bomb = 0; bomb < 2; bomb++) {
                float angle = hard ? (loop * 2 + bomb) * 45f + loop * 30f
                        : bomb == 0 ? 45f : 225f;
                double radians = Math.toRadians(angle);
                var offset = new Vec3(Math.cos(radians) * 2.6, 1.4, Math.sin(radians) * 2.6);
                for (int burst = 0; burst < 3; burst++) {
                    int frame = 300 + loop * 220 + bomb * 15 + burst * 12;
                    var bullet = shot(BALL, 0.20f, hard ? GREEN : BLUE, 105);
                    float phase = burst * 15f + bomb * 90f;
                    shots.add(sweep(fire(EmitShape.ring(12, 0.6f, true), bullet,
                            frame, 0, 3).withOffset(offset), 0f, phase, phase));
                }
            }
        }
        return timeline(shots, 4200);
    }

    /**
     * BossCard4 (L1090): setInterrupt 4200 to Boss5. BossCard4At (L1157) fires 16-way
     * volleys (etCount 16,1) offset (0,-32) curving at -0.0062831854 rad per frame on a
     * 15-frame cadence; the port keeps every second volley. Two BossCard4Bomb helpers
     * (L1183) flank the boss, and their output differs by difficulty: Normal's
     * BossCard4BombAt (L1227) looses single aimed shots at fully random angles after
     * waits of 30 and 60 frames (meltdown rain), while Hard's BossCard4BombAth (L1254)
     * spins bullets at 0.02617994 rad per frame over 120 frames — the tokamak's rotating
     * plasma — on a 52-frame cadence (diffWait 60/60/60/52).
     */
    private static DanmakuPattern meltdown(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        // BossCard4At: 16-way curved volleys, phase stepping in for the curvature
        for (int volley = 0, start = 240; start < 4200; volley++, start += 30) {
            float phase = -volley * 6.75f;
            var bullet = shot(FIREBALL, hard ? 0.30f : 0.26f, EMBER, 110);
            shots.add(sweep(fire(EmitShape.ring(hard ? 18 : 16, 1.4f, true), bullet,
                    start, 0, 3).withOffset(new Vec3(0, 0.40f, 0)), 0f, phase, phase));
        }
        for (int side = 0; side < 2; side++) {
            var offset = new Vec3(side == 0 ? -2.4f : 2.4f, 1.6f, 0);
            if (!hard) {
                // BossCard4BombAt: random-angle singles, wait(30) then wait(60)
                for (int rain = 0, frame = 300; frame < 4200; rain++, frame += 45) {
                    float phase = (rain * 83f + side * 47f) % 360f;
                    var bullet = shot(BULLET, 0.20f, WHITE, 105);
                    shots.add(sweep(fire(EmitShape.aim(), bullet, frame, 0, 3)
                            .withOffset(offset), 0f, phase, phase));
                }
            } else {
                // BossCard4BombAth: plasma columns rotating 1.5 degrees per frame
                for (int ring = 0, frame = 300; frame < 4200; ring++, frame += 52) {
                    float phase = ring * 90f + side * 180f;
                    var bullet = shot(BALL, 0.24f, BLUE, 105);
                    shots.add(sweep(fire(EmitShape.multiRing(3, 5, 0.8f, true, 24f, 0.2f, 0.04f),
                            bullet, frame, 0, 3).withOffset(offset), 0f, phase, phase));
                }
            }
        }
        return timeline(shots, 4200);
    }

    /**
     * BossCard5 (L1307): setInterrupt 8400 to BossDead — the survival finale.
     * BossCard5At2 (L1458) is the sun: bullets leave at speed 0 and ramp to 1.5 under
     * a 3000-frame etEx transform, released in wait(20)/wait(10) cycles at angle %B.
     * BossCard5At3 (L1496) runs three BossCard5At1b helpers around it. BossCard5Bomb
     * (L1518) climbs four $BI3 stages whose BombAt/BombAt2 (L1592/L1620) place single
     * shots at offset (%B,%C) on a 3-frame cadence (diffWait 6/3/3/2). Normal builds
     * the artificial sun from expanding shells; Hard adds a counter-rotating corona
     * and a fourth sunspot stage.
     */
    private static DanmakuPattern subterraneanSun(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        for (int burst = 0, start = 300; start < 8400; burst++, start += 90) {
            // BossCard5At2 sun shell: slow rotation, rings stepping outward and faster
            float phase = burst * 5f;
            var bullet = shot(BALL, 0.24f, WHITE, 120);
            shots.add(sweep(fire(EmitShape.multiRing(2, hard ? 20 : 16, 2.4f, true,
                    11.25f, 0.9f, 0.12f), bullet, start, 0, 3)
                    .withOffset(new Vec3(0, 1.6f, 0)), 0f, phase, phase));
            if (hard && burst % 2 == 0) {
                // Hard corona: counter-rotating shell (Subterranean Sun)
                var corona = shot(BULLET, 0.28f, EMBER, 115);
                shots.add(sweep(fire(EmitShape.ring(16, 3.6f, true), corona, start + 45, 0, 3)
                        .withOffset(new Vec3(0, 1.6f, 0)), 0f, -phase, -phase));
            }
        }
        // BossCard5At3: three BossCard5At1b helpers wheeling around the sun
        for (int helper = 0; helper < 3; helper++) {
            for (int volley = 0, frame = 60; frame < 8400; volley++, frame += 45) {
                float angle = volley * 9f + helper * 120f;
                double radians = Math.toRadians(angle);
                var offset = new Vec3(Math.cos(radians) * 3.0, 1.9, Math.sin(radians) * 3.0);
                var bullet = shot(PELLET, hard ? 0.22f : 0.18f, RED, 105);
                shots.add(sweep(fire(EmitShape.aim(), bullet, frame, 0, 3)
                        .withOffset(offset), 0f, angle + 180f, angle + 180f));
            }
        }
        // BossCard5Bomb: four $BI3 stages seating sunspots at offset (%B,%C)
        for (int stage = 0; stage < 4; stage++) {
            int stageStart = 1500 + stage * 1500;
            int spots = hard ? 4 : 3;
            for (int spot = 0; spot < spots; spot++) {
                float angle = spot * (360f / spots) + stage * 30f;
                double radians = Math.toRadians(angle);
                var offset = new Vec3(Math.cos(radians) * 2.6, 1.2, Math.sin(radians) * 2.6);
                for (int volley = 0, frame = stageStart; frame < 8400; volley++, frame += 45) {
                    var bullet = shot(BULLET, 0.22f, hard ? GREEN : BLUE, 105);
                    float phase = (volley * 17f + spot * 61f) % 360f;
                    shots.add(sweep(fire(EmitShape.aim(), bullet, frame, 0, 3)
                            .withOffset(offset), 0f, phase, phase));
                }
            }
        }
        return timeline(shots, 8400);
    }
}
