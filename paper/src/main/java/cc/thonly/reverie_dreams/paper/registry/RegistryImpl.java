package cc.thonly.reverie_dreams.paper.registry;

import net.momirealms.craftengine.core.registry.AbstractMappedRegistry;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryImpl<T> extends AbstractMappedRegistry<T> {
    private ResourceKey<T> defaultKey = null;
    private final Map<Key, T> byKey;

    public RegistryImpl(ResourceKey<? extends Registry<T>> key) {
        super(key, 128, true);
        this.byKey = new ConcurrentHashMap<>(128);
    }

    public RegistryImpl<T> defaultKey(ResourceKey<T> defaultKey) {
        this.defaultKey = defaultKey;
        return this;
    }

    @Override
    public Holder.Reference<T> registerForHolder(@NotNull ResourceKey<T> key) {
        if (!key.registry().equals(this.key.location())) {
            throw new IllegalStateException(key + " is not allowed to be registered in " + this.key);
        } else if (this.byIdentifier.containsKey(key.location())) {
            throw new IllegalStateException("Adding duplicate key '" + key + "' to registry");
        } else {
            Holder.Reference<T> reference = this.byResourceKey.computeIfAbsent(key, (k) -> Holder.Reference.create(this, k));
            this.byResourceKey.put(key, reference);
            this.byIdentifier.put(key.location(), reference);
            this.byKey.put(key.location(), reference.value());
            this.byId.add(reference);
            return reference;
        }
    }

    @Override
    public Holder.Reference<T> register(@NotNull ResourceKey<T> key, T value) {
        if (!key.registry().equals(super.key.location())) {
            throw new IllegalStateException(key + " is not allowed to be registered in " + this.key);
        } else if (this.byIdentifier.containsKey(key.location())) {
            throw new IllegalStateException("Adding duplicate key '" + key + "' to registry");
        } else {
            Holder.Reference<T> reference = this.byResourceKey.computeIfAbsent(key, (k) -> Holder.Reference.createConstant(this, k, value));
            this.byResourceKey.put(key, reference);
            this.byIdentifier.put(key.location(), reference);
            this.byId.add(reference);
            return reference;
        }
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
        Optional<Holder.Reference<T>> optionalHolder = super.get(key);
        if (optionalHolder.isEmpty()) {
            return super.get(key);
        }
        return optionalHolder;
    }

    @Override
    public Optional<Holder.Reference<T>> get(Key id) {
        Optional<Holder.Reference<T>> optionalHolder = super.get(id);
        if (optionalHolder.isEmpty()) {
            Holder.Reference<T> defaultReference = this.byIdentifier.get(id);
            if (defaultReference == null) {
                return Optional.empty();
            }
            return Optional.of(defaultReference);
        }
        return optionalHolder;
    }

    @Override
    public @Nullable T getValue(@org.jetbrains.annotations.Nullable Key id) {
        T value = super.getValue(id);
        if (value == null) {
            Holder.Reference<T> defaultReference = this.byIdentifier.get(this.defaultKey.location());
            if (defaultReference == null) {
                return null;
            }
            return defaultReference.value();
        }
        return value;
    }

    @Override
    public @Nullable T getValue(@org.jetbrains.annotations.Nullable ResourceKey<T> key) {
        T value = super.getValue(key);
        if (value == null) {
            Holder.Reference<T> defaultReference = this.byResourceKey.get(this.defaultKey);
            if (defaultReference == null) {
                return null;
            }
            return defaultReference.value();
        }
        return value;
    }

    @Override
    public int getId(T value) {
        for (int i = 0; i < this.byId.size(); i++) {
            if (Objects.equals(this.byId.get(i), value)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Key getKey(T value) {
        for (Map.Entry<Key, T> keyTEntry : this.byKey.entrySet()) {
            if (Objects.equals(keyTEntry.getValue(), value)) {
                return keyTEntry.getKey();
            }
        }
        if (this.defaultKey == null) {
            return null;
        }
        return this.defaultKey.location();
    }
}
