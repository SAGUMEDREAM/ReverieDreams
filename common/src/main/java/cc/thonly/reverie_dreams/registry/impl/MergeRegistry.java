package cc.thonly.reverie_dreams.registry.impl;

import com.mojang.serialization.Lifecycle;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public abstract class MergeRegistry<T> implements WritableRegistry<T> {

    private final ResourceKey<? extends Registry<T>> key;
    private final List<Registry<T>> list = new ArrayList<>();

    public MergeRegistry(ResourceKey<? extends Registry<T>> key) {
        this.key = key;
    }

    public MergeRegistry(ResourceKey<? extends Registry<T>> key, Registry... registries) {
        this.key = key;
        this.list.addAll((Collection) Arrays.stream(registries).toList());
    }

    public MergeRegistry(ResourceKey<? extends Registry<T>> key, List<Registry> registries) {
        this.key = key;
        this.list.addAll((Collection) registries);
    }

    @Override
    public Holder.@Nullable Reference<T> register(ResourceKey<T> key, T value, RegistrationInfo registrationInfo) {
        log.error("MergeRegistry is read-only!");
        return null;
    }

    public void bindTags(Map<TagKey<T>, List<Holder<T>>> pendingTags) {
        log.warn("MergeRegistry does not support bindTags");
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T value) {
        log.error("MergeRegistry cannot create intrusive holder");
        return null;
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    @Override
    public Lifecycle registryLifecycle() {
        return Lifecycle.stable();
    }

    @Override
    public boolean isEmpty() {
        for (Registry<T> registry : this.list) {
            if (registry.size() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return entrySet().size();
    }

    @Override
    public @Nullable T getValue(@Nullable ResourceKey<T> key) {
        if (key == null) return null;
        for (Registry<T> registry : this.list) {
            T val = registry.getValue(key);
            if (val != null) return val;
        }
        return null;
    }

    @Override
    public @Nullable T getValue(@Nullable Identifier key) {
        if (key == null) return null;
        for (Registry<T> registry : this.list) {
            T val = registry.getValue(key);
            if (val != null) return val;
        }
        return null;
    }

    @Override
    public boolean containsKey(ResourceKey<T> key) {
        return list.stream().anyMatch(r -> r.containsKey(key));
    }

    @Override
    public boolean containsKey(Identifier key) {
        return list.stream().anyMatch(r -> r.containsKey(key));
    }

    @Override
    public @Nullable Identifier getKey(T thing) {
        for (Registry<T> registry : list) {
            Identifier id = registry.getKey(thing);
            if (id != null) return id;
        }
        return null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T thing) {
        for (Registry<T> registry : list) {
            Optional<ResourceKey<T>> key = registry.getResourceKey(thing);
            if (key.isPresent()) return key;
        }
        return Optional.empty();
    }

    @Override
    public int getId(@Nullable T thing) {
        for (Registry<T> registry : list) {
            int id = registry.getId(thing);
            if (id != -1) return id;
        }
        return -1;
    }

    @Override
    public @Nullable T byId(int id) {
        for (Registry<T> registry : list) {
            T val = registry.byId(id);
            if (val != null) return val;
        }
        return null;
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
        for (Registry<T> registry : list) {
            Optional<Holder.Reference<T>> holder = registry.get(id);
            if (holder.isPresent()) return holder;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Holder.Reference<T>> get(Identifier id) {
        for (Registry<T> registry : list) {
            Optional<Holder.Reference<T>> holder = registry.get(id);
            if (holder.isPresent()) return holder;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Holder.Reference<T>> get(int id) {
        for (Registry<T> registry : list) {
            Optional<Holder.Reference<T>> holder = registry.get(id);
            if (holder.isPresent()) return holder;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        for (Registry<T> registry : list) {
            Optional<Holder.Reference<T>> any = registry.getAny();
            if (any.isPresent()) return any;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource random) {
        List<Holder.Reference<T>> all = listElements().toList();
        if (all.isEmpty()) return Optional.empty();
        return Optional.of(all.get(random.nextInt(all.size())));
    }

    @Override
    public Holder<T> wrapAsHolder(T value) {
        for (Registry<T> registry : list) {
            try {
                return registry.wrapAsHolder(value);
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
        for (Registry<T> registry : this.list) {
            Optional<HolderSet.Named<T>> tag = registry.get(id);
            if (tag.isPresent()) return tag;
        }
        return Optional.empty();
    }

    @Override
    public Stream<HolderSet.Named<T>> getTags() {
        return this.list.stream().flatMap(Registry::getTags).distinct();
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return this.getTags();
    }

    @Override
    public Set<Identifier> keySet() {
        return this.list.stream()
                   .flatMap(r -> r.keySet().stream())
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return this.list.stream()
                   .flatMap(r -> r.registryKeySet().stream())
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        Map<ResourceKey<T>, T> map = new LinkedHashMap<>();
        for (Registry<T> registry : this.list) {
            for (Map.Entry<ResourceKey<T>, T> e : registry.entrySet()) {
                map.putIfAbsent(e.getKey(), e.getValue());
            }
        }
        return map.entrySet();
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return this.list.stream()
                   .flatMap(HolderLookup::listElements)
                   .distinct();
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return entrySet().stream().map(Map.Entry::getValue).iterator();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> element) {
        for (Registry<T> registry : this.list) {
            Optional<RegistrationInfo> info = registry.registrationInfo(element);
            if (info.isPresent()) return info;
        }
        return Optional.of(RegistrationInfo.BUILT_IN);
    }

    @Override
    public Registry<T> freeze() {
        return this;
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        return new HolderGetter<>() {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
                return MergeRegistry.this.get(id);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
                return MergeRegistry.this.get(id);
            }
        };
    }

    @Override
    public PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> tags) {
        return new PendingTags<T>() {
            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return MergeRegistry.this.key;
            }

            @Override
            public RegistryLookup<T> lookup() {
                return MergeRegistry.this;
            }

            @Override
            public void apply() {
                log.warn("MergeRegistry does not support tag reload");
            }

            @Override
            public int size() {
                return tags.tags().size();
            }

            public Map<TagKey<T>, List<Holder<T>>> contents() {
                return Map.of();
            }
        };
    }
}