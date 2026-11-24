package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dispatches each cue once per pass through the attack timeline.
 */
final class DanmakuLaserRuntime {
    private final List<DanmakuLaser> cues;
    private int cursor;

    DanmakuLaserRuntime(List<DanmakuLaser> cues) {
        this.cues = cues.stream().sorted(Comparator.comparingInt(DanmakuLaser::at)).toList();
    }

    void seek(int attackTick) {
        int low = 0;
        int high = this.cues.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (this.cues.get(middle).at() < attackTick) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        this.cursor = low;
    }

    void fire(int attackTick, Consumer<DanmakuLaser> spawn) {
        while (this.cursor < this.cues.size() && this.cues.get(this.cursor).at() == attackTick) {
            spawn.accept(this.cues.get(this.cursor++));
        }
    }

    static Placement place(DanmakuLaser cue, Vec3 anchor, float sourceYaw, @Nullable SpellAimFrame playerFrame) {
        if (playerFrame == null) {
            return new Placement(anchor.add(DanmakuMath.rotateY(cue.offset(), -sourceYaw)),
                    cue.withYaw(sourceYaw + cue.yaw()));
        }
        Vec3 direction = playerFrame.toWorld(DanmakuMath.direction(cue.pitch(), cue.yaw()));
        float[] angles = DanmakuMath.pitchYaw(Vec3.ZERO, direction);
        return new Placement(anchor.add(playerFrame.toWorld(cue.offset())), cue.withDirection(angles[0], angles[1]));
    }

    record Placement(Vec3 origin, DanmakuLaser cue) {
    }
}
