package cc.thonly.reverie_dreams.entity.variant;

/** A single persisted evacuation clock drives both render paths. End bounds are exclusive. */
public enum RabbitUnitEvacuationPhase {
    STARTUP(0, 6), WARMUP(6, 16), TAKEOFF(16, 24), ASCENT(24, 40), DEPARTURE(40, 48);

    public static final int TOTAL_TICKS = 48;
    private final int start, end;
    RabbitUnitEvacuationPhase(int start, int end) { this.start = start; this.end = end; }
    public int startTick() { return this.start; }
    public int endTick() { return this.end; }
    public static RabbitUnitEvacuationPhase at(int ticks) {
        for (var phase : values()) if (ticks < phase.end) return phase;
        return DEPARTURE;
    }
}
