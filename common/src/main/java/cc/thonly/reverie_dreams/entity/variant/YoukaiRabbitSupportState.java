package cc.thonly.reverie_dreams.entity.variant;

/** Recruitment limits survive saves; summoned companions can never recruit recursively. */
public final class YoukaiRabbitSupportState {
    public static final int CALL_COOLDOWN = 6000;
    public static final int REINFORCEMENT_LIFETIME = 1200;
    private long nextCall;
    private boolean reinforcement;
    private long expires;

    public long nextCall() { return this.nextCall; }
    public boolean isReinforcement() { return this.reinforcement; }
    public long expires() { return this.expires; }
    public boolean claimCall(int nearbyCompanions, long now) {
        if (nearbyCompanions > 0 || this.reinforcement || now < this.nextCall) return false;
        this.nextCall = now + CALL_COOLDOWN;
        return true;
    }
    public void markReinforcement(long now) {
        this.reinforcement = true;
        this.expires = now + REINFORCEMENT_LIFETIME;
    }
    public boolean expired(long now) { return this.reinforcement && now >= this.expires; }
    public void restore(long nextCall, boolean reinforcement, long expires) {
        this.nextCall = Math.max(0, nextCall);
        this.reinforcement = reinforcement;
        this.expires = Math.max(0, expires);
    }
}
