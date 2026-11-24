package cc.thonly.reverie_dreams.entity;

/** Visual clocks only. Navigation, velocity, action revisions and attack timing are owned by the entity. */
final class TenguLocomotion {
    private TenguLocomotion() {}

    static final class Gait {
        private static final double REST_PHASE = .25;
        private static final double STRIDE = 3 * Math.sin(Math.toRadians(25));
        private static final double START_DISTANCE = .0075;
        private static final double KEEP_DISTANCE = .003;
        private double previous = REST_PHASE;
        private double advance;
        private int quietTicks = 2;

        void update(double distance, double scale, boolean grounded) {
            if (!grounded || !Double.isFinite(distance) || distance > Math.max(1, scale * .85)) {
                this.previous = REST_PHASE;
                this.advance = 0;
                this.quietTicks = 2;
                return;
            }
            this.previous = this.phase(1);
            if (distance > (this.active() ? KEEP_DISTANCE : START_DISTANCE) * scale) {
                this.quietTicks = 0;
                this.advance = Math.min(.5, distance / Math.max(.1, STRIDE * scale));
            } else {
                this.advance = 0;
                // A missed position update can hold a pose for one tick, but must never add a step.
                if (++this.quietTicks >= 2) {
                    this.quietTicks = 2;
                    this.previous = REST_PHASE;
                }
            }
        }

        boolean active() {
            return this.quietTicks < 2;
        }

        double phase(double partialTick) {
            return (this.previous + this.advance * Math.clamp(partialTick, 0, 1)) % 1;
        }
    }

    static final class Flight {
        static final int TAKEOFF_TICKS = 8;
        static final int LAND_TICKS = 8;
        private static final int FLAP_TICKS = 12;
        private static final int GLIDE_TICKS = 12;

        enum Phase {
            GROUND(null), TAKEOFF("takeoff"), FLAP("fly"), GLIDE("glide"), LAND("land");

            final String clip;

            Phase(String clip) {
                this.clip = clip;
            }
        }

        private Phase phase = Phase.GROUND;
        private int elapsed;
        private int groundTicks;
        private int airTicks;
        private int glideSignalTicks;
        private double rise;
        private boolean hadGroundContact;

        void update(boolean grounded, boolean eligible, double horizontal, double vertical) {
            if (!eligible || !Double.isFinite(horizontal) || !Double.isFinite(vertical)
                    || horizontal > 1 || Math.abs(vertical) > 1) {
                this.enter(Phase.GROUND);
                this.groundTicks = this.airTicks = 0;
                this.rise = 0;
                this.hadGroundContact = false;
                return;
            }
            if (this.elapsed < FLAP_TICKS) this.elapsed++;
            if (grounded) {
                this.airTicks = 0;
                this.rise = 0;
                if (++this.groundTicks >= 2) {
                    this.groundTicks = 2;
                    this.hadGroundContact = true;
                    if (this.airborne()) this.enter(Phase.LAND);
                    else if (this.phase == Phase.LAND && this.elapsed >= LAND_TICKS) this.enter(Phase.GROUND);
                }
                return;
            }

            this.groundTicks = 0;
            if (this.airTicks < 4) {
                this.airTicks++;
                this.rise += vertical;
            }
            switch (this.phase) {
                case GROUND, LAND -> {
                    // A rise from confirmed ground is a launch. A fall or a crow first seen in air skips it.
                    if (this.hadGroundContact && this.airTicks >= 2 && this.rise > .045) {
                        this.enter(Phase.TAKEOFF);
                    } else if (this.airTicks >= 4) {
                        this.enter(Phase.FLAP);
                    }
                }
                case TAKEOFF -> {
                    if (this.elapsed >= TAKEOFF_TICKS) this.enter(Phase.FLAP);
                }
                case FLAP -> {
                    boolean coasting = horizontal >= .075 && vertical >= -.085 && vertical <= .035;
                    this.glideSignalTicks = coasting ? Math.min(4, this.glideSignalTicks + 1) : 0;
                    if (this.elapsed >= FLAP_TICKS && this.glideSignalTicks >= 4) this.enter(Phase.GLIDE);
                }
                case GLIDE -> {
                    boolean needsStroke = horizontal < .05 || vertical < -.16 || vertical > .085;
                    this.glideSignalTicks = needsStroke ? this.glideSignalTicks + 1 : 0;
                    if (this.elapsed >= GLIDE_TICKS || this.glideSignalTicks >= 2) this.enter(Phase.FLAP);
                }
            }
        }

        private void enter(Phase phase) {
            this.phase = phase;
            this.elapsed = this.glideSignalTicks = 0;
        }

        Phase phase() {
            return this.phase;
        }

        int elapsed() {
            return this.elapsed;
        }

        boolean airborne() {
            return this.phase == Phase.TAKEOFF || this.phase == Phase.FLAP || this.phase == Phase.GLIDE;
        }
    }
}
