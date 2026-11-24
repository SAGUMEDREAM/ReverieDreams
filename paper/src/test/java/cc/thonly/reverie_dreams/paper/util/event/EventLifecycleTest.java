package cc.thonly.reverie_dreams.paper.util.event;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EventLifecycleTest {
    @Test
    void clearedListenersDoNotReturnWhenRegisteringAgain() {
        List<String> calls = new ArrayList<>();
        Event<Runnable> event = newEvent();
        event.register(() -> calls.add("old"));
        event.invoker().run();

        event.unbound();
        event.unbound();
        assertFalse(event.hasHandlers());
        event.invoker().run();
        event.register(() -> calls.add("new"));
        event.invoker().run();

        assertEquals(List.of("old", "new"), calls);
    }

    @Test
    void clearingAnEventAlsoReleasesItsOrderedPhases() {
        List<String> calls = new ArrayList<>();
        Event<Runnable> event = newEvent();
        NamespacedKey before = new NamespacedKey("reverie_dreams", "before");
        NamespacedKey after = new NamespacedKey("reverie_dreams", "after");
        event.addPhaseOrdering(before, after);
        event.register(after, () -> calls.add("after"));
        event.register(before, () -> calls.add("before"));
        event.invoker().run();

        event.unbound();
        event.register(after, () -> calls.add("replacement"));
        event.invoker().run();

        assertEquals(List.of("before", "after", "replacement"), calls);
    }

    private static Event<Runnable> newEvent() {
        return EventFactory.createArrayBacked(Runnable.class, listeners -> () -> {
            for (Runnable listener : listeners) {
                listener.run();
            }
        });
    }
}
