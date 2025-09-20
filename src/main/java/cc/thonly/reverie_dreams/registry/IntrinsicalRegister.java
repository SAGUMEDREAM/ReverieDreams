package cc.thonly.reverie_dreams.registry;

import cc.thonly.registry_modifier.mixin.NamedAccessor;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("deprecation")
public class IntrinsicalRegister<T> implements MutableRegistry<T> {
    private final RegistryKey<? extends Registry<T>> key;
    private final Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    private final Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    private final HashBiMap<Integer, RegistryEntry.Reference<T>> rawIdToEntry;
    private final Map<T, RegistryEntry.Reference<T>> valueToEntry;
    private final Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;
    private final List<Initialization<T>> builders = new LinkedList<>();
    private final List<ReloadStep<T>> reloadableSteps = new LinkedList<>();
    private final Map<Identifier, T> builtins = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<TagKey<T>, RegistryEntryList.Named<T>> tags = new Object2ObjectLinkedOpenHashMap<>();
    private final Lifecycle lifecycle;
    private Identifier defaultId;
    private boolean frozen = false;
    @Getter
    private boolean reloadable = false;
    @Getter
    private Codec<T> codec;
    @Nullable
    @Getter
    private IntrinsicalRegister<T> parent;

    public IntrinsicalRegister(RegistryKey<? extends Registry<T>> key) {
        this(key, Lifecycle.stable());
    }

    public IntrinsicalRegister(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        this.key = key;
        this.lifecycle = lifecycle;
        this.idToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.keyToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.rawIdToEntry = HashBiMap.create();
        this.valueToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.keyToEntryInfo = new Object2ObjectLinkedOpenHashMap<>();
    }

    public IntrinsicalRegister(RegistryKey<? extends Registry<T>> key, @Nullable IntrinsicalRegister<T> parent) {
        this(key, Lifecycle.stable(), parent);
    }

    public IntrinsicalRegister(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, @Nullable IntrinsicalRegister<T> parent) {
        this(key, lifecycle);
        this.parent = parent;
    }

