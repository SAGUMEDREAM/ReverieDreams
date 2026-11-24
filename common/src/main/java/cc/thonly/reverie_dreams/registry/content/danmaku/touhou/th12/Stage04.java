package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th12.Th12PatternSupport.*;

/** TH12 stage04.ecl: Minamitsu Murasa's Normal and Hard spell-practice entries. */
final class Stage04 {
    private static final String MURASA = "item.reverie_dreams.spellcard.character.mizuhashi_murasa";
    private static final int BLUE = 0x6388DB;
    private static final int CYAN = 0x74D8D8;
    private static final int WHITE = 0xF0EBF5;
    private static final int RED = 0xE65C60;

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 44, false, MURASA, "倾覆「同路人之锚」", 2700, () -> sinkingAnchor(false)),
                card(4, 45, true, MURASA, "倾覆「沉没之锚」", 2700, () -> sinkingAnchor(true)),
                card(4, 48, false, MURASA, "溺符「Deep Vortex」（深海漩涡）", 2400, () -> deepVortex(false)),
                card(4, 49, true, MURASA, "溺符「Sinkable Vortex」（沉底漩涡）", 2400, () -> deepVortex(true)),
                card(4, 52, false, MURASA, "港符「Phantom Ship Harbor」（幽灵船之泊）", 2700, () -> phantomHarbor(false)),
                card(4, 53, true, MURASA, "港符「幽灵船之港」", 2700, () -> phantomHarbor(true)),
                card(4, 55, false, MURASA, "幽灵「Sinker Ghost」（使船沉没的幽灵）", 2640, () -> sinkerGhost(false)),
                card(4, 56, true, MURASA, "幽灵「悄然袭来的长勺」", 2640, () -> sinkerGhost(true)));
    }

    // BossCard1 launches Anchor00 from the boss. Anchor00_at fires backward with a +0.19635 rad spiral,
    // speed 1.2 on Normal and 1.8 on Hard. Hard also adds the two Anchor00_at2h side clouds.
    private static DanmakuPattern sinkingAnchor(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var trail = shot(BULLET, hard ? 0.36f : 0.30f, BLUE, 100);
        for (int wave = 0, start = 120; start < 2500; wave++, start += 320) {
            float side = (wave & 1) == 0 ? -1f : 1f;
            shots.add(fire(EmitShape.fan(2, 22.5f), trail, start, 200, hard ? 12 : 18)
                    .withOffset(new Vec3(side * 0.5, 0.35, 0))
                    .withRotation(KeyframeRange.empty(0f, 540f)));
            if (hard) {
                shots.add(fire(EmitShape.fan(15, 168f), shot(BULLET, 0.24f, CYAN, 90), start + 180, 30, 6)
                        .withOffset(new Vec3(-8.0f, 0.4, 0)).withJitter(10f));
                shots.add(fire(EmitShape.fan(15, 168f), shot(BULLET, 0.24f, CYAN, 90), start + 180, 30, 6)
                        .withOffset(new Vec3(8.0f, 0.4, 0)).withJitter(10f));
            }
        }
        return timeline(shots, 2700);
    }

    // BossCard2_at2 is fired twice from player-position. Normal uses 60+70 bullets, Hard uses 80+90,
    // with 6/8 radial candidates and acceleration over 300/200 frames.
    private static DanmakuPattern deepVortex(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var pull = shot(BULLET, 0.02f, WHITE, 150, MotionSegment.accelerate(ticks(100), hard ? 0.018f : 0.012f));
        int[] durations = hard ? new int[]{80, 90} : new int[]{60, 70};
        int start = 150;
        for (int duration : durations) {
            shots.add(fire(EmitShape.ring(hard ? 8 : 6, 3.0f, true), pull, start, duration, 3)
                    .withRotation(KeyframeRange.empty(0f, 360f)).withJitter(6f));
            start += 550;
        }
        return timeline(shots, 2400);
    }

    // BossCard3 places ten Anchor03 objects around the boss, each firing backward. Anchor03_at uses
    // a 0.628319 rad step; the speed is 0.8 on Normal and 1.0 on Hard.
    private static DanmakuPattern phantomHarbor(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var wake = shot(BULLET, hard ? 0.30f : 0.24f, BLUE, 100);
        for (int cycle = 0, start = 150; start < 2400; cycle++, start += 480) {
            float side = (cycle & 1) == 0 ? -1f : 1f;
            shots.add(fire(EmitShape.fan(hard ? 10 : 10, 360f), wake, start, hard ? 60 : 60, hard ? 18 : 18)
                    .withOffset(new Vec3(side * 2.0f, 0.25, 0)).withJitter(6f));
            if (hard) {
                shots.add(fire(EmitShape.fan(10, 180f), shot(BULLET, 0.26f, CYAN, 80), start + 190, 30, 6)
                        .withOffset(new Vec3(side * 4.0f, 0.5, 0)).withJitter(8f));
            }
        }
        return timeline(shots, 2700);
    }

    // BossCard4_at creates two paired spirals every frame for 20 frames; Hard also fires a second
    // copy at 1.5x distance. Every fifth cast aims the base angle at the player.
    private static DanmakuPattern sinkerGhost(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var inner = shot(BULLET, 0.02f, RED, 130, MotionSegment.accelerate(ticks(100), 0.010f));
        var outer = shot(BULLET, 0.02f, WHITE, 130, MotionSegment.accelerate(ticks(100), 0.015f));
        int start = 150;
        int interval = hard ? 60 : 80;
        for (int wave = 0; start < 2500; wave++, start += interval) {
            boolean aimed = (wave % 5) == 0;
            var emitter = fire(EmitShape.fan(2, 18f), inner, start, 20, 3)
                    .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(aimed ? 0f : 20f,
                            aimed ? 1080f : 1100f));
            shots.add(emitter);
            var second = fire(EmitShape.fan(2, 18f), outer, start, 20, 3)
                    .withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(aimed ? 0f : 20f,
                            aimed ? 1080f : 1100f));
            if (hard) {
                second = second.withOffset(new Vec3(0, 0.6, 0));
            }
            shots.add(second);
            interval = Math.max(1, interval - 12);
        }
        return timeline(shots, 2640);
    }
}
