package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.registry.BiRegistryImpls;
import com.sun.nio.sctp.IllegalUnbindException;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.Identifier;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class BiRegistryImpl<K, V> implements Serializable {
    @Getter
    private final Identifier key;
    @Setter
    @Getter
    private ReloadableFactory reloadableFactory = null;
    @Getter
    private final Class<K> klass;
    @Getter
    private final Class<V> vlass;

    @Getter(AccessLevel.PROTECTED)
    private final Map<Integer, V> rawToEntry;
    @Getter(AccessLevel.PROTECTED)
    private final Map<K, V> keyToEntry;
    @Getter(AccessLevel.PROTECTED)
    private final Map<V, K> entryToKey;

    protected BiRegistryImpl(Identifier key, Class<K> klass, Class<V> vlass) {
        if (key == null) {
            throw new IllegalArgumentException("Registry require a key, but it is null");
        }
        if (BiRegistryImpls.ROOT.containsKey(key)) {
            throw new IllegalUnbindException("Repeat the creation of the same named instance");
        }
        this.key = key;
        this.rawToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.keyToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.entryToKey = new Object2ObjectLinkedOpenHashMap<>();
        this.klass = klass;
        this.vlass = vlass;
        BiRegistryImpls.ROOT.put(key, this);
    }

    public static <K, V> BiRegistryImpl<K, V> createRegister(Identifier id, Class<K> klass, Class<V> vlass) {
        return new BiRegistryImpl<>(id, klass, vlass);
    }

    public static <K, V> BiRegistryImpl<K, V> of(Identifier id, Class<K> klass, Class<V> vlass) {
        return (BiRegistryImpl<K, V>) BiRegistryImpls.ROOT.computeIfAbsent(id, _ -> new BiRegistryImpl<>(id, klass, vlass));
    }

    public static <K, V> BiRegistryImpl<K, V> of(Class<K> klass, Class<V> vlass) {
        Set<Map.Entry<Identifier, BiRegistryImpl<?, ?>>> registries = BiRegistryImpls.ROOT.entrySet();
        for (Map.Entry<Identifier, BiRegistryImpl<?, ?>> entry : registries) {
            BiRegistryImpl<?, ?> registry = entry.getValue();
            if (registry.klass == klass && registry.vlass == vlass) {
                return (BiRegistryImpl<K, V>) registry;
            }
        }
        return new BiRegistryImpl<>(Identifier.parse(klass.getName().toLowerCase() + "_2_" + vlass.getName().toLowerCase()), klass, vlass);
    }

    public static <K, V> BiRegistryImpl<K, V> ofNullable(Class<K> klass, Class<V> vlass) {
        Set<Map.Entry<Identifier, BiRegistryImpl<?, ?>>> registries = BiRegistryImpls.ROOT.entrySet();
        for (Map.Entry<Identifier, BiRegistryImpl<?, ?>> entry : registries) {
            BiRegistryImpl<?, ?> registry = entry.getValue();
            if (registry.klass == klass && registry.vlass == vlass) {
                return (BiRegistryImpl<K, V>) registry;
            }
        }
        return null;

    }

    public V add(K key, V value) {
        if (this.keyToEntry.containsKey(key)) {
            throw new IllegalStateException("Duplicate key: " + key);
        }
        if (this.keyToEntry.containsValue(value)) {
            throw new IllegalStateException("Duplicate value: " + value);
        }
        int raw = this.rawToEntry.size();
        this.rawToEntry.put(raw, value);
        this.keyToEntry.put(key, value);
        this.entryToKey.put(value, key);
        return value;
    }

    public V get(K key) {
        return keyToEntry.get(key);
    }

    public K getKey(V value) {
        return this.entryToKey.get(value);
    }

    public Optional<V> getOptional(K key) {
        return Optional.ofNullable(get(key));
    }

    public Optional<K> getKeyOptional(V value) {
        return Optional.ofNullable(this.getKey(value));
    }

    public Stream<K> keysStream() {
        return this.keyToEntry.keySet().stream();
    }

    public Stream<V> valuesStream() {
        return this.keyToEntry.values().stream();
    }

    public Set<K> keys() {
        return Collections.unmodifiableSet(this.keyToEntry.keySet());
    }

    public Set<V> values() {
        return Set.copyOf(this.keyToEntry.values());
    }

    public BiRegistryImpl<K, V> reload(ReloadableFactory reloadableFactory) {
        this.reloadableFactory = reloadableFactory;
        return this;
    }

    public void reload() {
        if (this.reloadableFactory != null) {
            this.rawToEntry.clear();
            this.keyToEntry.clear();
            this.reloadableFactory.onReload();
        }
    }

    @FunctionalInterface
    public interface ReloadableFactory {
        void onReload();
    }
}
