package cc.thonly.reverie_dreams.data.danmaku;

/**
 * Per-item-use spawn budget. This is intentionally separate from the spellcard
 * STG director: single-item trajectories are instant volleys, not patterns.
 */
public final class DanmakuVolleyBudget {
    public static final int MAX_BULLETS_PER_USE = 64;

    private static final ThreadLocal<Integer> REMAINING = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);

    private DanmakuVolleyBudget() {
    }

    public static void begin(int max) {
        REMAINING.set(Math.max(0, max));
    }

    public static boolean consume() {
        int remaining = REMAINING.get();
        if (remaining <= 0) {
            return false;
        }
        if (remaining != Integer.MAX_VALUE) {
            REMAINING.set(remaining - 1);
        }
        return true;
    }

    public static void end() {
        REMAINING.remove();
    }
}
