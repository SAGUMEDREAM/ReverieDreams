package cc.thonly.reverie_dreams.entity.variant;

/** Shared visual state for GeckoLib and the vanilla-client model; never drives movement or damage. */
public final class HairballAnimationState {
    public enum Clip {
        IDLE("idle", true), WALK("walk", true), ATTACK("attack", false),
        JUMP("jump", false), FALL("fall", false), LAND("land", false), HURT("hurt", false);

        private final String animation;
        private final boolean looping;

        Clip(String animation, boolean looping) {
            this.animation = animation;
            this.looping = looping;
        }

        public String animation() { return this.animation; }
        public boolean looping() { return this.looping; }
    }

    public static final int ATTACK_TICKS = 8;
    public static final int LAND_TICKS = 6;
    private int attackUntil;
    private int landUntil;
    private int airborneTicks;
    private int previousHurtTime;
    private int revision;
    private Clip clip = Clip.IDLE;

    public void beginAttack(int tick) {
        if (this.previousHurtTime > 0) return;
        this.attackUntil = tick + ATTACK_TICKS;
        this.revision++;
    }

    public void update(int tick, boolean grounded, boolean inFluid, double verticalSpeed,
                       double horizontalSpeedSquared, int hurtTime, boolean dying) {
        if (hurtTime > this.previousHurtTime) {
            this.revision++;
            this.attackUntil = 0;
        }
        this.previousHurtTime = hurtTime;
        if (inFluid || dying) {
            this.airborneTicks = 0;
            this.landUntil = 0;
        } else if (!grounded) {
            this.airborneTicks++;
            this.landUntil = 0;
        } else {
            if (this.airborneTicks >= 2) this.landUntil = tick + LAND_TICKS;
            this.airborneTicks = 0;
        }

        if (dying) {
            this.attackUntil = 0;
            this.clip = Clip.IDLE;
        } else if (hurtTime > 0) {
            this.clip = Clip.HURT;
        } else if (tick < this.attackUntil) {
            this.clip = Clip.ATTACK;
        } else if (!grounded && !inFluid) {
            this.clip = verticalSpeed > 0.05 ? Clip.JUMP : Clip.FALL;
        } else if (tick < this.landUntil) {
            this.clip = Clip.LAND;
        } else {
            this.clip = horizontalSpeedSquared > 0.0001 ? Clip.WALK : Clip.IDLE;
        }
    }

    public Clip clip() { return this.clip; }
    public int revision() { return this.revision; }

    /** Twelve-tick cycle at the usual speed, with restrained timing at either extreme. */
    public static double walkSpeed(double horizontalSpeed) {
        return Math.clamp(horizontalSpeed / 0.09, 0.65, 1.65);
    }
}
