package cc.thonly.reverie_dreams.entity.ai.goal.work;

/**
 * Bounds retries for both failed path construction and paths that make no progress.
 */
final class WorkNavigationProgress {
    static final int STALL_TICKS = 100;
    private double bestDistance;
    private long progressTick;
    private int pathFailures;

    void reset(long gameTime, double distanceSquared) {
        this.bestDistance = distanceSquared;
        this.progressTick = gameTime;
        this.pathFailures = 0;
    }

    boolean stalled(long gameTime, double distanceSquared) {
        if (distanceSquared < this.bestDistance - 0.25) {
            this.bestDistance = distanceSquared;
            this.progressTick = gameTime;
        }
        return gameTime - this.progressTick >= STALL_TICKS;
    }

    boolean pathFailed(boolean pathStarted) {
        this.pathFailures = pathStarted ? 0 : this.pathFailures + 1;
        return this.pathFailures >= 3;
    }
}
