package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"LombokGetterMayBeUsed", "unchecked", "unused", "LombokSetterMayBeUsed"})
public class NeoRegistryProvider<T> extends RegistryProvider<T> {
    protected final List<AddCallback<T>> addCallbacks = new ArrayList<>();
    protected final List<BakeCallback<T>> bakeCallbacks = new ArrayList<>();
    protected final List<ClearCallback<T>> clearCallbacks = new ArrayList<>();
    final Map<Identifier, Identifier> aliases = new HashMap<>();
    final Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> dataMaps = new IdentityHashMap<>();

    private int maxId = Integer.MAX_VALUE - 1;
    private boolean sync;

    public NeoRegistryProvider(ResourceKey<? extends Registry<T>> key) {
        super(key);
    }

    public NeoRegistryProvider(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    public NeoRegistryProvider(ResourceKey<? extends Registry<T>> key, @org.jetbrains.annotations.Nullable RegistryProvider<T> parent) {
        super(key, parent);
    }

    public NeoRegistryProvider(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, @org.jetbrains.annotations.Nullable RegistryProvider<T> parent) {
        super(key, lifecycle, parent);
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public boolean doesSync() {
        return this.sync;
    }

    public void setMaxId(int maxId) {
        this.maxId = maxId;
    }

    @Override
    public int getMaxId() {
        return this.maxId;
    }

    @Override
    public void addCallback(RegistryCallback<T> callback) {
        if (callback instanceof AddCallback<T> addCallback)
            this.addCallbacks.add(addCallback);
        if (callback instanceof BakeCallback<T> bakeCallback)
            this.bakeCallbacks.add(bakeCallback);
        if (callback instanceof ClearCallback<T> clearCallback)
            this.clearCallbacks.add(clearCallback);
    }

    @Override
    public <C extends RegistryCallback<T>> void addCallback(Class<C> type, C callback) {
        super.addCallback(type, callback);
    }

    @Override
    public void addAlias(Identifier from, Identifier to) {
        if (from.equals(to))
            return;
        if (this.aliases.containsKey(from)) {
            Identifier old = this.aliases.get(from);
            if (!old.equals(to))
                throw new IllegalStateException("Duplicate alias with key \"" + from + "\" attempting to map to \"" + to + "\", found existing mapping \"" + old + "\"");
        }
        if (resolve(from).equals(to))
            throw new IllegalStateException("Infinite alias loop detected: from " + from + " to " + to);
        this.aliases.put(from, to);
    }

    @Override
    public void addAliaName(Identifier from, Identifier to) {
        this.addAlias(from, to);
    }

    @Override
    public Identifier resolve(Identifier name) {
        if (this.containsKey(name))
            return name;

        Identifier alias = this.aliases.get(name);
        if (alias == null)
            return name;

        return resolve(alias);
    }

    @Override
    public ResourceKey<T> resolve(ResourceKey<T> key) {
        Identifier resolvedName = resolve(key.identifier());
        // Try to reuse the key if possible
        return resolvedName == key.identifier() ? key : ResourceKey.create(this.key(), resolvedName);
    }

    @Override
    public Identifier resolveId(Identifier name) {
        return this.resolve(name);
    }

    @Override
    public ResourceKey<T> resolveKey(ResourceKey<T> key) {
        return this.resolve(key);
    }

    @Override
    public int getId(ResourceKey<T> key) {
        T value = this.getValue(key);
        return value == null ? -1 : this.getId(value);
    }

    @Override
    public int getId(Identifier name) {
        T value = this.getValue(name);
        return value == null ? -1 : this.getId(value);
    }

    @Override
    public boolean containsValue(T value) {
        return this.values().contains(value);
    }

    protected void clear(boolean full) {
        this.aliases.clear();
        if (full) {
            this.dataMaps.clear();
        }
    }

    @Override
    public <A> @Nullable A getData(DataMapType<T, A> type, ResourceKey<T> key) {
        final var innerMap = this.dataMaps.get(type);
        return innerMap == null ? null : (A) innerMap.get(key);
    }

    @Override
    public <A> Map<ResourceKey<T>, A> getDataMap(DataMapType<T, A> type) {
        return (Map<ResourceKey<T>, A>) this.dataMaps.getOrDefault(type, Map.of());
    }

    @Override
    public @Nullable Identifier getKeyOrNull(T element) {
        return super.getKeyOrNull(element);
    }

    public Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> getDataMaps() {
        return this.dataMaps;
    }
}
