package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14.Th14PatternSupport.*;

/**
 * TH14 st02mbs.ecl (Sekibanki midboss) and st02bs.ecl. Numbers are the ECL spell ids.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st02mbs.ecl.txt, st02bs.ecl.txt.
 */
final class Stage02 {
    private static final String SEKI = "sekibanki";
    // bid 12 (pointed bullet) colors 6/4; bid 20 small rings and the 30x30 ball row.
    private static final int BLUE = 0x6464E8;
    private static final int GOLD = 0xD8B050;
    private static final int MAGENTA = 0xB85CD0;
    private static final int RED = 0xE05260;
    private static final int LASER = 0xC85CA8;
    private static final Vec3 HEAD = new Vec3(0, 1.8, 0);

    private Stage02() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(2, 12, false, SEKI, "飞符「Flying Head」（飞行之头）", 1920, () -> flyingHead(false)),
                card(2, 13, true, SEKI, "飞符「Flying Head」（飞行之头）", 1920, () -> flyingHead(true)),
                card(2, 16, false, SEKI, "首符「Close-Eye Shot」（闭目射击）", 2280, () -> closeEyeShot(false)),
                card(2, 17, true, SEKI, "首符「辘轳首飞来」", 2280, () -> closeEyeShot(true)),
                card(2, 20, false, SEKI, "飞头「Multiplicative Head」（倍增之头）", 2280, () -> multiplicativeHead(false)),
                card(2, 21, true, SEKI, "飞头「Seventh Head」（第七个头）", 2280, () -> multiplicativeHead(true)),
                card(2, 24, false, SEKI, "飞头「Dullahan Night」（杜拉罕之夜）", 2700, () -> dullahanNight(false)),
                card(2, 25, true, SEKI, "飞头「Dullahan Night」（杜拉罕之夜）", 2700, () -> dullahanNight(true)));
    }

    /** Bullet that leaves nearly still and accelerates to 1.0 px/frame (the flying-head trail). */
    private static DanmakuBulletSpec trailBullet(int color, int lifetimeTicks) {
        return shot(BALL, 0.002f, color, lifetimeTicks,
                MotionSegment.accelerate(0, 0.006f), MotionSegment.linear(ticks(30)));
    }

    /**
     * MBossCard1 (st02mbs L160): spell 10, 1920f, all difficulties. MCard01Head circles above the
     * boss and every 160 frames fires three aimed volleys of $A bullets (1.875 degrees apart,
     * speed 3.0 braking to 1.0 in 40 frames) while MBossCard1At heads drop downward trailing one
     * 0.01->1.0 px/frame bullet every 2 frames. $A starts 20 (Normal) / 30 (Hard) and grows to 64.
     */
    private static DanmakuPattern flyingHead(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var trail = trailBullet(GOLD, 53);
        shots.add(sweep(fire(EmitShape.aim(), trail, 300, 1620, 2), 6f, 0f, 0f).withJitter(20f));
        int count = hard ? 30 : 20;
        for (int start = 300; start < 1920; start += 160) {
            int window = Math.min(62, 1920 - start);
            var volley = shot(BULLET, 0.56f, BLUE, 33,
                    MotionSegment.accelerate(0, -0.028f), MotionSegment.linear(ticks(40)));
            shots.add(sweep(fire(EmitShape.fan(count, (count - 1) * 1.875f), volley, start, window, 30)
                    .withOffset(HEAD), 0f, 0f, 0f));
            if (count < 64) {
                count++;
            }
        }
        return timeline(shots, 1920);
    }

    /**
     * BossCard1 (st02bs L475): spell 14, 2280f. Card01Head tracks the player above the boss and
     * drops one 0.01->1.0 px/frame golden ring every 4 frames (lifetime 120f Normal, 240f Hard).
     * BossCard1_at2 rings the screen with lasers every 100 frames: 16 ways on a 22.5-degree
     * spacing (Normal, 128 px) or 20 ways on 18 degrees (Hard, 224 px), rotating 74.16 degrees
     * per batch.
     */
    private static DanmakuPattern closeEyeShot(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        shots.add(sweep(fire(EmitShape.aim(), trailBullet(GOLD, hard ? 80 : 40), 240, 2040, 4),
                10f, 0f, 0f).withJitter(14f));
        var beams = new ArrayList<DanmakuLaser>();
        int ways = hard ? 20 : 16;
        float spacing = 360f / ways;
        for (int batch = 0, start = 300; start < 2280; batch++, start += 100) {
            float base = 90f + batch * 74.16f;
            for (int way = 0; way < ways; way++) {
                beams.add(laser(ticks(start), 0f, base + way * spacing,
                        hard ? 14f : 8f, 0.9f, 15, 15, 0f, LASER));
            }
        }
        return timeline(shots, beams, 2280);
    }

    /**
     * BossCard2 (st02bs L645): spell 18, 2280f. Five heads (Normal) or seven (Hard) fly outward,
     * settle, and each loops twin player-aimed 128 px lasers every 100 frames plus a 15-shot
     * u-turn batch (bid 5, 1.0-1.2 px/frame curving 114.6 degrees over 120 frames) every 350
     * frames. BossCard2_at3 answers a close player with a 64-way ring of magenta ovals braking
     * 1.5 -> 1.0 px/frame.
     */
    private static DanmakuPattern multiplicativeHead(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        List<Vec3> heads = new ArrayList<>();
        heads.add(new Vec3(-7.5, 2.4, 0));
        heads.add(new Vec3(7.5, 2.4, 0));
        heads.add(new Vec3(-8.5, 1.8, 0));
        heads.add(new Vec3(8.5, 1.8, 0));
        heads.add(new Vec3(0, 2.6, 0));
        if (hard) {
            heads.add(new Vec3(-6.5, 2.0, 0));
            heads.add(new Vec3(6.5, 2.0, 0));
        }
        for (int batch = 0, start = 380; start < 2280; batch++, start += 100) {
            for (Vec3 head : heads) {
                float side = (float) (Math.signum(head.x) * 12f);
                beams.add(laser(ticks(start), 14f, side - 3f, 8f, 0.9f, 15, 15, 0f, LASER));
                beams.add(laser(ticks(start), 14f, side + 3f, 8f, 0.9f, 15, 15, 0f, LASER));
            }
        }
        for (int batch = 0, start = 380; start < 2280; batch++, start += 350) {
            float spin = (batch & 1) == 0 ? 2.8f : -2.8f;
            var curved = shot(BALL, 0.2f, BLUE, 80, MotionSegment.rotate(0, spin));
            for (Vec3 head : heads) {
                shots.add(fire(EmitShape.aim(), curved, start, 30, 2).withOffset(head).withJitter(8f));
            }
        }
        var ringBullet = shot(BALL, 0.28f, MAGENTA, 70,
                MotionSegment.accelerate(0, -0.0045f), MotionSegment.linear(ticks(20)));
        for (int start = 380; start < 2280; start += 240) {
            shots.add(fire(EmitShape.ring(64, 1.1f, true), ringBullet, start, 0, 3)
                    .withOffset(new Vec3(0, 1.6, 0)));
        }
        return timeline(shots, beams, 2280);
    }

    /**
     * BossCard3 (st02bs L813): spell 22, 2700f. The head patrols three anchors while firing
     * three player-aimed laser volleys per 180-frame cycle (twin 128 px beams on Normal, twin
     * plus 20-degree wides on Hard); our fixed laser origin keeps the boss position. BossCard3At
     * satellites spawn every 8 (Normal) / 6 (Hard) frames in directions rotating 22.5 degrees,
     * each trailing 0.01->1.0 px/frame magenta ovals on a 6/4-frame cadence.
     */
    private static DanmakuPattern dullahanNight(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        for (int start = 260; start < 2700; start += 180) {
            for (int volley = 0; volley < 3; volley++) {
                int volleyStart = start + volley * 30;
                beams.add(laser(ticks(volleyStart), 14f, -3f, 8f, 0.9f, 15, 15, 0f, LASER));
                beams.add(laser(ticks(volleyStart), 14f, 3f, 8f, 0.9f, 15, 15, 0f, LASER));
                if (hard) {
                    beams.add(laser(ticks(volleyStart), 14f, -20f, 8f, 0.9f, 15, 15, 0f, LASER));
                    beams.add(laser(ticks(volleyStart), 14f, 20f, 8f, 0.9f, 15, 15, 0f, LASER));
                }
            }
        }
        var trail = trailBullet(MAGENTA, 33);
        int spawnStep = hard ? 8 : 12;
        int trailStep = hard ? 4 : 6;
        for (int k = 0, start = 310; start < 2700; k++, start += spawnStep) {
            float direction = 135f + k * 22.5f;
            int window = Math.min(120, 2700 - start);
            shots.add(sweep(fire(EmitShape.aim(), trail, start, window, trailStep),
                    0f, direction, direction).withJitter(15f));
        }
        return timeline(shots, beams, 2700);
    }
}
