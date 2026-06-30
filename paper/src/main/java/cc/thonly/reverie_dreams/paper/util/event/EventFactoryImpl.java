package cc.thonly.reverie_dreams.paper.util.event;

import com.google.common.collect.MapMaker;
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public final class EventFactoryImpl {
    private static final Set<ArrayBackedEvent<?>> ARRAY_BACKED_EVENTS = Collections.newSetFromMap((new MapMaker()).weakKeys().makeMap());

    private EventFactoryImpl() {
    }

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        ArrayBackedEvent<T> event = new ArrayBackedEvent<>(type, invokerFactory);
        ARRAY_BACKED_EVENTS.add(event);
        return event;
    }

    public static void ensureContainsDefault(NamespacedKey[] defaultPhases) {
        for(NamespacedKey id : defaultPhases) {
            if (id.equals(Event.DEFAULT_PHASE)) {
                return;
            }
        }

        throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
    }

    public static void ensureNoDuplicates(NamespacedKey[] defaultPhases) {
        for(int i = 0; i < defaultPhases.length; ++i) {
            for(int j = i + 1; j < defaultPhases.length; ++j) {
                if (defaultPhases[i].equals(defaultPhases[j])) {
                    throw new IllegalArgumentException("Duplicate event phase: " + String.valueOf(defaultPhases[i]));
                }
            }
        }

    }
}
