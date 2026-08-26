package cc.thonly.reverie_dreams.util;

import lombok.Getter;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NamespacedKey {
    private static final Map<String, NamespacedKey> INSTANCES = new ConcurrentHashMap<>();

    private final String namespace;
    private final String path;

    private NamespacedKey(String namespace, String path) {
        this.namespace = namespace.toLowerCase();
        this.path = path.toLowerCase();
    }

    public static NamespacedKey of(String namespace, String path) {
        String key = namespace + ":" + path;
        return INSTANCES.computeIfAbsent(key, k -> new NamespacedKey(namespace, path));
    }

    public static NamespacedKey of(Identifier id) {
        return of(id.getNamespace(), id.getPath());
    }

    public static NamespacedKey ofVanilla(String path) {
        return of("minecraft", path);
    }

    public static NamespacedKey of(String id) {
        String[] split = id.split(":", 2);
        if (split.length == 2) {
            return of(split[0], split[1]);
        } else {
            return of("minecraft", split[0]);
        }
    }

    public Identifier build() {
        return Identifier.fromNamespaceAndPath(this.namespace, this.path);
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NamespacedKey other)) return false;
        return this.namespace.equals(other.namespace) && this.path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
