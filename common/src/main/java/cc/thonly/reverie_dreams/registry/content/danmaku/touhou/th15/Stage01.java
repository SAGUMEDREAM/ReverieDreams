package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th15.Th15PatternSupport.*;

/** TH15 st01bs.ecl: Seiran's Normal and Hard spell-practice entries. */
final class Stage01 {
    private static final String SEIRAN = "seiran";
    private static final Identifier BULLET = ReverieDreams.id("bullet");
    private static final Identifier PELLET = ReverieDreams.id("pellet");
    private static final int VIOLET = 0x8B5FD6;
    private static final int GOLD = 0xDDBC61;

    private Stage01() {}

    static List<TouhouSpell> spells() {
        return List.of(
                card(1, 2, false, SEIRAN, "弹符「雄鹰射击」", 2400, () -> eagleShooting(false)),
                card(1, 2, true, SEIRAN, "弹符「雄鹰射击」", 2400, () -> eagleShooting(true)),
                card(1, 6, false, SEIRAN, "枪符「Lunatic Gun」", 2400, () -> lunaticGun(false)),
                card(1, 6, true, SEIRAN, "枪符「Lunatic Gun」", 2400, () -> lunaticGun(true)));
    }

    // st01bs.ecl BossCard1_at2 (528-594): two interlaced emitters fire escalating
    // bursts of 20 (N) / 40 (H) volleys; the base angle rotates by (30°-2°·cycle)
    // per frame, so successive shots alternate ~180° (N) / ~120° (H) apart.
    // BossCard1 (458-481) adds aimed escalating streams every 200f (N) / 100f (H).
    // The two ECL emitters share one position; the mod mirrors them left/right.
    private static DanmakuPattern eagleShooting(boolean hard) {
        final int deadline = 2400;
        var emitters = new ArrayList<DanmakuEmitter>();
        var kunai = shot(BULLET, hard ? 0.20f : 0.15f, VIOLET, 80);
        int bursts = hard ? 8 : 9;
        int burstGap = hard ? 291 : 249;
        int shots = hard ? 40 : 20;
        int step = hard ? 3 : 6;
        for (int side = 0; side < 2; side++) {
            Vec3 offset = new Vec3(side == 0 ? -1.1 : 1.1, 0.2, 0);
            for (int c = 0; c < bursts; c++) {
                for (int i = 0; i < shots; i++) {
                    int count = Math.min(2 + 2 * i, 24);
                    float yaw = 90f + i * (hard ? 4f : 6f) * (30 - 2 * c) * (side == 0 ? 1f : -1f);
                    var cue = sweep(fire(EmitShape.fan(count, 8f), kunai, 96 + c * burstGap + i * step, 0, 1), 0, yaw, yaw);
                    emitters.add(cue.withOffset(offset));
                }
            }
        }
        var aimed = shot(PELLET, hard ? 0.30f : 0.20f, GOLD, 90);
        int streamGap = hard ? 99 : 198;
        for (int stream = 0; 102 + stream * streamGap + 48 < 2340; stream++) {
            for (int i = 0; i < 8; i++) {
                emitters.add(fire(EmitShape.fan(Math.min(1 + i, 8), 6f), aimed, 102 + stream * streamGap + i * 6, 0, 1));
            }
        }
        return timeline(emitters, deadline);
    }

    // st01bs.ecl BossCard2_at2 (739-816): twin machine guns spray 5-lane 16-row
    // ladders (speed ladder sampled to three layers) at random headings every
    // 90f. BossCard2 (640-694) sweeps five (N) / ten (H) aimed streams whose
    // headings walk [0,+60,-60,-30,+30] (and [-30,+15,-15,+30,0] on Hard).
    private static DanmakuPattern lunaticGun(boolean hard) {
        final int deadline = 2400;
        var emitters = new ArrayList<DanmakuEmitter>();
        float[] ladder = {0.10f, 0.26f, 0.42f};
        int color = VIOLET;
        for (int side = 0; side < 2; side++) {
            Vec3 offset = new Vec3(side == 0 ? -1.1 : 1.1, 0.25, 0);
            for (int volley = 0; 96 + volley * 90 < 2340; volley++) {
                float heading = volley * 137f + side * 61f;
                for (float speed : ladder) {
                    var bullet = shot(BULLET, speed + (hard ? 0.05f : 0f), color, 100);
                    var cue = sweep(fire(EmitShape.fan(5, 8f), bullet, 96 + volley * 90, 0, 1), 0, heading, heading);
                    emitters.add(cue.withOffset(offset));
                }
            }
        }
        var pellet = shot(PELLET, hard ? 0.35f : 0.20f, GOLD, 90);
        float[] offsets = hard ? new float[]{-30, 15, -15, 30, 0, -30, 15, -15, 30, 0}
                : new float[]{0, 60, -60, -30, 30};
        int period = hard ? 189 : 144;
        for (int round = 0; 102 + round * period + offsets.length * 9 + 24 < 2340; round++) {
            for (int s = 0; s < offsets.length; s++) {
                for (int i = 0; i < 8; i++) {
                    emitters.add(fire(EmitShape.fan(Math.min(1 + i, 8), 14f), pellet,
                            102 + round * period + s * 9 + i * 3, 0, 1));
                }
            }
        }
        return timeline(emitters, deadline);
    }
}
