package cc.thonly.reverie_dreams.paper.util.event;

import org.bukkit.NamespacedKey;

import java.lang.reflect.Array;
import java.util.Arrays;

@SuppressWarnings("unchecked")
class EventPhaseData<T> extends SortableNode<EventPhaseData<T>> {
    final NamespacedKey id;
    T[] listeners;

    EventPhaseData(NamespacedKey id, Class<?> listenerClass) {
        this.id = id;
        this.listeners = (T[]) Array.newInstance(listenerClass, 0);
    }

    void addListener(T listener) {
        int oldLength = this.listeners.length;
        this.listeners = Arrays.copyOf(this.listeners, oldLength + 1);
        this.listeners[oldLength] = listener;
    }

    protected String getDescription() {
        return this.id.toString();
    }
}
