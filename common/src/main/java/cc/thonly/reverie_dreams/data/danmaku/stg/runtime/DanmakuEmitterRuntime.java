package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.function.KeyframeFunction;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuSpawner;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuScatter;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 单个 {@link DanmakuEmitter} 的运行时实例。
 */
public final class DanmakuEmitterRuntime {
    private final DanmakuEmitter emitter;
    private final DanmakuPatternRuntime owner;
    private final int order;
    private final int start;
    private final int duration;
    private final int interval;
    private int nextFireTick;

    DanmakuEmitterRuntime(DanmakuEmitter emitter, DanmakuPatternRuntime owner, int order) {
        this.emitter = emitter;
        this.owner = owner;
        this.order = order;
        this.start = Math.max(0, emitter.delay());
        this.duration = Math.max(0, emitter.duration());
        this.interval = Math.max(1, emitter.interval());
    }

    int order() {
        return this.order;
    }

    int nextFireTick() {
        return this.nextFireTick;
    }

    boolean scheduleFrom(int fromTick) {
        // Align to the original cadence even when an intro is outside the repeated window.
        long elapsed = Math.max(0L, (long) fromTick - this.start);
        long next = Math.ceilDiv(elapsed, this.interval) * this.interval;
        if (next > this.duration || next + this.start > Integer.MAX_VALUE) {
            return false;
        }
        this.nextFireTick = this.start + (int) next;
        return true;
    }

    void fire(int patternTick) {
        DanmakuEmitter emitter = this.emitter;
        int localTick = patternTick - this.start;
        int duration = this.duration;
        Vec3 anchor = this.anchor();
        float progress = duration <= 0 ? 1f : Mth.clamp((float) localTick / (float) duration, 0f, 1f);
        float rotationAngle = sample(emitter.rotation(), progress);
        Vec3 offset = DanmakuMath.rotateY(emitter.offset(), rotationAngle);
        DanmakuScatter scatter = emitter.scatter();
        boolean scatterPosition = scatter.width() > 0 || scatter.depth() > 0;
        boolean scatterSpeed = scatter.minSpeedScale() < 1;
        float jitter = Math.max(0f, emitter.jitter());
        ThreadLocalRandom random = scatterPosition || scatterSpeed || scatter.yawHalfRange() > 0 || jitter > 0
                ? ThreadLocalRandom.current() : null;
        SpellAimFrame viewFrame = this.owner.viewFrame();
        Vec3 localMuzzle = offset;
        if (scatterPosition) {
            localMuzzle = localMuzzle.add((random.nextFloat() - 0.5f) * scatter.width(), 0,
                    (random.nextFloat() - 0.5f) * scatter.depth());
        }
        Vec3 muzzle = anchor.add(viewFrame == null ? localMuzzle : viewFrame.toWorld(localMuzzle));

        float basePitch = emitter.pitch();
        float baseYaw = emitter.yaw();
        if (viewFrame != null) {
            // A local-space cast authors both fixed and aimed vectors in its caster's frame.
            if (emitter.aim() == EmitAim.OUTWARD && offset.lengthSqr() > 1.0E-6D) {
                float[] angles = DanmakuMath.pitchYaw(Vec3.ZERO, localMuzzle);
                basePitch += angles[0];
                baseYaw += angles[1];
            }
        } else
            switch (emitter.aim()) {
                case TARGET, TARGET_OR_SOURCE_FACING -> {
                    Entity source = this.owner.getSource();
                    boolean fallback = emitter.aim() == EmitAim.TARGET_OR_SOURCE_FACING;
                    boolean aimedByPlayer = this.owner.hasPlayerCaster()
                            && (fallback || this.owner.getPattern().timing().isTimed() || !(source instanceof Player));
                    Vec3 target = aimedByPlayer ? null : this.owner.targetPosition();
                    if (target != null) {
                        float[] pitchYaw = DanmakuMath.pitchYaw(muzzle, target);
                        basePitch = pitchYaw[0];
                        baseYaw = pitchYaw[1];
                    } else if (fallback || aimedByPlayer) {
                        basePitch = this.owner.sourcePitch(basePitch);
                        baseYaw = this.owner.sourceYaw(baseYaw);
                    }
                }
                case FIXED -> {
                }
                case SOURCE_FACING -> {
                    basePitch = this.owner.sourcePitch(basePitch);
                    baseYaw = this.owner.sourceYaw(baseYaw);
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
        Entity caster = this.owner.getCaster();
        for (EmitShape.Shot shot : shots) {
            float shotPitch = shot.pitch();
            float shotYaw = shot.yaw();
            if (scatter.yawHalfRange() > 0) {
                shotYaw += (2 * random.nextFloat() - 1) * scatter.yawHalfRange();
            }
            float speedScale = scatterSpeed
                    ? shot.speedScale() * Mth.lerp(random.nextFloat(), scatter.minSpeedScale(), 1f)
                    : shot.speedScale();
            if (jitter > 0) {
                // 抖动只影响生成时刻的初始方向，双端确定性不受影响
                shotYaw += (random.nextFloat() * 2f - 1f) * jitter;
                shotPitch += (random.nextFloat() * 2f - 1f) * jitter * 0.5f;
            }
            Vec3 spawnOffset = shot.offset();
            if (viewFrame != null) {
                spawnOffset = viewFrame.toWorld(spawnOffset);
                float[] angles = DanmakuMath.pitchYaw(Vec3.ZERO, viewFrame.toWorld(DanmakuMath.direction(shotPitch, shotYaw)));
                shotPitch = angles[0];
                shotYaw = angles[1];
            }
            this.owner.track(DanmakuSpawner.spawn(
                    this.owner.getWorld(),
                    caster,
                    muzzle.add(spawnOffset),
                    shotPitch,
                    shotYaw,
                    emitter.bullet(),
                    speedScale
            ));
        }
    }

    private Vec3 anchor() {
        return switch (this.emitter.anchor()) {
            case ORIGIN -> this.owner.getOrigin();
            case TARGET -> {
                Vec3 target = this.owner.targetPosition();
                yield target != null ? target : this.sourceAnchor();
            }
            case SOURCE -> this.sourceAnchor();
        };
    }

    private Vec3 sourceAnchor() {
        return this.emitter.attached() ? this.owner.sourceMuzzlePosition() : this.owner.getOrigin();
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
