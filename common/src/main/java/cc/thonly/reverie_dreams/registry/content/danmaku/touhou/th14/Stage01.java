package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14PatternSupport.*;

/**
 * TH14 st01mbs.ecl (Cirno midboss) and st01bs.ecl (Wakasagihime).
 * Numbers are the ECL spell ids (0 = timer card, 2 and 6 = boss cards).
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st01mbs.ecl.txt, st01bs.ecl.txt.
 */
final class Stage01 {
    private static final String CIRNO = "cirno";
    private static final String WAKA = "wakasagihime";
    // TH14 16-color column palette sampled from bullet1.png (bid 12 row, colors 0-15).
    private static final int BLUE = 0x6464E8;
    private static final int GREEN = 0x58D868;
    private static final int CYAN = 0x6CE8E8;
    // The 30x30 bubble row only carries eight colors; its fourth entry is rose red.
    private static final int ROSE = 0xE05260;

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(1, 1, true, CIRNO, "冰符「Ultimate Blizzard」（终极风暴）", 1500, Stage01::ultimateBlizzard),
                card(1, 4, false, WAKA, "水符「Tail Fin Slap」（尾鳍拍击）", 2400, () -> tailFinSlap(false)),
                card(1, 5, true, WAKA, "水符「Tail Fin Slap」（尾鳍拍击）", 2400, () -> tailFinSlap(true)),
                card(1, 8, false, WAKA, "鳞符「Scale Wave」（鳞之波）", 2400, () -> scaleWave(false)),
                card(1, 9, true, WAKA, "鳞符「逆鳞的惊涛」", 2400, () -> scaleWave(true)));
    }

    /**
     * MBossCard1 (st01mbs L240): spell3 0, 1500f, Hard/Lunatic only (Normal branches to MBossDead
     * without a card, st01mbs L25-L36). MBossCard1_at: Hard fires 3x8 = 24 shards spread 2 degrees
     * apart (46 degrees) over eight repeats on a 6-frame cadence while the base yaw walks 10
     * degrees per repeat; MBossCard1_at2 waits 300 frames, then alternates bubble clusters
     * (6 ways at 3 degrees, speed 8 px/frame braking to 0 in 60 frames) every 160 frames.
     */
    private static DanmakuPattern ultimateBlizzard() {
        var shots = new ArrayList<DanmakuEmitter>();
        var snow = shot(BULLET, 0.19f, BLUE, 80,
                MotionSegment.accelerate(0, 0.007f), MotionSegment.linear(ticks(30)));
        for (int cycle = 60; cycle < 1500; cycle += 320) {
            // counter-rotating shard streams: 8 repeats x 6 frames, fan walks 10 degrees per repeat
            shots.add(sweep(fire(EmitShape.fan(24, 46f), snow, cycle, 48, 2), 0f, 0f, -80f));
            if (cycle + 160 + 48 < 1500) {
                shots.add(sweep(fire(EmitShape.fan(24, 46f), snow, cycle + 160, 48, 2), 0f, 0f, 80f));
            }
        }
        var bubble = shot(BUBBLE, 1.5f, ROSE, 150,
                MotionSegment.accelerate(0, -0.075f), MotionSegment.linear(ticks(60)));
        for (int k = 0; 360 + k * 160 < 1500; k++) {
            float base = (k & 1) == 0 ? 22.5f : 157.5f;
            shots.add(sweep(fire(EmitShape.fan(6, 15f), bubble, 360 + k * 160, 0, 3)
                    .withOffset(new Vec3(0, 0.4, 0)), 0f, base, base));
            // Hard fires the 11.25/168.75 degree twin cluster beside each main angle
            float twin = base < 90f ? 11.25f : 168.75f;
            shots.add(sweep(fire(EmitShape.fan(6, 15f), bubble, 360 + k * 160, 0, 3)
                    .withOffset(new Vec3(0, 0.4, 0)), 0f, twin, twin));
        }
        return timeline(shots, 1500);
    }

    /**
     * BossCard1 (st01bs L301): spell 2, 2400f, one declaration for all difficulties.
     * Two 60-wave streams per 750-frame cycle (left at +30, right at +390); each wave fires
     * $A bullets spanning (A-1) x 5.5626 degrees and the base yaw walks 10 degrees per wave,
     * starting at +/-200 degrees from the player. $A grows by one per stream up to
     * diffI caps 6 (Normal) / 12 (Hard). Bullet speed starts 0.8 px/frame and each wave's
     * bullets brake-accelerate by %F/60 per frame for 60 frames with %F = 0.8 + i x 0.4/0.33.
     */
    private static DanmakuPattern tailFinSlap(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        int count = hard ? 6 : 2;
        int cap = hard ? 12 : 6;
        float step = hard ? 0.33f : 0.4f;
        int[] starts = {90, 450, 840, 1200, 1590, 1950, 2340};
        for (int start : starts) {
            if (start >= 2400) {
                continue;
            }
            boolean right = (start % 750) >= 400;
            int window = Math.min(120, 2400 - start);
            addSlapStream(shots, count, step, start, window, right);
            if (count < cap) {
                count++;
            }
        }
        return timeline(shots, 2400);
    }

    private static void addSlapStream(List<DanmakuEmitter> shots, int count, float step,
                                      int start, int window, boolean right) {
        float meanGain = Math.min(0.5f, (0.8f + (count - 1) * step * 0.5f) * 0.1875f);
        var slap = shot(BULLET, 0.15f, BLUE, 70,
                MotionSegment.accelerate(0, meanGain / 20f), MotionSegment.linear(ticks(60)));
        float from = right ? 200f : -200f;
        float to = right ? -400f : 400f;
        shots.add(sweep(fire(EmitShape.fan(count, (count - 1) * 5.5626f), slap, start, window, 2),
                0f, from, to).withOffset(new Vec3(0, 0.35, 0)));
    }

    /**
     * BossCard2 (st01bs L442): spell 6, 2400f; Normal is 鱗符「スケールウェイブ」, Hard 逆鱗の荒波.
     * BossCard2At/At2 satellites enter from +/-112 px and sweep a 1-frame spiral:
     * 400 planned shots (about 270 before the satellite despawns) with the angle stepping
     * 15 degrees per frame, speed 0.1 px/frame ramping to 1.6 (Normal) / 2.2 (Hard) in 60 frames.
     * Normal spawns two satellites per loop (colors 10/8), Hard three (colors 10/8/6);
     * the loop interval $A shrinks 300 -> 60 in steps of 60. BossCard2_at2 has no caller.
     */
    private static DanmakuPattern scaleWave(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var spiral = shot(BULLET, 0.02f, BLUE, 90,
                MotionSegment.accelerate(0, hard ? 0.021f : 0.015f), MotionSegment.linear(ticks(60)));
        int interval = 300;
        int start = 120;
        while (start < 2400) {
            addSatellite(shots, spiral, start, -7.0f, GREEN, 180f, -4050f, 2400);
            addSatellite(shots, spiral, start + 120, 7.0f, CYAN, 0f, 4050f, 2400);
            if (hard) {
                addSatellite(shots, spiral, start + 240, -7.0f, BLUE, 180f, -4050f, 2400);
            }
            start += (hard ? 360 : 240) + interval;
            interval = Math.max(60, interval - 60);
        }
        return timeline(shots, 2400);
    }

    private static void addSatellite(List<DanmakuEmitter> shots, DanmakuBulletSpec spiral,
                                     int start, float x, int color, float fromYaw, float toYaw, int deadline) {
        if (start >= deadline) {
            return;
        }
        int window = Math.min(270, deadline - start);
        // three bullets per tick spaced 15 degrees reproduce the 1-frame spiral cadence
        shots.add(sweep(fire(EmitShape.fan(3, 30f), spiral.withColor(color), start, window, 1)
                .withOffset(new Vec3(x, 0.9, 0)), 0f, fromYaw, toYaw));
    }
}
