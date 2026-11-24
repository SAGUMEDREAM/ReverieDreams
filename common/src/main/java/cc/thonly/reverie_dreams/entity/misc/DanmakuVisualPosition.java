package cc.thonly.reverie_dreams.entity.misc;

import net.minecraft.world.phys.Vec3;

/**
 * Client presentation samples. The entity keeps its authoritative corrections
 * for simulation; only the rendered position eases out small packet jumps.
 */
public final class DanmakuVisualPosition {
    private static final int CORRECTION_TICKS = 3;
    private static final double MAX_OFFSET_SQUARED = 1.5 * 1.5;
    // Ignore fixed-point position packet rounding (1/4096 block per axis).
    private static final double ROUNDING_ERROR_SQUARED = 3.0 / (4096.0 * 4096.0);

    private Vec3 lastSimulatedPosition;
    private Vec3 previous;
    private Vec3 current;
    private int correctionTicks;

    DanmakuVisualPosition(Vec3 initialPosition) {
        this.lastSimulatedPosition = initialPosition;
        this.previous = initialPosition;
        this.current = initialPosition;
    }

    void sample(Vec3 start, Vec3 end) {
        // Only changes outside the entity tick are network corrections. Turns,
        // acceleration and intentional pauses are part of the simulated step.
        double jumpSquared = this.lastSimulatedPosition.distanceToSqr(start);
        this.lastSimulatedPosition = end;
        double offsetSquared = this.current.distanceToSqr(start);
        if (jumpSquared > MAX_OFFSET_SQUARED || offsetSquared > MAX_OFFSET_SQUARED) {
            this.previous = start;
            this.current = end;
            this.correctionTicks = 0;
            return;
        }
        if (jumpSquared > ROUNDING_ERROR_SQUARED) {
            this.correctionTicks = CORRECTION_TICKS;
        }
        this.previous = this.current;
        if (this.correctionTicks > 1) {
            double weight = (this.correctionTicks - 1.0) / this.correctionTicks;
            this.current = end.add(this.previous.subtract(start).scale(weight));
            this.correctionTicks--;
        } else {
            this.current = end;
            this.correctionTicks = 0;
        }
    }

    public Vec3 previous() {
        return this.previous;
    }

    public Vec3 current() {
        return this.current;
    }
}
