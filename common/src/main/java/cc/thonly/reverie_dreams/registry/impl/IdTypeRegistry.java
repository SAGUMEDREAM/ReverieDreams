package cc.thonly.reverie_dreams.registry.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class IdTypeRegistry<T> {
    private final Map<Identifier, T> contents = new Object2ObjectLinkedOpenHashMap<>(16);

    public T register(Identifier key, T value) {
        if (this.contents.containsKey(key) || this.contents.containsValue(value)) {
            throw new RuntimeException("Try registering duplicate key %s or value %s".formatted(key, value));
        }
        this.contents.put(key, value);
        return value;
    }

    public T get(Identifier key) {
        return this.contents.get(key);
    }

    public Set<Map.Entry<Identifier, T>> entries() {
        return this.contents.entrySet();
    }

    public Set<Identifier> keys() {
        return this.contents.keySet();
    }

    public Collection<T> values() {
        return this.contents.values();
    }

}
