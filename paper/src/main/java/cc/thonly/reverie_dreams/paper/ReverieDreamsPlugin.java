package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.registry.InitHolder;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("LombokGetterMayBeUsed")
public final class ReverieDreamsPlugin extends JavaPlugin {
    private static ReverieDreamsPlugin INSTANCE;
    private final Object lock = new Object();
    private DefaultEventBusImpl bus;

    public ReverieDreamsPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.bus = new DefaultEventBusImpl(this);
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

    public DefaultEventBusImpl getBus() {
        return this.bus;
    }

    public static ReverieDreamsPlugin getMod() {
        return INSTANCE;
    }

}
