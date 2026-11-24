package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** TH12 stage06.ecl: Hijiri Byakuren's Normal and Hard spell-practice entries. */
final class Stage06 {
    private static final String BYAKUREN = "item.reverie_dreams.spellcard.character.hijiri_byakuren";
    private static final Identifier BUTTERFLY = ReverieDreams.id("butterfly");
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BACILLUS = ReverieDreams.id("bacillus");
    private static final int VIOLET = 0x9A60D7;
    private static final int ROSE = 0xE46E9F;
    private static final int BLUE = 0x648DDD;
    private static final int GOLD = 0xDDBC61;
    private static final List<Vec3> FOUR_SOURCES = List.of(
            stageOffset(-114, 54), stageOffset(114, 54), stageOffset(-64, -80), stageOffset(64, -80));

    private Stage06() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(6, 79, false, BYAKUREN, "魔法「紫云的征兆」", 2700, () -> purpleCloud(false)),
                card(6, 80, true, BYAKUREN, "吉兆「紫色的云路」", 2100, () -> purpleCloud(true)),
                card(6, 83, false, BYAKUREN, "魔法「魔界蝶的妖香」", 2700, () -> magicButterfly(false)),
                card(6, 84, true, BYAKUREN, "魔法「魔法蝴蝶」", 2700, () -> magicButterfly(true)),
                card(6, 87, false, BYAKUREN, "光魔「星之漩涡」", 3000, () -> stellarMaelstrom(false)),
                card(6, 88, true, BYAKUREN, "光魔「魔法银河系」", 3000, () -> stellarMaelstrom(true)),
                card(6, 91, false, BYAKUREN, "大魔法「魔神复诵」", 4200, () -> DemonRecitation.pattern(false, FOUR_SOURCES)),
                card(6, 92, true, BYAKUREN, "大魔法「魔神复诵」", 4200, () -> DemonRecitation.pattern(true, FOUR_SOURCES)),
                card(6, 94, false, BYAKUREN, "「圣尼公的空气卷轴」", 4800, () -> airScroll(false)),
                card(6, 95, true, BYAKUREN, "「圣尼公的空气卷轴」", 4800, () -> airScroll(true)),
                card(6, 98, false, BYAKUREN, "飞钵「飞行幻想曲」", 7200, () -> flyingBowl(false)),
                card(6, 99, true, BYAKUREN, "飞钵「传说的飞空圆盘」", 7200, () -> flyingBowl(true)));
    }

    private static Vec3 stageOffset(int x, int depth) {
        return new Vec3(x / 30.0, 0.18, depth / 30.0);
    }

    // BossCard1_at1/_at2 shrink the source radius as they step by 9 degrees.
    // Hard adds at1h in the opposite direction. Sub-tick ECL shots are sampled.
    private static DanmakuPattern purpleCloud(boolean hard) {
        var emitters = new ArrayList<DanmakuEmitter>();
        addCloud(emitters, 80, 180, -1, false);
        addCloud(emitters, 40, -90, -1, true);
        if (hard) addCloud(emitters, 80, 180, 1, false);
        return loop(emitters, hard ? 2100 : 2700, 120, 300);
    }

    private static void addCloud(List<DanmakuEmitter> output, int slowWaves, float openingYaw, int turn, boolean reverses) {
        var bullet = shot(reverses ? BACILLUS : BALL, reverses ? 0.16f : 0.28f,
                reverses ? ROSE : VIOLET, 125, MotionSegment.accelerate(1, 0.003f));
        var layers = EmitShape.fan(4, reverses ? 17f : 23f);
        for (int wave = 0; wave < slowWaves + 40; wave += 2) {
            int fastWave = Math.max(0, wave - slowWaves);
            int sourceFrame = 2 * Math.min(slowWaves, wave) + fastWave;
            int winding = Math.min(slowWaves, wave) + (reverses ? -fastWave : fastWave);
            float yaw = openingYaw + turn * winding * 9f;
            double radius = (80 - wave) / 29.0;
            Vec3 offset = DanmakuMath.direction(0, yaw).scale(radius).add(0, reverses ? 0.68 : 0.19, 0);
            output.add(sweep(fire(layers, bullet, 120 + sourceFrame, 0, 3), 0, yaw, yaw).withOffset(offset));
        }
    }

    // BossEyes2 uses four butterfly emitters at two turn rates. The other
    // callback fires sixteen laser headings spaced by 22.5 degrees.
    private static DanmakuPattern magicButterfly(boolean hard) {
        final int deadline = 2700;
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        var butterfly = shot(BUTTERFLY, hard ? 0.29f : 0.23f, ROSE, 107);
        for (int i = 0; i < FOUR_SOURCES.size(); i++) {
            float turn = (i % 2 == 0 ? 1 : -1) * (i < 2 ? 1.8f : 1.2f);
            var cue = fire(EmitShape.fan(hard ? 3 : 1, 32.7f), butterfly, 190, deadline - 191, hard ? 9 : 10)
                    .withOffset(FOUR_SOURCES.get(i));
            emitters.add(sweep(cue, 0, 0, turn * (deadline - 190)));
        }
        for (int frame = 190; frame < deadline; frame += 100) {
            for (int ray = 0; ray < 16; ray++) {
                lasers.add(laser(ticks(frame), 0, ray * 22.5f, 18f, 0.55f, 20, 15, 0f, BLUE));
            }
        }
        return timeline(emitters, lasers, deadline);
    }

    // BossCard3_at2/at2b interleave four lanes and then four offset lanes.
    // The moving curves are adapted to short sweeping beams.
    private static DanmakuPattern stellarMaelstrom(boolean hard) {
        final int deadline = 3000;
        var emitters = new ArrayList<DanmakuEmitter>();
        var lasers = new ArrayList<DanmakuLaser>();
        for (int side : new int[]{-1, 1}) {
            for (int i = 0, frame = 120 + (side < 0 ? 40 : 0); frame < deadline; i++, frame += hard ? 12 : 14) {
                int lane = (i / 4) % 2 + 2 * (i % 4);
                var offset = new Vec3(side * 6.1, 0.2, (lane - 3.5) * 0.95);
                lasers.add(laser(ticks(frame), -2, 52f * side, 13f, 0.43f,
                        11, 24, -0.65f * side, VIOLET).withOffset(offset));
            }
        }
        int interval = 200;
        var star = shot(STAR, 0.28f, GOLD, 114);
        for (int frame = 420; frame < deadline; frame += interval, interval = Math.max(60, interval - 5)) {
            for (Vec3 offset : FOUR_SOURCES) {
                emitters.add(fire(EmitShape.aim(), star, frame, 0, 3).withOffset(offset));
            }
        }
        return timeline(emitters, lasers, deadline);
    }

    // BossCard5_at interpolates the sprint endpoints at 1/60 steps, with a
    // 12-degree progression and delayed acceleration.
    private static DanmakuPattern airScroll(boolean hard) {
        final int deadline = 4800;
        var emitters = new ArrayList<DanmakuEmitter>();
        var marker = shot(BALL, 0.10f, ROSE, 129, MotionSegment.setSpeed(0, 0),
                MotionSegment.accelerate(54, 0.007f));
        Vec3 previous = new Vec3(4.7, 0.5, 0);
        int frame = 120;
        while (frame < deadline) {
            for (int phase = 0; phase < 4; phase++) {
                for (int sprint = 0; sprint < 8; sprint++) {
                    frame += Math.max(10, (hard ? 40 : 80) - 10 * sprint);
                    if (frame >= deadline) return timeline(emitters, deadline);
                    Vec3 next = scrollPoint(phase, sprint);
                    for (int sample = 0; sample <= 60; sample += 3) {
                        float yaw = sample * (sprint % 2 == 0 ? 12f : -12f);
                        var cue = sweep(fire(EmitShape.aim(), marker, frame, 0, 3), -3, yaw, yaw);
                        emitters.add(cue.withOffset(next.lerp(previous, sample / 60.0)));
                    }
                    previous = next;
                }
                frame += (hard ? 90 : 120) + 60;
            }
        }
        return timeline(emitters, deadline);
    }

    private static Vec3 scrollPoint(int phase, int sprint) {
        return switch (phase) {
            case 0 -> new Vec3((sprint % 2 == 0 ? -1 : 1) * 4.7, 0.5, -3.2 + sprint * 0.92);
            case 1 -> new Vec3(3.75 - sprint * 1.23, 0.7, sprint % 2 == 0 ? -3.5 : 4.0);
            case 2 -> new Vec3((sprint % 2 == 0 ? 1 : -1) * 4.7, 0.5, 3.25 - sprint * 0.92);
            default -> new Vec3(-3.75 + sprint * 1.23, 0.7, sprint % 2 == 0 ? 4.0 : -3.5);
        };
    }

    // BossCard6 alternates four sources, curving neighboring rings in opposite
    // directions. HP gates use elapsed time. The 36-second threshold is from ECL.
    private static DanmakuPattern flyingBowl(boolean hard) {
        final int deadline = 7200;
        int[] boundaries = {120, 1440, 2580, 3720, 4620, 5040, deadline};
        int[] periods = {300, 250, 230, 200, 150, 150};
        int[] counts = hard ? new int[]{24, 24, 26, 26, 26, 32} : new int[]{24, 20, 22, 26, 26, 32};
        int[] colors = {BLUE, VIOLET, GOLD, BLUE, ROSE, ROSE};
        var emitters = new ArrayList<DanmakuEmitter>();
        for (int phase = 0; phase < periods.length; phase++) {
            for (int frame = boundaries[phase], wave = 0; frame < boundaries[phase + 1]; frame += periods[phase], wave++) {
                for (int i = 0; i < FOUR_SOURCES.size(); i++) {
                    int direction = (i + i / 2 + wave) % 2 == 0 ? -1 : 1;
                    var orb = shot(BALL, hard ? 0.21f : 0.19f, colors[phase], 164,
                            MotionSegment.setSpeed(12, 0.12f),
                            MotionSegment.rotate(19, direction * (hard ? 4.91f : 4.5f)),
                            MotionSegment.rotate(39, direction * (hard ? 1.02f : 0.94f)),
                            MotionSegment.setSpeed(39, 0.28f));
                    var cue = fire(EmitShape.ring(counts[phase], 0.5f, true), orb, frame, 0, 3)
                            .withOffset(FOUR_SOURCES.get(i));
                    float yaw = wave * 17f + i * 21f;
                    emitters.add(sweep(cue, 0, yaw, yaw));
                }
            }
        }
        return timeline(emitters, deadline);
    }
}
