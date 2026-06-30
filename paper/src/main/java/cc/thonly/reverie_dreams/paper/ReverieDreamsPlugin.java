package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.registry.InitHolder;
import cc.thonly.reverie_dreams.paper.server.ItemInventoryTickManager;
import cc.thonly.reverie_dreams.paper.util.event.Event;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.libraries.adventure.key.KeyPattern;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("LombokGetterMayBeUsed")
public final class ReverieDreamsPlugin extends JavaPlugin {
    public static final String MOD_ID = "reverie_dreams";
    private static ReverieDreamsPlugin INSTANCE;
    private final Object lock = new Object();
    private EventBus bus;

    public ReverieDreamsPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.bus = new EventBus(this);
        synchronized (lock) {
            InitHolder.initialize();
        }
        this.bus.start();
    }

    @Override
    public void onDisable() {
        this.bus.stop();
        this.bus = null;
    }

    public EventBus getBus() {
        return this.bus;
    }

    public static NamespacedKey id(String path) {
        return new NamespacedKey(MOD_ID, path);
    }

    public static Key key(@KeyPattern.Value final @NotNull String path) {
        return new Key(MOD_ID, path);
    }

    public static ReverieDreamsPlugin getMod() {
        return INSTANCE;
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class EventBus {
        private final JavaPlugin plugin;
        private final Server server;
        private final BukkitScheduler scheduler;
        private BukkitTask tickEvent;

        public EventBus(JavaPlugin plugin) {
            this.plugin = plugin;
            this.server = Bukkit.getServer();
            this.scheduler = this.server.getScheduler();
            this.tickEvent = null;
        }

        public void start() {
            this.tickEvent = this.scheduler.runTaskTimer(this.plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemInventoryTickManager.INSTANCE.invokePlayer(player);
                }
            }, 0L, 1L);
        }

        public void stop() {
            Event.BUS.forEach(Event::unbound);
            this.tickEvent.cancel();
        }
    }

}
