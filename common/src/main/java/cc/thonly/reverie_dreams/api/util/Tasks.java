package cc.thonly.reverie_dreams.api.util;

import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.server.MinecraftServer;

public class Tasks {

    public static Builder delay(MinecraftServer server, float ticks) {
        return new Builder(server, ticks);
    }

    public static class Builder {
        private final MinecraftServer server;
        private final float ticks;

        public Builder(MinecraftServer server, float ticks) {
            this.server = server;
            this.ticks = ticks;
        }

        public DelayedTask run(Runnable action) {
            return DelayedTask.create(server, ticks, action);
        }

        public DelayedTask runSeconds(float seconds, Runnable action) {
            return DelayedTask.createFromSecond(server, seconds, action);
        }
    }
}