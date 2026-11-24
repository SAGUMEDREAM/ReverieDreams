package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13;

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

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th13.Th13PatternSupport.*;

/**
 * TH13 st07bs.ecl: Mamizou Futatsuiwa's Extra boss battle — the ten-round transformation duel.
 * Her bullets spawn animal entities (ins_640 "BossCardN_EtAt") that spray decelerating aimed
 * shots; those spawned columns are ported as split payloads and familiars.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th13/ecl/st07bs.ecl.txt.
 */
final class Extra {
    private static final String MAMIZOU = "hutatsuiwa_mamizou";
    // Sampled from th13 bullet.anm: spr5 = 0x8B77FF violet, spr19 = 0xFF00FF magenta,
    // spr8 = 0x28FFFF cyan (animal entities), spr11 = 0x9DFF2A lime, spr32 = white.
    private static final int VIOLET = 0x8B77FF;
    private static final int MAGENTA = 0xFF00FF;
    private static final int ANIMAL_CYAN = 0x28FFFF;
    private static final int LIME = 0x9DFF2A;
    private static final int WHITE = 0xFFFFFF;

    private Extra() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(7, 109, MAMIZOU, "第一回合「灵长化弹幕变化」", 4200, Extra::primateChange),
                extraCard(7, 110, MAMIZOU, "第二回合「肉食化弹幕变化」", 4200, Extra::carnivoreChange),
                extraCard(7, 111, MAMIZOU, "第三回合「延羽化弹幕变化」", 4200, Extra::featherChange),
                extraCard(7, 112, MAMIZOU, "第四回合「两生化弹幕变化」", 4200, Extra::amphibianChange),
                extraCard(7, 113, MAMIZOU, "第五回合「鸟兽戏画」", 4200, Extra::chojugiga),
                extraCard(7, 114, MAMIZOU, "第六回合「狸猫变身学校」", 4200, Extra::tanukiSchool),
                extraCard(7, 115, MAMIZOU, "第七回合「野生之离岛」", 4200, () -> wildIsland(false)),
                extraCard(7, 126, MAMIZOU, "「狂野飞毯」", 4200, () -> wildIsland(true)),
                extraCard(7, 116, MAMIZOU, "变化「笨拙巫女的伪调伏」", 4800, Extra::fakeShrine),
                extraCard(7, 117, MAMIZOU, "「玛米藏化弹幕十变化」", 4620, Extra::tenChanges),
                extraCard(7, 118, MAMIZOU, "狢符「满月砰砰狸」", 7200, Extra::fullMoonPonpokorin));
    }

    /** The animal-spawn payload shared by the 弾幕変化 rounds: a slow aimed echo. */
    private static MotionSegment animalShot(int at) {
        return MotionSegment.split(ticks(at), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                PELLET, VIOLET, 0.8f, 0.1f, 1, 0f, 110));
    }

    // BossCard1 (986-1240): two 320f passes per cycle with the base heading flipping +-90 deg
    // (1014-1024); each pass fires an aimed [-9985]-count ring (2.2 px/f, 18 deg/f curl, at
    // 1223-1239) whose bullets spawn primate entities firing slow aimed shots (EtAt 1195+).
    private static DanmakuPattern primateChange() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var ring = shot(BALL, 0.22f, VIOLET, 190, animalShot(24));
        for (int v = 0; v < 8; v++) {
            float yaw = v % 2 == 0 ? -90f : 90f;
            var cue = fire(EmitShape.ring(16, 0f, true), ring, 120 + v * 21, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw));
        }
        return loop(emitters, 4200, 120, 150);
    }

    // BossCard2 (1241-1358): endless 1-bullet streams from +-128 px of the player's column
    // (1345-1352) at 3.2 px/f aimed with a tiny player-scaled drift, cadence 10 -> 5f; the
    // bullets become carnivore entities spraying decelerating 3.0 -> 0.6 px/f shots (EtAt_at).
    private static DanmakuPattern carnivoreChange() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var claw = shot(BULLET, 0.32f, VIOLET, 170, animalShot(16));
        for (int v = 0; v < 24; v++) {
            int at = 120 + v * Math.max(5, 10 - v / 6);
            float side = v % 2 == 0 ? -8f : 8f;
            emitters.add(fire(EmitShape.aim(), claw, at, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0).withOffset(new Vec3(side, 0.2, 0)));
        }
        return loop(emitters, 4200, 120, 100);
    }

    // BossCard3 (1360-1460): endless 16x2-count feathered rings (3.2/2.2 px/f tiers, 1447-1459)
    // aimed at the player; bullets spawn bird entities firing decelerating shots (EtAt).
    private static DanmakuPattern featherChange() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var feather = shot(BULLET, 0.32f, VIOLET, 180, animalShot(20));
        for (int v = 0; v < 20; v++) {
            emitters.add(fire(EmitShape.ring(16, 0f, true), feather, 120 + v * 9, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 4200, 120, 100);
    }

    // BossCard4 (1462-1606): seven volleys of 4-count rings (2.8/2.2 px/f) whose heading drifts
    // +-20 deg and whose lives grow 10/23/36/49/62/75/88 frames per volley (1597-1602); the
    // bullets become frogs firing V-shaped 3-shot fans (EtAt_at 1520-1575).
    private static DanmakuPattern amphibianChange() {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int v = 0; v < 7; v++) {
            var frog = shot(BALL, 0.28f, MAGENTA, 10 + 13 * v, animalShot(8));
            float yaw = 20f * v;
            var cue = fire(EmitShape.ring(4, 0f, true), frog, 180 + v * 20, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw + 20f));
        }
        return loop(emitters, 4200, 180, 140);
    }

    // BossCard5 (1608-1734): the scroll of birds and beasts — columns of animal entities march
    // in from x = +-208 px every 10f (at 1651-1675) and +-192 px with player-offset rows (at2),
    // each firing a decelerating aimed shot (EtAt_at: 3.0 -> 0.6 px/f over 20f steps).
    private static DanmakuPattern chojugiga() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var march = fire(EmitShape.aim(), shot(PELLET, 0.3f, MAGENTA, 110, MotionSegment.setSpeed(ticks(20), 0.06f)),
                0, 30, 4).withAim(EmitAim.TARGET, 0, 0);
        var marchPattern = DanmakuPattern.of(sweep(march, 0, 0f, 0f));
        for (int k = 0; k < 10; k++) {
            float x = -10.4f + 2.3f * k;
            familiars.add(DanmakuFamiliar.of(new Vec3(x, 0.2, -2.4f + 0.3f * k), marchPattern)
                    .withDelay(ticks(120) + k * ticks(10)).withDuration(ticks(320)));
        }
        for (int k = 0; k < 10; k++) {
            float x = 10.4f - 2.3f * k;
            familiars.add(DanmakuFamiliar.of(new Vec3(x, 0.2, -2.4f + 0.3f * k), marchPattern)
                    .withDelay(ticks(180) + k * ticks(10)).withDuration(ticks(320)));
        }
        var cover = shot(RICE, 0.15f, MAGENTA, 160);
        emitters.add(fire(EmitShape.ring(12, 0f, true), cover, 240, 0, 1));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(4200), ticks(120), ticks(200)));
    }

    // BossCard6 (1735-1882): the transformation school — 7 columns of leaf entities at -192 px
    // and 6 columns at -160 px, spaced 64 px (at 1857-1882), each firing a slow 2.0 px/f aimed
    // shot every 90f (EtAt_at 1834-1855). Ported as two rows of familiars.
    private static DanmakuPattern tanukiSchool() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var pop = fire(EmitShape.aim(), shot(RICE, 0.2f, VIOLET, 130), 0, 300, 30).withAim(EmitAim.TARGET, 0, 0);
        var popPattern = DanmakuPattern.of(sweep(pop, 0, 0f, 240f));
        for (int k = 0; k < 7; k++) {
            familiars.add(DanmakuFamiliar.of(new Vec3(-12f + 4f * k, 0.2, 1.2), popPattern)
                    .withDelay(ticks(180)).withDuration(ticks(3600)));
        }
        for (int k = 0; k < 6; k++) {
            familiars.add(DanmakuFamiliar.of(new Vec3(-10f + 4f * k, 0.2, 2.2), popPattern)
                    .withDelay(ticks(210)).withDuration(ticks(3600)));
        }
        var cover = shot(RICE, 0.12f, LIME, 160);
        emitters.add(fire(EmitShape.ring(10, 0f, true), cover, 180, 0, 1));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(4200), ticks(180), ticks(210)));
    }

    // BossCard7 (1884-2119): animal waves sweep the field edges — 64 + 96 column spawns per
    // wave from both sides at 2f cadence (at 1974-2021, at2 mirrors), cycling every 290f; the
    // Overdrive card (126) tightens the cycle to 10-40f and adds a second layer (at2OD).
    private static DanmakuPattern wildIsland(boolean overdrive) {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var spray = fire(EmitShape.aim(), shot(BULLET, 0.22f, VIOLET, 120, MotionSegment.setSpeed(ticks(20), 0.08f)),
                0, 40, 3).withAim(EmitAim.TARGET, 0, 0);
        var sprayPattern = DanmakuPattern.of(sweep(spray, 0, 0f, 180f));
        int rows = overdrive ? 16 : 10;
        for (int k = 0; k < rows; k++) {
            float z = -8f + 16f * k / rows;
            familiars.add(DanmakuFamiliar.of(new Vec3(-13f, 0.2, z), sprayPattern)
                    .withDelay(ticks(120) + k * (overdrive ? ticks(3) : ticks(8))).withDuration(ticks(600)));
            familiars.add(DanmakuFamiliar.of(new Vec3(13f, 0.2, z), sprayPattern)
                    .withDelay(ticks(150) + k * (overdrive ? ticks(3) : ticks(8))).withDuration(ticks(600)));
        }
        var cover = shot(RICE, 0.14f, VIOLET, 170);
        emitters.add(fire(EmitShape.ring(14, 0f, true), cover, 120, 0, 1));
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(4200), ticks(120), ticks(overdrive ? 60 : 150)));
    }

    // BossCard8 (2121-2297): the fake shrine maiden bounces across the field (move 2267-2297)
    // firing 5x5 volleys of 2-count fans with a +10 deg rotating aim (2239-2243) at 2.2 px/f,
    // then a pass of transform bullets that become the fake shrine firing 4-count accelerating
    // aimed rings (at2/EtAt2_at 2246-2224).
    private static DanmakuPattern fakeShrine() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var fan = shot(BALL, 0.22f, VIOLET, 180, animalShot(16));
        for (int v = 0; v < 25; v++) {
            float yaw = 10f * v;
            var cue = fire(EmitShape.fan(2, 3f), fan, 120 + v * 8, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw));
        }
        var gohei = shot(BULLET, 0.22f, LIME, 190, animalShot(20));
        for (int v = 0; v < 8; v++) {
            emitters.add(fire(EmitShape.ring(4, 0f, true), gohei, 260 + v * 16, 0, 1)
                    .withAim(EmitAim.TARGET, 0, 0));
        }
        return loop(emitters, 4800, 120, 140);
    }

    // BossCard9 (2299-2686): the ten-transformation survival finale — giant Mamizou entities
    // (EtAt/Bird/Dog/Human, 32 px) spawn and each fire eight accelerating rings (at2, waits
    // shrink 120 -> 60f) with special at3/at4 waves interleaved; the cycle repeats forever.
    private static DanmakuPattern tenChanges() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var familiars = new ArrayList<DanmakuFamiliar>();
        var ringShot = shot(STAR, 0.05f, VIOLET, 200, MotionSegment.setSpeed(ticks(80), 0.18f));
        var ringPattern = DanmakuPattern.of(fire(EmitShape.ring(8, 0f, true), ringShot, 0, 40, 5)
                .withAim(EmitAim.TARGET, 0, 0));
        Vec3[] spots = {new Vec3(-8, 0.2, -2), new Vec3(8, 0.2, -2), new Vec3(0, 0.2, -4), new Vec3(-4, 0.2, 1)};
        for (int k = 0; k < spots.length; k++) {
            familiars.add(DanmakuFamiliar.of(spots[k], ringPattern)
                    .withDelay(ticks(120) + k * ticks(45)).withDuration(ticks(600)));
        }
        for (int v = 0; v < 30; v++) {
            var spark = shot(PELLET, 0.16f, WHITE, 150);
            emitters.add(fire(EmitShape.aim(), spark, 120 + v * 12, 0, 1).withAim(EmitAim.TARGET, 0, 0));
        }
        return new DanmakuPattern(emitters).withFamiliars(familiars)
                .withTiming(new cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming(
                        ticks(4620), ticks(120), ticks(180)));
    }

    // BossCard10 (1032-1170): the capture-bonus full moon — 28-count double rings (1.7 px/f,
    // 18 deg/f curl, aim zig-zagging via [-9981] flip 1071) every 15-160f by boss health, the
    // second ring spawning leaf entities that dash off (EtAt 1100-1139, 1.5 px/f).
    private static DanmakuPattern fullMoonPonpokorin() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var moon = shot(BALL, 0.17f, ANIMAL_CYAN, 210, animalShot(28));
        var moonEcho = shot(BALL, 0.17f, ANIMAL_CYAN, 210, animalShot(28));
        for (int v = 0; v < 10; v++) {
            float yaw = v % 2 == 0 ? 0f : 180f;
            var cue = fire(EmitShape.ring(28, 0f, true), moon, 120 + v * 30, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue, 0, yaw, yaw + 18f * 28));
            var cue2 = fire(EmitShape.ring(28, 0f, true), moonEcho, 135 + v * 30, 0, 1).withAim(EmitAim.TARGET, 0, 0);
            emitters.add(sweep(cue2, 0, yaw, yaw + 18f * 28));
        }
        return loop(emitters, 7200, 120, 300);
    }
}
