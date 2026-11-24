package cc.thonly.reverie_dreams.entity.misc.spark;

/** Continuous firing while the owner is available; acquiring a target never gates the beam. */
public final class MasterSparkCycle {
    public static final int LIFETIME_TICKS = 30 * 20;
    public static final int DAMAGE_INTERVAL = 10;
    public enum Phase { REST, FIRE }
    private Phase phase = Phase.REST;
    private int elapsed;
    public void tick(boolean ownerAvailable) {
        if (!ownerAvailable) {
            reset();
        } else if (phase == Phase.REST) {
            phase = Phase.FIRE;
            elapsed = 0;
        } else {
            elapsed++;
        }
    }

    public void reset() { phase = Phase.REST; elapsed = 0; }
    public Phase phase() { return phase; }
    public int elapsed() { return elapsed; }
    public boolean damagePulse() { return phase == Phase.FIRE && elapsed % DAMAGE_INTERVAL == 0; }
    public float charge() { return phase == Phase.FIRE ? 1 : 0; }
}
