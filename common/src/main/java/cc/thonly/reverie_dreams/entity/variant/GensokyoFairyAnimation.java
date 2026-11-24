package cc.thonly.reverie_dreams.entity.variant;

import net.minecraft.world.entity.Entity;

public enum GensokyoFairyAnimation {
    IDLE("animation.idle", 0),
    WALK("animation.walk", 0),
    FLY("animation.fly", 0),
    MELEE("animation.melee", 12),
    RANGED("animation.ranged", 18),
    HURT("animation.hurt", 10),
    DEATH("animation.death", 36);

    private final String animationName;
    private final int durationTicks;

    GensokyoFairyAnimation(String animationName, int durationTicks) {
        this.animationName = animationName;
        this.durationTicks = durationTicks;
    }

    public String animationName() {
        return this.animationName;
    }

    public int durationTicks() { return this.durationTicks; }
    public boolean looping() { return this.durationTicks == 0; }

    public static GensokyoFairyAnimation select(boolean grounded, boolean moving) {
        return !grounded ? FLY : moving ? WALK : IDLE;
    }

    public static GensokyoFairyAnimation of(Entity entity) {
        return select(entity.onGround() || entity.isInWater(),
                entity.getDeltaMovement().horizontalDistanceSqr() > 0.0001);
    }
}
