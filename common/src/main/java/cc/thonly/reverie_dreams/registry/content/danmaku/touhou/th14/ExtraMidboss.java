package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th14;

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
 * TH14 st07mbs.ecl (Tsukumo sisters, Extra midboss). The research catalog carries only
 * MBossCard3 (spellEx 109); MBossCard2 (spellEx 108) has no catalog id and is not registered.
 * Source: E:/Temp/rd-ecl-continuation-20261116/th14/ecl/st07mbs.ecl.txt.
 */
final class ExtraMidboss {
    private static final String BENBEN = "item.reverie_dreams.spellcard.character.tsukumo_benben";
    private static final int WHITE = 0xE8E8F0;

    private ExtraMidboss() {}

    static List<TouhouSpell> spells() {
        return List.of(
                extraCard(7, 110, BENBEN, "弦乐「净琉璃世界」", 2400, ExtraMidboss::joruriWorld));
    }

    /**
     * MBossCard3/MBossCard3B (st07mbs L581): spellEx 109, 2400f, both sisters alternating every
     * 60 frames. MBossCard3_at: 300 white notes on a 2-frame cadence whose yaw steps -6 degrees
     * (plus roll) and which bounce off the field walls; MBossCard3B_at: 200 notes on a 3-frame
     * cadence whose yaw steps +222.5 degrees (dense scatter) with a 0.012 rad/frame drift.
     */
    private static DanmakuPattern joruriWorld() {
        var shots = new ArrayList<DanmakuEmitter>();
        var bouncer = shot(NOTE, 0.26f, WHITE, 110, MotionSegment.bounce(0, 11f));
        shots.add(sweep(fire(EmitShape.aim(), bouncer, 90, 600, 2), 0f, 0f, -1800f)
                .withOffset(new Vec3(-1.5, 1.6, 0)).withJitter(6f));
        var scatter = shot(NOTE, 0.26f, WHITE, 100, MotionSegment.rotate(0, -0.36f));
        shots.add(sweep(fire(EmitShape.aim(), scatter, 750, 600, 3), 0f, 0f, 44500f)
                .withOffset(new Vec3(1.5, 1.6, 0)).withJitter(4f));
        shots.add(sweep(fire(EmitShape.aim(), bouncer, 1410, 600, 2), 0f, 0f, -1800f)
                .withOffset(new Vec3(-1.5, 1.6, 0)).withJitter(6f));
        return timeline(shots, 2400);
    }
}
