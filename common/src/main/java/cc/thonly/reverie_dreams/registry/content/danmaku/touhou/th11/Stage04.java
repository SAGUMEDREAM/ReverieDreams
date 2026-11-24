package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th11.Th11PatternSupport.*;

/**
 * TH11 stage04.ecl. This file intentionally contains only the shared first card;
 * the remaining Satori cards are route or shot-type scripts in stage4c00x and stage4c01x.
 */
final class Stage04 {
    private static final String SATORI = "komeiji_satori";
    private static final int PURPLE = 0xB75BD0;
    private static final int RED = 0xE05260;

    private Stage04() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(4, 44, false, SATORI, "回忆「Terrible Souvenir」（恐怖的回忆）", 2580,
                        () -> terribleSouvenir(false)),
                card(4, 45, true, SATORI, "回忆「恐怖催眠术」", 2580,
                        () -> terribleSouvenir(true)));
    }

    /**
     * BossCard1: 16 Normal or 32 Hard beams per cue. The ECL sets one cue at -90
     * degrees, the second at +90 degrees 200 frames later, then repeats after a
     * 150-frame tail. Beam angular speed is 0.6 degrees/frame, or 1.8 degrees/tick.
     */
    private static DanmakuPattern terribleSouvenir(boolean hard) {
        var shots = new ArrayList<DanmakuEmitter>();
        var beams = new ArrayList<DanmakuLaser>();
        int beamCount = hard ? 32 : 16;
        for (int groupStart = 150; groupStart < 2580; groupStart += 750) {
            addSouvenirCue(shots, beams, hard, groupStart, -1, beamCount);
            if (groupStart + 200 < 2580) {
                addSouvenirCue(shots, beams, hard, groupStart + 200, 1, beamCount);
            }
        }
        return timeline(shots, 2580).withLasers(beams);
    }

    private static void addSouvenirCue(List<DanmakuEmitter> shots, List<DanmakuLaser> beams,
                                        boolean hard, int start, int side, int beamCount) {
        for (int beam = 0; beam < beamCount; beam++) {
            float angle = side * 90f + beam * (360f / beamCount);
            beams.add(new DanmakuLaser(ticks(start), 0f, angle, 16f, 0.40f,
                    ticks(20), ticks(50), -side * 1.8f, PURPLE));
        }

        int volleyCount = hard ? 12 : 8;
        float speed = hard ? 0.22f : 0.18f;
        var spiral = shot(PELLET, speed, PURPLE, 105);
        for (int volley = 0; volley < 16; volley++) {
            int frame = start + 200 + volley * 6;
            float phase = 90f - side * 12f * volley;
            shots.add(sweep(fire(EmitShape.ring(volleyCount, 2.7f, true), spiral, frame, 0, 3),
                    0f, phase, phase));
        }

        var pulse = shot(BULLET, hard ? 0.30f : 0.26f, RED, 115);
        var pulseShape = hard ? EmitShape.fan(8, 60f) : EmitShape.aim();
        shots.add(sweep(fire(pulseShape, pulse, start + 200 + 96, 0, 3),
                0f, -side * 12f, -side * 12f));
    }
}
