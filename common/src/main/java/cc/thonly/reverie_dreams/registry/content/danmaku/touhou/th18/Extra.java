package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th18.Th18PatternSupport.*;

/** Japanese TH18 st07mbs.ecl and st07bs.ecl. */
final class Extra {
    private static final int PURPLE = 0xA16DDD;
    private static final int CYAN = 0x63BCE6;
    private static final int GREEN = 0x73C977;
    private static final int ORANGE = 0xEA9E66;
    private static final int RED = 0xDC5965;

    private Extra() {}

    static List<TouhouSpell> spells() {
        return Stream.concat(ExtraMidboss.spells().stream(), Stream.of(
                card(88, "蛊毒「食虫同类相残」", 4200, Extra::cannibalisticInsect),
                card(89, "蛊毒「洞穴群虫」", 4200, Extra::caveSwarmer),
                card(90, "蛊毒「空中蜈蚣」", 4200, Extra::skyPendra),
                card(91, "采掘「不断堆积的矿山废料」", 4800, Extra::mineDump),
                card(92, "采掘「矿山爆破」", 4800, Extra::mineBlast),
                card(93, "采掘「妖怪们的盾构法」", 4800, Extra::shieldMethod),
                card(94, "大蜈蚣「食蛇者」", 4800, Extra::snakeEater),
                card(95, "大蜈蚣「食龙者」", 5940, Extra::dragonEater),
                card(96, "「蛊毒的美食家」", 4800, Extra::kodokuGourmet),
                card(97, "「虫姬大人光辉而不得安宁的每一天」", 9000, Extra::princess))).toList();
    }

    private static TouhouSpell card(int number, String name, int frames, Supplier<DanmakuPattern> pattern) {
        return TouhouSpell.implemented(String.format(Locale.ROOT, "touhou/th18/s07_%03d_extra", number),
                name, frames, pattern).withCharacter("entity.reverie_dreams.himemushi_momoyo");
    }

