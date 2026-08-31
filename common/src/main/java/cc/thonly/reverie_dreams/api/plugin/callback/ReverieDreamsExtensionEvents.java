package cc.thonly.reverie_dreams.api.plugin.callback;

import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

import java.util.List;

public class ReverieDreamsExtensionEvents {
    public static final Event<EntryPoint> ADD_EVENT = EventFactory.of(
            (listeners) -> () -> {
                for (EntryPoint listener : listeners) {
                    ReverieDreamsPlugin plugin = listener.registerPlugin();
                    ReverieDreamsPluginLoader.registerPlugin(plugin);
                }
                return null;
            }
    );

    @FunctionalInterface
    public interface EntryPoint {
        ReverieDreamsPlugin registerPlugin();
    }

    public static final Event<FabricEntryPoint> SCAN_EVENT = EventFactory.of(
            (listeners) -> () -> {
                for (FabricEntryPoint listener : listeners) {
                    List<ReverieDreamsPlugin> plugins = listener.registerPlugin();
                    for (ReverieDreamsPlugin plugin : plugins) {
                        ReverieDreamsPluginLoader.registerPlugin(plugin);
                    }
                }
                return null;
            }
    );

    @FunctionalInterface
    public interface FabricEntryPoint {
        List<ReverieDreamsPlugin> registerPlugin();
    }
}
