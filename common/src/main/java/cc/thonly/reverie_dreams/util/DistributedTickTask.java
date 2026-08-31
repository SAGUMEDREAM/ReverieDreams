package cc.thonly.reverie_dreams.util;

import java.io.Closeable;

public class DistributedTickTask implements Closeable {
    private final Runnable action;
    private final int interval;
    private boolean closed = false;
    private int tick = 0;

    public DistributedTickTask(Runnable action, int interval) {
        this.action = action;
        this.interval = interval;
    }

    public void tick() {
        if (this.closed) {
            return;
        }
        this.tick++;
        if (this.tick >= this.interval) {
            this.action.run();
            this.tick = 0;
        }
    }

    public static DistributedTickTask createTickTask(Runnable action, int interval) {
        return new DistributedTickTask(action, interval);
    }

    @Override
    public void close() {
        this.closed = true;
    }
}
