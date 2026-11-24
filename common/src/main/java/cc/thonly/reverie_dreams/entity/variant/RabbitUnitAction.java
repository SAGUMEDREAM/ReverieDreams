package cc.thonly.reverie_dreams.entity.variant;

import java.util.Locale;


public enum RabbitUnitAction {
    IDLE(true), WALK(true), HOLD(true), GUN_WALK(true), SHOOT(false),
    RELOAD(false), GUARD(true), MELEE(false), HURT(false), EVACUATE(false),
    AIM(false), GUARD_HIT(false);

    private final boolean looping;
    RabbitUnitAction(boolean looping) { this.looping = looping; }
    public boolean looping() { return this.looping; }
    public String animationName() { return "animation." + this.name().toLowerCase(Locale.ROOT); }
}
