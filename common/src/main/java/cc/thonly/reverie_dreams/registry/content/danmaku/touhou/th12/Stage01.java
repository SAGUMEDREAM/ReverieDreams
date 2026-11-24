package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** TH12 stage01.ecl: Nazrin's spell-practice entries, Normal and Hard only. */
final class Stage01 {
    private static final String NAZRIN = "nazrin";
    private static final int RED = 0xE65C60;
    private static final int BLUE = 0x597EDB;
    private static final int PURPLE = 0x8B7EE8;
    private static final int GREEN = 0x74D854;
    private static final int PALE = 0xE7DFF5;

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(1, 1, true, NAZRIN, "棒符「忙碌探宝棒」", 1920, Stage01::busyRod),
                card(1, 4, false, NAZRIN, "搜符「稀有金属探测器」", 2400, () -> detector(false)),
                card(1, 5, true, NAZRIN, "搜符「黄金探测器」", 2100, () -> detector(true)),
                card(1, 8, false, NAZRIN, "视符「娜兹玲灵摆」", 2400, () -> pendulum(false)),
                card(1, 9, true, NAZRIN, "视符「高感度娜兹玲灵摆」", 2400, () -> pendulum(true)));
    }

    // MBossCard1At: a continuous two-sided screen laser. The ECL angular increment is 0.000524 rad/frame
    // for Hard; Normal ends at 4.383618 rad, so both use the same visible rate.
    // MBossCard1At2/At2b: two curtain sweeps start at frame 120/160 and run to the deadline.
    private static DanmakuPattern busyRod() {
        var shots = new ArrayList<DanmakuEmitter>();
        var curtain = shot(BULLET, 0.32f, RED, 90, MotionSegment.accelerate(ticks(20), 0.006f));
        for (int side : new int[]{-1, 1}) {
            shots.add(spread(fire(EmitShape.aim(), curtain, 160, 1760, 3)
                    .withOffset(new Vec3(side * 5.2, 1.1, 0)), 20f, 0.8f, 0f, 0.72f));
        }
        var beams = List.of(
                laser(ticks(120), 0f, 0f, 30f, 1.0f, 10, 620, 0.090f, PURPLE),
                laser(ticks(120), 0f, 180f, 30f, 1.0f, 10, 620, -0.090f, PURPLE));
        return timeline(shots, beams, 1920);
    }

    // BossCard1_at/_atH. Normal uses sprite 7/8; Hard uses 7/13. The green 7/10 finishing sweep is shared.
    private static DanmakuPattern detector(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        int normal = hard ? PURPLE : BLUE;
        addDetectorCycle(beams, 150, normal, GREEN, hard);
        addDetectorCycle(beams, 990, hard ? BLUE : PURPLE, GREEN, hard);
        var burst = shot(BALL, 0.30f, PALE, 88, MotionSegment.accelerate(ticks(30), 0.008f));
        int frames = hard ? 2100 : 2400;
        shots.add(spread(fire(EmitShape.fan(5, 54f), burst, 330, frames - 330, 24), 1.2f, 0.6f, 16f, 0.80f));
        return timeline(shots, beams, frames);
    }

    private static void addDetectorCycle(List<DanmakuLaser> beams, int start, int color, int finishColor, boolean hard) {
        pair(beams, start, 45f, 135f, color);
        pair(beams, start + 60, -22.5f, 202.5f, color);
        pair(beams, start + 100, -81.8f, 261.9f, color);

        int frame = start + 280;
        float angle = 45f;
        for (int i = 0; i < 8; i++) {
            single(beams, frame, angle, color);
            angle -= 11.25f;
            frame += 6;
        }

        frame += 180;
        angle = 135f;
        float step = hard ? 12.86f : 6.43f;
        int interval = hard ? 6 : 12;
        for (int i = 0; i < 16; i++) {
            single(beams, frame, angle, color);
            angle += step;
            frame += interval;
        }

        frame += 90;
        angle = 45f;
        for (int i = 0; i < 12; i++) {
            single(beams, frame, angle, finishColor);
            angle += 12.86f;
            frame += 6;
        }
    }

    private static void pair(List<DanmakuLaser> beams, int frame, float yaw1, float yaw2, int color) {
        single(beams, frame, yaw1, color);
        single(beams, frame, yaw2, color);
    }

    private static void single(List<DanmakuLaser> beams, int frame, float yaw, int color) {
        beams.add(laser(ticks(frame), 0f, yaw, 24f, 1.0f, 10, 12, 0f, color));
    }

    // BossCard2: three Normal familiars or five Hard familiars circle the boss and fire aimed 3-bullet fans
    // every 4/6 frames. BossCard2_at adds an accelerating one-bullet fan with the 5% interval decay.
    private static DanmakuPattern pendulum(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int startAngles = hard ? 18 : 90;
        int spacing = hard ? 72 : 120;
        int familiarCount = hard ? 5 : 3;
        var aimed = shot(BALL, 0.28f, PALE, 92);
        for (int i = 0; i < familiarCount; i++) {
            float yaw = startAngles + spacing * i;
            Vec3 radius = new Vec3(3.0, 0.65, 0);
            var emitter = fire(EmitShape.fan(3, 45f), aimed, 150, 2250, hard ? 6 : 4)
                    .withOffset(radius)
                    .withRotation(KeyframeRange.empty(yaw, yaw + 240f));
            shots.add(emitter);
        }

        var core = shot(BALL, 0.16f, PURPLE, 110, MotionSegment.accelerate(ticks(40), 0.008f));
        int frame = 330;
        int interval = 240;
        while (interval > 25) {
            shots.add(fire(EmitShape.aim(), core, frame, 0, 3).withJitter(5f));
            frame += interval;
            interval = (interval * 95) / 100;
        }
        return timeline(shots, 2400);
    }
}