    // BossCard1_at: twenty radial directions with three consecutive curved speed phases.
    private static DanmakuPattern cannibalisticInsect() {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int side : new int[]{1, -1}) {
            var insect = shot("bacillus", 0.09f, side > 0 ? CYAN : PURPLE, 156, 0.0013f,
                    MotionSegment.rotate(1, side * 3f), MotionSegment.setSpeed(27, 0.25f),
                    MotionSegment.rotate(28, side * 3f), MotionSegment.setSpeed(60, 0.12f),
                    MotionSegment.rotate(61, -side * 6f), MotionSegment.setSpeed(86, 0.28f));
            for (int wave = 0; wave < 15; wave++) {
                emitters.add(fire(EmitShape.ring(20, 0.65f, true), insect,
                        (side > 0 ? 94 : 259) + wave * 3, side * wave * 9f, side * 5f));
            }
        }
        return loop(emitters, 4200, 94, 330);
    }

    // BossCard2_Et: three separated sources lay streams that bend away from the cave entrance.
    private static DanmakuPattern caveSwarmer() {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int source = 0; source < 3; source++) {
            var insect = shot("ball", 0.10f, source == 1 ? ORANGE : GREEN, 139, 0.001f,
                    MotionSegment.rotate(1, source == 1 ? -4.5f : 4.5f),
                    MotionSegment.setSpeed(20, 0.20f));
            for (int wave = 0; wave < 40; wave++) {
                emitters.add(fire(EmitShape.aim(), insect, 90 + source * 12 + wave * 3,
                        source * 70f + wave * 9f, -9 + source * 9)
                        .withOffset(new Vec3((source - 1) * 3.3, 0.4 + source * 0.45, 1.3)));
            }
        }
        return loop(emitters, 4200, 90, 420);
    }

    // BossCard3_at: sequence of moving laser heads, with a narrow three-way tail after the transition.
    private static DanmakuPattern skyPendra() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var tail = shot("water_drop", 0.31f, CYAN, 106, MotionSegment.setSpeed(24, 0.22f));
        for (int frame = 90, index = 0; frame < 4200; frame += 20, index++) {
            float yaw = (float) (70 * Math.sin(index * 0.21));
            lasers.add(beam(frame, -6f, yaw, 13f, 0.48f, 24, 48, CYAN));
            if (frame >= 305) emitters.add(fire(EmitShape.fan(3, 10f), tail, frame, -yaw, 7f));
        }
        return loop(emitters, 4200, 0, 0).withLasers(lasers);
    }

    // BossCard4_at alternates slow fragments from two sides, collecting them in layered curtains.
    private static DanmakuPattern mineDump() {
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int side : new int[]{1, -1}) {
            var rubble = shot("needle", 0.075f, side > 0 ? RED : ORANGE, 174, 0.0013f,
                    MotionSegment.freeze(27, 14), MotionSegment.setSpeed(41, 0.18f));
            for (int frame = 90, wave = 0; frame < 4800; frame += 8, wave++) {
                double x = (wave % 12) * 0.47;
                emitters.add(fire(EmitShape.aim(), rubble, frame, side * 35f, 24f)
                        .withJitter(10f).withOffset(new Vec3(side * x, 3.3 + wave % 4 * 0.22, 2.5)));
            }
        }
        return loop(emitters, 4800, 0, 0);
    }

    // BossCard5_at lays five explosives; BossCard5_et produces distinct bright, dark and small fragments.
    private static DanmakuPattern mineBlast() {
        var mine = shot("water_drop", 0.30f, ORANGE, 104, MotionSegment.freeze(24, 20),
                MotionSegment.split(42, shrapnel("star", CYAN, 10, 138f, 0.23f)),
                MotionSegment.split(44, shrapnel("ball", PURPLE, 7, 118f, 0.19f)),
                MotionSegment.split(45, shrapnel("pellet", RED, 14, 160f, 0.32f)));
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int wave = 0; wave < 3; wave++) {
            emitters.add(fire(EmitShape.fan(5, 80f), mine, 90 + wave * 30, wave * 22f - 22f, -7f));
        }
        return loop(emitters, 4800, 90, 550);
    }

    // BossCard6_at constructs a five-fold shield while BossCard6_at2/3 add thirty-way rings and loose beams.
    private static DanmakuPattern shieldMethod() {
        var lasers = new ArrayList<DanmakuLaser>();
        var emitters = new ArrayList<DanmakuEmitter>();
        var stars = shot("star", 0.22f, PURPLE, 115, MotionSegment.setSpeed(26, 0.14f));
        for (int frame = 90, cycle = 0; frame < 4800; frame += 180, cycle++) {
            for (int edge = 0; edge < 5; edge++) {
                lasers.add(beam(frame, 0f, edge * 72f + cycle * 24f, 14f, 0.58f, 45, 90, CYAN));
            }
            emitters.add(fire(EmitShape.ring(30, 1f, true), stars, frame + 60, cycle * 19f, 0));
            for (int lane = 0; lane < 7 && frame + 80 + lane * 6 < 4800; lane++) {
                lasers.add(beam(frame + 80 + lane * 6, 8f - lane * 2.1f, 180 + lane * 22f,
                        11f, 0.38f, 33, 36, RED));
            }
        }
        return loop(emitters, 4800, 0, 0).withLasers(lasers);
    }

    // BossCard7_at: three distinct beam colors, with a 64-way backing burst.
    private static DanmakuPattern snakeEater() {
        var lasers = new ArrayList<DanmakuLaser>();
        var emitters = new ArrayList<DanmakuEmitter>();
        var ring = shot("ball", 0.25f, PURPLE, 101, MotionSegment.setSpeed(31, 0.15f));
        int[] colors = {RED, GREEN, PURPLE};
        for (int frame = 150, wave = 0; frame < 4800; frame += 60, wave++) {
            float yaw = (wave % 3 - 1) * 42f;
            lasers.add(beam(frame, 0f, yaw, 22f, 1.1f, 45, 78, colors[wave % 3]));
            if (wave % 3 == 0) emitters.add(fire(EmitShape.ring(64, 1.4f, true), ring, frame, yaw, -3f));
        }
        return loop(emitters, 4800, 0, 0).withLasers(lasers);
    }

    // BossCard8_et: five moving sources leave stationary pellets on a golden-angle
    // sequence and finish by releasing them along the same 3.883222-radian sequence.
    private static DanmakuPattern dragonEater() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bead = shot("bacillus", 0.025f, CYAN, 148, MotionSegment.freeze(12, 17),
                MotionSegment.setSpeed(29, 0.27f));
        for (int phase = 0; phase < 2; phase++) {
            int side = phase == 0 ? 1 : -1;
            for (int source = 0; source < 5; source++) {
                for (int wave = 0; wave < 26; wave++) {
                    float yaw = (float) (wave * Math.toDegrees(3.883222) + source * 72f);
                    double angle = Math.toRadians(yaw);
                    Vec3 origin = new Vec3(side * (source - 2) * 1.5 + Math.sin(angle) * 0.65,
                            0.65 + source * 0.4, 0.15 + wave * 0.18 + Math.cos(angle) * 0.65);
                    emitters.add(fire(EmitShape.aim(), bead, 120 + phase * 210 + wave * 3, yaw, 3f)
                            .withOffset(origin));
                }
            }
        }
        return loop(emitters, 5940, 120, 420);
    }

    // BossCard9 is a timed survival attack. A slow perimeter curtain and periodic concentrated cuts overlap.
    private static DanmakuPattern kodokuGourmet() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var curtain = shot("rice", 0.13f, RED, 175, 0.0014f, MotionSegment.rotate(1, 2.1f));
        var cut = shot("needle", 0.31f, PURPLE, 105, MotionSegment.setSpeed(22, 0.19f));
        for (int frame = 100, wave = 0; frame < 4800; frame += 10, wave++) {
            double angle = wave * Math.PI / 18;
            Vec3 from = new Vec3(4.0 * Math.sin(angle), 0.4 + 1.55 * Math.abs(Math.cos(angle)), 2.4);
            emitters.add(fire(EmitShape.aim(), curtain, frame, (float) Math.toDegrees(angle) + 180, -5f)
                    .withOffset(from).withJitter(23f));
        }
        for (int frame = 440, wave = 0; frame < 4800; frame += 200, wave++) {
            emitters.add(fire(EmitShape.fan(11, 64f), cut, frame, wave * 90f, -6f));
        }
        return loop(emitters, 4800, 0, 0);
    }

    // BossCard10_wait can switch on HP or time. A held card has no encounter HP,
    // so the same five phases use the ECL time thresholds.
    private static DanmakuPattern princess() {
        var emitters = new ArrayList<DanmakuEmitter>();
        int[] frames = {90, 2100, 4200, 6300, 7800, 9000};
        int[] layers = {3, 5, 7, 12, 12};
        int[] steps = {20, 20, 24, 20, 15};
        for (int phase = 0; phase < layers.length; phase++) {
            for (int side : new int[]{-1, 1}) {
                var bead = shot("bacillus", 0.16f, side > 0 ? RED : PURPLE, 113, 0.001f,
                        MotionSegment.rotate(1, side * 4.7f),
                        MotionSegment.setSpeed(48, 0.22f));
                for (int frame = frames[phase], wave = 0; frame < frames[phase + 1]; frame += steps[phase], wave++) {
                    var shape = EmitShape.multiRing(layers[phase], 3, 0.55f, true, side * 5f, 0.15f, 0.08f);
                    emitters.add(fire(shape, bead, frame, side * wave * 9f, -5f + phase * 2)
                            .withOffset(new Vec3(side * 1.1, 0.6, 0)));
                }
            }
        }
        return loop(emitters, 9000, 0, 0);
    }

    private static DanmakuLaser beam(int frame, float pitch, float yaw, float length, float width, int warning, int active, int color) {
        return new DanmakuLaser(ticks(frame), pitch, yaw, length, width, ticks(warning), ticks(active), 0f, color);
    }

    private static MotionPayload shrapnel(String type, int color, int count, float spread, float speed) {
        return MotionPayload.split(ReverieDreams.id(type), color, 0.77f, count, spread, speed, 100);
    }
}
