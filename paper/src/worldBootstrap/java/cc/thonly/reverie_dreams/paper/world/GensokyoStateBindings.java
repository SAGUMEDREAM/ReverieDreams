package cc.thonly.reverie_dreams.paper.world;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** Atomically published state table. Native objects remain private to the generator. */
final class GensokyoStateBindings<T> {
    private final List<String> requested;
    private final Set<String> customSurfaces;
    private volatile Map<String, T> states;
    private Map<String, String> installedEncodings = Map.of();

    GensokyoStateBindings(Set<String> requested, Set<String> customSurfaces, Map<String, T> nativeStates) {
        this.requested = List.copyOf(requested);
        this.customSurfaces = Set.copyOf(customSurfaces);
        if (!requested.containsAll(customSurfaces) || !requested.containsAll(nativeStates.keySet()))
            throw new IllegalArgumentException("Bindings must belong to the requested source palette");
        this.states = Map.copyOf(nativeStates);
    }

    static Set<String> customSurfaces(List<String> palette, List<Long> counts) {
        if (palette.size() != counts.size()) throw new IllegalArgumentException("Surface histogram/palette mismatch");
        var result = new LinkedHashSet<String>();
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) < 0) throw new IllegalArgumentException("Negative source histogram");
            if (counts.get(i) > 0 && !palette.get(i).startsWith("minecraft:")) result.add(palette.get(i));
        }
        return Set.copyOf(result);
    }

    Set<String> requestedCustomStates() { return customSurfaces; }
    Map<String, T> snapshot() { return states; }
    List<String> unresolved() {
        Map<String, T> current = states;
        return requested.stream().filter(source -> !current.containsKey(source)).toList();
    }

    synchronized void installVerifiedBindings(Map<String, String> sourceToNativeEncoded, Function<String, T> parser) {
        Map<String, String> supplied = Map.copyOf(Objects.requireNonNull(sourceToNativeEncoded));
        Objects.requireNonNull(parser);
        // Validate the complete request before parsing or publishing any entry.
        for (var entry : supplied.entrySet()) {
            String source = entry.getKey(), encoded = entry.getValue();
            if (!customSurfaces.contains(source)) throw new IllegalArgumentException("Not an active custom source surface: " + source);
            int colon = encoded.indexOf(':');
            if (encoded.isBlank() || colon < 1 || encoded.startsWith("minecraft:"))
                throw new IllegalArgumentException("Expected a real registered custom native state, without a vanilla alias: " + encoded);
            String previous = installedEncodings.get(source);
            if (previous != null && !previous.equals(encoded))
                throw new IllegalStateException("Refusing to change an installed native state identity: " + source);
        }
        var staged = new HashMap<>(states);
        var encodings = new HashMap<>(installedEncodings);
        for (var entry : supplied.entrySet()) {
            T parsed = Objects.requireNonNull(parser.apply(entry.getValue()), "Native parser returned null");
            staged.put(entry.getKey(), parsed);
            encodings.put(entry.getKey(), entry.getValue());
        }
        Map<String, T> complete = Map.copyOf(staged);
        installedEncodings = Map.copyOf(encodings);
        states = complete;
    }
}
