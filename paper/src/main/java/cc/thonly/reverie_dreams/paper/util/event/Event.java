package cc.thonly.reverie_dreams.paper.util.event;

import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public abstract class Event<T> {
    public static List<Event> BUS = new ArrayList<>();
    protected volatile T invoker;
    public static final NamespacedKey DEFAULT_PHASE = new NamespacedKey("paper", "default");

    public final T invoker() {
        return this.invoker;
    }

    public abstract void register(T var1);

    public abstract void register(NamespacedKey var1, T var2);

    public abstract void addPhaseOrdering(NamespacedKey var1, NamespacedKey var2);

    public abstract boolean hasHandlers();

    public abstract void unbound();
}
