package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.mixin.accessor.NamedAccessor;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.Initialization;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.ReloadStep;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Util;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("deprecation")
public class RegistryHandler<T> implements WritableRegistry<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final Map<Identifier, Holder.Reference<T>> idToEntry;
    private final Map<ResourceKey<T>, Holder.Reference<T>> keyToEntry;
    private final HashBiMap<Integer, Holder.Reference<T>> rawIdToEntry;
    private final Map<T, Holder.Reference<T>> valueToEntry;
    private final Map<ResourceKey<T>, RegistrationInfo> keyToEntryInfo;
    private final List<Initialization<T>> builders = new LinkedList<>();
    private final List<ReloadStep<T>> reloadableSteps = new LinkedList<>();
    private final Map<Identifier, T> builtins = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<TagKey<T>, HolderSet.Named<T>> tags = new Object2ObjectLinkedOpenHashMap<>();
    private final Lifecycle lifecycle;
    private Identifier defaultId;
    private boolean frozen = false;
    @Getter
    private boolean reloadable = false;
    @Getter
    private Codec<T> codec;
    @Nullable
    @Getter
    private RegistryHandler<T> parent;

    public RegistryHandler(ResourceKey<? extends Registry<T>> key) {
        this(key, Lifecycle.stable());
    }

    public RegistryHandler(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        this.key = key;
        this.lifecycle = lifecycle;
        this.idToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.keyToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.rawIdToEntry = HashBiMap.create();
        this.valueToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.keyToEntryInfo = new Object2ObjectLinkedOpenHashMap<>();
    }

    public RegistryHandler(ResourceKey<? extends Registry<T>> key, @Nullable RegistryHandler<T> parent) {
        this(key, Lifecycle.stable(), parent);
    }

    public RegistryHandler(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, @Nullable RegistryHandler<T> parent) {
        this(key, lifecycle);
        this.parent = parent;
    }

    @SuppressWarnings("ConstantValue")
    public void validate() {
        AtomicInteger next = new AtomicInteger();
        this.keyToEntry.forEach((registryKey, reference) -> {
            if (registryKey == null) {
                log.error("Can't verify registry key, rawId: {}", next.get());
                return;
            }
            if (reference == null) {
                log.error("Can't verify registry entry reference, registryKey: {}", registryKey);
                return;
            }
            if (reference.value() == null) {
                log.error("Can't verify registry entry value, registryKey: {}", registryKey);
                return;
            }
            next.getAndIncrement();
        });
    }

    public RegistryHandler<T> shadow() {
        return new RegistryHandler<>(this.key, this);
    }

    public Set<Map.Entry<Identifier, T>> idEntrySet() {
        return this.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().identifier(), Map.Entry::getValue)).entrySet();
    }

    public RegistryHandler<T> build() {
        if (this.frozen) {
            return this;
        }
        this.builders.forEach(step -> step.bootstrap(this));
        this.freeze();
        return this;
    }

    @SafeVarargs
    public final RegistryHandler<T> builder(Initialization<T>... initializations) {
        this.builders.addAll(Arrays.asList(initializations));
        return this;
    }

    @SafeVarargs
    public final RegistryHandler<T> reloadBuilder(ReloadStep<T>... steps) {
        this.reloadableSteps.addAll(Arrays.asList(steps));
        this.reloadable = true;
        return this;
    }

    public RegistryHandler<T> codec(Codec<T> codec) {
        this.codec = codec;
        return this;
    }

    public RegistryHandler<T> defaultId(Identifier defaultId) {
        this.defaultId = defaultId;
        return this;
    }

    public void clear() {
        this.idToEntry.clear();
        this.keyToEntry.clear();
        this.rawIdToEntry.clear();
        this.valueToEntry.clear();
        this.keyToEntryInfo.clear();
    }

    public void reload(ResourceManager manager) {
        this.idToEntry.clear();
        this.keyToEntry.clear();
        this.rawIdToEntry.clear();
        this.valueToEntry.clear();
        this.keyToEntryInfo.clear();
        for (Map.Entry<Identifier, T> ivMapEntry : this.builtins.entrySet()) {
            Identifier key = ivMapEntry.getKey();
            T value = ivMapEntry.getValue();
            this.register(ResourceKey.create(this.key, key), value, RegistrationInfo.BUILT_IN);
        }
        for (ReloadStep<T> step : this.reloadableSteps) {
            step.reload(manager);
        }
    }

    public Stream<Map.Entry<Identifier, T>> streamIdToValue() {
        return this.idToEntry.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().value())).entrySet().stream();
    }

    public Map<Integer, Holder.Reference<T>> getIdToEntryMap() {
        return Map.copyOf(this.rawIdToEntry);
    }

    public Set<Identifier> keys() {
        return new LinkedHashSet<>(this.idToEntry.keySet());
    }

    public Set<T> values() {
        return new LinkedHashSet<>(this.valueToEntry.keySet());
    }

    public void setBuiltin(Identifier id, T value) {
        if (this.builtins.containsKey(id) || this.builtins.containsValue(value)) {
            return;
        }
        this.builtins.put(id, value);
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> key, T value, RegistrationInfo info) {
        Identifier id = key.identifier();
        Holder.Reference<T> entry = createIntrusiveHolder(value);
        entry.bindKey(key);
        this.idToEntry.put(id, entry);
        this.keyToEntry.put(key, entry);
        this.rawIdToEntry.put(this.idToEntry.size() - 1, entry);
        this.valueToEntry.put(value, entry);
        this.keyToEntryInfo.put(key, info);
        if (value instanceof BuiltinObject) {
            this.builtins.put(id, value);
        }
        if (value instanceof OwnerBinding<?>) {
            //noinspection unchecked
            OwnerBinding<T> ownerBinding = (OwnerBinding<T>) value;
            ownerBinding.setOwner(this);
        }
        return entry;
    }

    public Holder.Reference<T> set(ResourceKey<T> key, T value, RegistrationInfo info) {
        if (!this.keyToEntry.containsKey(key)) {
            return this.register(key, value, info);
        }
        Identifier id = key.identifier();
        Optional<Holder.Reference<T>> oldEntry = this.get(id);
        if (oldEntry.isEmpty()) {
            log.error("Can't find prev value in registry {}", this.key);
            Optional<Holder.Reference<T>> defaultEntry = this.getAny();
            return defaultEntry.orElse(null);
        }

        this.builtins.remove(id);
        int oldRawId = this.rawIdToEntry.inverse().get(oldEntry.get());
        this.rawIdToEntry.remove(oldRawId);

        Holder.Reference<T> entry = createIntrusiveHolder(value);
        entry.bindKey(key);
        this.idToEntry.put(id, entry);
        this.keyToEntry.put(key, entry);
        this.rawIdToEntry.put(oldRawId, entry);
        this.valueToEntry.put(value, entry);
        this.keyToEntryInfo.put(key, info);
        if (value instanceof BuiltinObject) {
            this.builtins.put(id, value);
        }
        return entry;
    }

    @Override
    public void bindTag(TagKey<T> tag, List<Holder<T>> registryEntries) {
        HolderSet.Named<T> entryListNamed = NamedAccessor.callNew(this, tag);
        this.tags.put(tag, entryListNamed);
        entryListNamed.contents = new ArrayList<>(registryEntries);
    }

    @Override
    public boolean isEmpty() {
        return this.keyToEntry.isEmpty();
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        return new HolderGetter<>() {
            public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
                return Optional.of(this.getOrThrow(key));
            }

            public Holder.Reference<T> getOrThrow(ResourceKey<T> key) {
                return RegistryHandler.this.getOrCreateEntry(key);
            }

            public Optional<HolderSet.Named<T>> get(TagKey<T> tag) {
                return Optional.of(this.getOrThrow(tag));
            }

            public HolderSet.Named<T> getOrThrow(TagKey<T> tag) {
                return RegistryHandler.this.getTag(tag);
            }
        };
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.lifecycle;
    }

    @Override
    public @Nullable Identifier getKey(T value) {
        for (Map.Entry<Identifier, Holder.Reference<T>> mapEntry : this.idToEntry.entrySet()) {
            if (mapEntry.getValue().value().equals(value)) {
                return mapEntry.getKey();
            }
        }
        return null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T value) {
        Holder.Reference<T> ref = this.valueToEntry.get(value);
        return ref != null && ref.isBound() ? Optional.of(ref.key()) : Optional.empty();
    }

    @Override
    public int getId(@Nullable T value) {
        @SuppressWarnings("DataFlowIssue") Optional<Holder.Reference<T>> entry = this.get(this.getKey(value));
        if (entry.isEmpty()) {
            return -1;
        }
        return this.rawIdToEntry.inverse().get(entry.get());
    }

    @Override
    public @Nullable T byId(int index) {
        Holder.Reference<T> tReference = this.rawIdToEntry.get(index);
        if (tReference == null) {
            return null;
        }
        return tReference.value();
    }

    @Override
    public int size() {
        return this.keyToEntry.size();
    }

    @Override
    public @Nullable T getValue(@Nullable ResourceKey<T> key) {
        Holder.Reference<T> entry = this.keyToEntry.get(key);
        if (entry == null) {
            Holder.Reference<T> tReference = this.getAny().orElse(null);
            if (tReference == null) {
                return null;
            }
            return tReference.value();
        }
        return entry.value();
    }

    @Override
    public @Nullable T getValue(@Nullable Identifier id) {
        if (id == null) {
            return this.getAny().map(Holder::value).orElse(null);
        }
        Holder.Reference<T> ref = this.idToEntry.get(id);
        if (ref != null) {
            return ref.value();
        }
        return this.getAny().map(Holder::value).orElse(null);
    }


    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> key) {
        return Optional.ofNullable(this.keyToEntryInfo.get(key));
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return this.get(this.defaultId);
    }

    @Override
    public Set<Identifier> keySet() {
        return Set.copyOf(this.idToEntry.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Util.mapValuesLazy(this.keyToEntry, Holder::value).entrySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Set.copyOf(this.keyToEntry.keySet());
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource random) {
        return Util.getRandomSafe(this.idToEntry.values().stream().toList(), random);
    }

    @Override
    public boolean containsKey(Identifier id) {
        return this.idToEntry.containsKey(id);
    }

    @Override
    public boolean containsKey(ResourceKey<T> key) {
        return this.keyToEntry.containsKey(key);
    }

    @Override
    public Registry<T> freeze() {
        this.frozen = true;
        return this;
    }

    public Registry<T> unfreeze() {
        this.frozen = false;
        return this;
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T value) {
        return Holder.Reference.createIntrusive(this, value);
    }

    Holder.Reference<T> getOrCreateEntry(ResourceKey<T> key) {
        return this.keyToEntry.computeIfAbsent(key, (key2) -> Holder.Reference.createStandAlone(this, key2));
    }

    HolderSet.Named<T> getTag(TagKey<T> key) {
        return this.tags.computeIfAbsent(key, this::createNamedEntryList);
    }

    private HolderSet.Named<T> createNamedEntryList(TagKey<T> tag) {
        return NamedAccessor.callNew(this, tag);
    }

    @Override
    public Optional<Holder.Reference<T>> get(int rawId) {
        return Optional.ofNullable(this.rawIdToEntry.get(rawId));
    }

    @Override
    public Optional<Holder.Reference<T>> get(Identifier id) {
        return Optional.ofNullable(this.idToEntry.get(id));
    }

    @Override
    public Holder<T> wrapAsHolder(T value) {
        Holder.Reference<T> reference = this.valueToEntry.get(value);
        return (reference != null ? reference : Holder.direct(value));
    }

    @Override
    public Stream<HolderSet.Named<T>> getTags() {
        return Stream.empty();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> tags) {
        return null;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.keyToEntry.values().stream().map(Holder::value).iterator();
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return this.keyToEntry.values().stream();
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return this.tags.values().stream();
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
        return this.get(key.identifier());
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> tag) {
        return Optional.ofNullable(this.tags.get(tag));
    }
}
