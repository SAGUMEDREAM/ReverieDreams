package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.server.ItemInventoryTickManager;
import cc.thonly.reverie_dreams.paper.util.event.Event;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

@SuppressWarnings("FieldCanBeLocal")
public class DefaultEventBusImpl {
    private final JavaPlugin plugin;
    private final Server server;
    private final BukkitScheduler scheduler;
    private BukkitTask tickEvent;

    public DefaultEventBusImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.server = Bukkit.getServer();
        this.scheduler = this.server.getScheduler();
        this.tickEvent = null;
    }

    public void start() {
        if (this.tickEvent != null) {
            return;
        }
        this.tickEvent = this.scheduler.runTaskTimer(this.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemInventoryTickManager.INSTANCE.invokePlayer(player);
            }
        }, 0L, 1L);
    }

    public void stop() {
        if (this.tickEvent != null) {
            this.tickEvent.cancel();
            this.tickEvent = null;
        }
        Event.BUS.forEach(Event::unbound);
    }
}