    public void verify() {
        AtomicInteger next = new AtomicInteger();
        this.keyToEntry.forEach((registryKey, reference) -> {
            if (registryKey == null)  {
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

    public IntrinsicalRegister<T> shadow() {
        return new IntrinsicalRegister<>(this.key, this);
    }

    public Set<Map.Entry<Identifier, T>> entrySet() {
        return this.getEntrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getValue(),
                        Map.Entry::getValue
                )).entrySet();
    }

    public IntrinsicalRegister<T> build() {
        if (this.frozen) {
            return this;
        }
        this.builders.forEach(step -> step.bootstrap(this));
        this.freeze();
        return this;
    }

    public IntrinsicalRegister<T> builder(Initialization<T>... initializations) {
        this.builders.addAll(Arrays.asList(initializations));
        return this;
    }

    public IntrinsicalRegister<T> reloadBuilder(ReloadStep<T>... steps) {
        this.reloadableSteps.addAll(Arrays.asList(steps));
        this.reloadable = true;
        return this;
    }

    public IntrinsicalRegister<T> codec(Codec<T> codec) {
        this.codec = codec;
        return this;
    }

    public IntrinsicalRegister<T> defaultId(Identifier defaultId) {
        this.defaultId = defaultId;
        return this;
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
            this.add(RegistryKey.of(this.key, key), value, RegistryEntryInfo.DEFAULT);
        }
        for (ReloadStep<T> step : this.reloadableSteps) {
            step.reload(manager);
        }
    }

    public Stream<Map.Entry<Identifier, T>> streamIdToValue() {
        return this.idToEntry.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().value()
        )).entrySet().stream();
    }

    public Map<Integer, RegistryEntry.Reference<T>> getIdToEntryMap() {
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
    public RegistryEntry.Reference<T> add(RegistryKey<T> key, T value, RegistryEntryInfo info) {
        Identifier id = key.getValue();
        RegistryEntry.Reference<T> entry = createEntry(value);
        entry.setRegistryKey(key);
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

    public RegistryEntry.Reference<T> set(RegistryKey<T> key, T value, RegistryEntryInfo info) {
        if (!this.keyToEntry.containsKey(key)) {
            return this.add(key, value, info);
        }
        Identifier id = key.getValue();
        Optional<RegistryEntry.Reference<T>> oldEntry = this.getEntry(id);
        if (oldEntry.isEmpty()) {
            log.error("Can't find prev value in registry {}", this.key);
            Optional<RegistryEntry.Reference<T>> defaultEntry = this.getDefaultEntry();
            return defaultEntry.orElse(null);
        }

        this.builtins.remove(id);
        int oldRawId = this.rawIdToEntry.inverse().get(oldEntry.get());
        this.rawIdToEntry.remove(oldRawId);

        RegistryEntry.Reference<T> entry = createEntry(value);
        entry.setRegistryKey(key);
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
    public void setEntries(TagKey<T> tag, List<RegistryEntry<T>> registryEntries) {
        RegistryEntryList.Named<T> entryListNamed = NamedAccessor.callNew(this, tag);
        this.tags.put(tag, entryListNamed);
        entryListNamed.entries = new ArrayList<>(registryEntries);
    }

    @Override
    public boolean isEmpty() {
        return this.keyToEntry.isEmpty();
    }

    @Override
    public RegistryEntryLookup<T> createMutableRegistryLookup() {
        return new RegistryEntryLookup<T>() {
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return Optional.of(this.getOrThrow(key));
            }

            public RegistryEntry.Reference<T> getOrThrow(RegistryKey<T> key) {
                return IntrinsicalRegister.this.getOrCreateEntry(key);
            }

            public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                return Optional.of(this.getOrThrow(tag));
            }

            public RegistryEntryList.Named<T> getOrThrow(TagKey<T> tag) {
                return IntrinsicalRegister.this.getTag(tag);
            }
        };
    }

    @Override
    public RegistryKey<? extends Registry<T>> getKey() {
        return this.key;
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    @Override
    public @Nullable Identifier getId(T value) {
        for (Map.Entry<Identifier, RegistryEntry.Reference<T>> mapEntry : this.idToEntry.entrySet()) {
            if (mapEntry.getValue().value().equals(value)) {
                return mapEntry.getKey();
            }
        }
        return null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T value) {
        RegistryEntry.Reference<T> ref = this.valueToEntry.get(value);
        return ref != null && ref.hasKeyAndValue() ? Optional.of(ref.registryKey()) : Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        Optional<RegistryEntry.Reference<T>> entry = this.getEntry(this.getId(value));
        if (entry.isEmpty()) {
            return -1;
        }
        return this.rawIdToEntry.inverse().get(entry.get());
    }

    @Override
    public @Nullable T get(int index) {
        RegistryEntry.Reference<T> tReference = this.rawIdToEntry.get(index);
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
    public @Nullable T get(@Nullable RegistryKey<T> key) {
        RegistryEntry.Reference<T> entry = this.keyToEntry.get(key);
        if (entry == null) {
            RegistryEntry.Reference<T> tReference = this.getDefaultEntry().orElse(null);
            if (tReference == null) {
                return null;
            }
            return tReference.value();
        }
        return entry.value();
    }

    @Override
    public @Nullable T get(@Nullable Identifier id) {
        if (id == null) {
            return this.getDefaultEntry().map(RegistryEntry::value).orElse(null);
        }
        RegistryEntry.Reference<T> ref = this.idToEntry.get(id);
        if (ref != null) {
            return ref.value();
        }
        return this.getDefaultEntry().map(RegistryEntry::value).orElse(null);
    }


    @Override
    public Optional<RegistryEntryInfo> getEntryInfo(RegistryKey<T> key) {
        return Optional.ofNullable(this.keyToEntryInfo.get(key));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getDefaultEntry() {
        return this.getEntry(this.defaultId);
    }

    @Override
    public Set<Identifier> getIds() {
        return Set.copyOf(this.idToEntry.keySet());
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return Collections.unmodifiableSet(Util.transformMapValuesLazy(this.keyToEntry, RegistryEntry::value).entrySet());
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return Set.copyOf(this.keyToEntry.keySet());
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return Util.getRandomOrEmpty(this.idToEntry.values().stream().toList(), random);
    }

    @Override
    public boolean containsId(Identifier id) {
        return this.idToEntry.containsKey(id);
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
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
    public RegistryEntry.Reference<T> createEntry(T value) {
        return RegistryEntry.Reference.intrusive(this, value);
    }

    RegistryEntry.Reference<T> getOrCreateEntry(RegistryKey<T> key) {
        return this.keyToEntry.computeIfAbsent(key, (key2) -> RegistryEntry.Reference.standAlone(this, key2));
    }

    RegistryEntryList.Named<T> getTag(TagKey<T> key) {
        return this.tags.computeIfAbsent(key, this::createNamedEntryList);
    }

    private RegistryEntryList.Named<T> createNamedEntryList(TagKey<T> tag) {
        return NamedAccessor.callNew(this, tag);
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return Optional.ofNullable(this.rawIdToEntry.get(rawId));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(Identifier id) {
        return Optional.ofNullable(this.idToEntry.get(id));
    }

    @Override
    public RegistryEntry<T> getEntry(T value) {
        RegistryEntry.Reference<T> reference = this.valueToEntry.get(value);
        return (reference != null ? reference : RegistryEntry.of(value));
    }

    @Override
    public Stream<RegistryEntryList.Named<T>> streamTags() {
        return Stream.empty();
    }

    @Override
    public PendingTagLoad<T> startTagReload(TagGroupLoader.RegistryTags<T> tags) {
        return null;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.keyToEntry.values().stream().map(RegistryEntry::value).iterator();
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return this.keyToEntry.values().stream();
    }

    @Override
    public Stream<RegistryEntryList.Named<T>> getTags() {
        return this.tags.values().stream();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
        return this.getEntry(key.getValue());
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
        return Optional.ofNullable(this.tags.get(tag));
    }
}
