package cc.thonly.reverie_dreams.api.server;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;

public interface ServerEvents {
    Event<Reloading> RELOADING = EventFactory.of(listeners -> (server, resources) -> {
        for (Reloading listener : listeners) {
            listener.handle(server, resources);
        }
    });
    Event<Reloaded> RELOADED = EventFactory.of(listeners -> server -> {
        for (Reloaded listener : listeners) {
            listener.handle(server);
        }
    });

    Event<Saving> SAVING_BEFORE = EventFactory.of(listeners -> (server, flush, force) -> {
        for (Saving listener : listeners) {
            listener.handle(server, flush, force);
        }
    });
    Event<Saving> SAVING_AFTER = EventFactory.of(listeners -> (server, flush, force) -> {
        for (Saving listener : listeners) {
            listener.handle(server, flush, force);
        }
    });

    @FunctionalInterface
    public interface Reloaded {
        void handle(MinecraftServer server);
    }

    @FunctionalInterface
    public interface Reloading {
        void handle(MinecraftServer server, ReloadableServerResources resources);
    }

    @FunctionalInterface
    public interface Saving {
        void handle(MinecraftServer server, boolean flush, boolean force);
    }
}
