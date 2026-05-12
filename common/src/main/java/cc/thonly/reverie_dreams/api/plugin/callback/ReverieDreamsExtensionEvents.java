package cc.thonly.reverie_dreams.api.plugin.callback;

import cc.thonly.reverie_dreams.api.plugin.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;

public class ReverieDreamsExtensionEvents {
    public static final Event<EntryPoint> EVENT = EventFactory.createArrayBacked(EntryPoint.class,
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
}
