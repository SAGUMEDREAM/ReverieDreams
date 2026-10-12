package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.function.KeyframeFunction;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuSpawner;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 单个 {@link DanmakuEmitter} 的运行时实例。
 */
public final class DanmakuEmitterRuntime {
    private final DanmakuEmitter emitter;
    private final DanmakuPatternRuntime owner;
    @Nullable
    private Vec3 capturedPosition;

    DanmakuEmitterRuntime(DanmakuEmitter emitter, DanmakuPatternRuntime owner) {
        this.emitter = emitter;
        this.owner = owner;
    }

    public DanmakuEmitter getEmitter() {
        return this.emitter;
    }

    /**
     * @return 该发射器是否仍在时间线内（true 表示图案不应被回收）
     */
    public boolean tick(int patternTick) {
        DanmakuEmitter emitter = this.emitter;
        int start = Math.max(0, emitter.delay());
        int duration = Math.max(0, emitter.duration());
        int end = start + duration;
        if (patternTick < start) {
            return true;
        }
        if (patternTick > end) {
            return false;
        }
        int localTick = patternTick - start;
        int interval = Math.max(1, emitter.interval());
        if (localTick % interval != 0) {
            return true;
        }

        Vec3 anchor = this.anchor();
        float progress = duration <= 0 ? 1f : Mth.clamp((float) localTick / (float) duration, 0f, 1f);
        float rotationAngle = sample(emitter.rotation(), progress);
        Vec3 offset = DanmakuMath.rotateY(emitter.offset(), rotationAngle);
        Vec3 muzzle = anchor.add(offset);

        float basePitch = emitter.pitch();
        float baseYaw = emitter.yaw();
        switch (emitter.aim()) {
            case TARGET -> {
                Vec3 target = this.owner.targetPosition();
                if (target != null) {
                    float[] pitchYaw = DanmakuMath.pitchYaw(muzzle, target);
                    basePitch = pitchYaw[0];
                    baseYaw = pitchYaw[1];
                }
            }
            case FIXED -> {
            }
            case SOURCE_FACING -> {
                Entity source = this.owner.getSource();
                if (source != null) {
                    basePitch = source.getXRot();
                    baseYaw = source.getYRot();
                }
            }
            case OUTWARD -> {
                if (offset.lengthSqr() > 1.0E-6D) {
                    float[] pitchYaw = DanmakuMath.pitchYaw(anchor, muzzle);
                    basePitch = pitchYaw[0];
                    baseYaw = pitchYaw[1];
                }
            }
        }
        basePitch += sample(emitter.pitchRange(), progress);
        baseYaw += sample(emitter.yawRange(), progress);

        List<EmitShape.Shot> shots = emitter.shape().emit(basePitch, baseYaw);
        for (EmitShape.Shot shot : shots) {
            DanmakuSpawner.spawn(
                    this.owner.getWorld(),
                    this.owner.getSource(),
                    muzzle.add(shot.offset()),
                    shot.pitch(),
                    shot.yaw(),
                    emitter.bullet(),
                    shot.speedScale()
            );
        }
        return true;
    }

    private Vec3 anchor() {
        Entity source = this.owner.getSource();
        if (this.emitter.attached() && source != null && !source.isRemoved()) {
            return new Vec3(source.getX(), source.getEyeY(), source.getZ());
        }
        if (this.capturedPosition == null) {
            this.capturedPosition = this.owner.getOrigin();
        }
        return this.capturedPosition;
    }

    private static float sample(@Nullable KeyframeRange range, float progress) {
        if (range == null) {
            return 0f;
        }
        KeyframeFunction function = range.createFunction();
        if (function == null) {
            return 0f;
        }
        return function.sample(Mth.clamp(progress, 0f, 1f));
    }
}
