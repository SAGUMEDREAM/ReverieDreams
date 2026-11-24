package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface BiRegistryProvider<K, V> {
    Identifier getKey();

    Class<K> getKlass();

    Class<V> getVlass();

    V add(K key, V value);

    V get(K key);

    K getKey(V value);

    Optional<V> getOptional(K key);

    Optional<K> getKeyOptional(V value);

    Stream<K> keysStream();

    Stream<V> valuesStream();

    Set<K> keys();

    Set<V> values();

    void reload();
}
