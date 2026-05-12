package cc.thonly.reverie_dreams.api.plugin.callback;

import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

import java.util.List;

public class ReverieDreamsExtensionEvents {
    public static final Event<EntryPoint> ADD_EVENT = EventFactory.createArrayBacked(EntryPoint.class,
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

    public static final Event<FabricEntryPoint> SCAN_EVENT = EventFactory.createArrayBacked(FabricEntryPoint.class,
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
