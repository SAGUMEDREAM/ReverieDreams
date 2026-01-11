package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.mixin.accessor.HolderReferenceAccessors;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;

import java.util.*;

@SuppressWarnings("unchecked")
public class ServerContentRegistry {
    public static final ServerContentRegistry IMPL = new ServerContentRegistry();
    private final Map<ResourceKey<Registry<Object>>, List<Tuple<ResourceKey<Object>, Object>>> contents = new Object2ObjectOpenHashMap<>(256);

    public static ServerContentRegistry getInstance() {
        return IMPL;
    }

    public <T> T register(ResourceKey<Registry<T>> registry, ResourceKey<T> resourceKey, T value) {
        List<Tuple<ResourceKey<Object>, Object>> objects = this.contents.computeIfAbsent((ResourceKey<Registry<Object>>) (Object) registry, r -> new ArrayList<>());
        Tuple<ResourceKey<Object>, Object> entry = new Tuple<>((ResourceKey<Object>) resourceKey, value);
        objects.add(entry);
        return value;
    }

    public <T> T register(ResourceKey<Registry<T>> registry, Identifier location, T value) {
        return register(registry, ResourceKey.create(registry, location), value);
    }

    public <T> T delete(ResourceKey<Registry<T>> registry, ResourceKey<T> resourceKey) {
        List<Tuple<ResourceKey<Object>, Object>> entries = this.contents.get(registry);
        if (entries == null) {
            return null;
        }
        Iterator<Tuple<ResourceKey<Object>, Object>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Tuple<ResourceKey<Object>, Object> next = iterator.next();
            if (Objects.equals(next.getA(), resourceKey)) {
                iterator.remove();
                return (T) next.getB();
            }
        }
        return null;
    }

    public <T> Optional<T> getValue(ResourceKey<Registry<T>> registry, ResourceKey<T> resourceKey) {
        List<Tuple<ResourceKey<Object>, Object>> entries = this.contents.get(registry);
        if (entries == null) {
            return Optional.empty();
        }
        for (Tuple<ResourceKey<Object>, Object> entry : entries) {
            ResourceKey<Object> entryKey = entry.getA();
            if (Objects.equals(resourceKey, entryKey)) {
                return Optional.of((T) entry.getB());
            }
        }
        return Optional.empty();
    }

    public <T> T get(ResourceKey<Registry<T>> registry, ResourceKey<T> resourceKey) {
        return getValue(registry, resourceKey).orElse(null);
    }

    public <T> void write(WritableRegistry<T> writableRegistry) {
        this.contents.forEach((registryKey, list) -> {
            if (!Objects.equals(registryKey, writableRegistry.key())) {
                return;
            }
            for (var tuple : list) {
                ResourceKey<Object> a = tuple.getA();
                Object b = tuple.getB();
                if (!a.registryKey().equals(writableRegistry.key())) {
                    continue;
                }
                if (writableRegistry.containsKey(a.identifier())) {
                    continue;
                }
                writableRegistry.register((ResourceKey<T>) a, (T) b, RegistrationInfo.BUILT_IN);
            }
        });
    }

    public <T> Holder.Reference<T> bindReferenceHolder(WritableRegistry<T> writable, ResourceKey<T> resourceKey, T value) {
        boolean b = writable.containsKey(resourceKey);
        if (!b) {
            return null;
        }
        Holder.Reference<T> reference = Holder.Reference.createStandAlone(writable, resourceKey);
        HolderReferenceAccessors<T> referenceAccessors = (HolderReferenceAccessors<T>) reference;
        referenceAccessors.reverie_dreams$setValue(value);
        return reference;
    }

    public <T> Collection<Tuple<ResourceKey<T>, T>> entries(ResourceKey<Registry<T>> registry) {
        List<Tuple<ResourceKey<Object>, Object>> list =
                contents.get((ResourceKey<Registry<Object>>) (Object) registry);

        if (list == null) {
            return List.of();
        }

        List<Tuple<ResourceKey<T>, T>> result = new ArrayList<>(list.size());
        for (Tuple<ResourceKey<Object>, Object> entry : list) {
            result.add(new Tuple<>(
                    (ResourceKey<T>) entry.getA(),
                    (T) entry.getB()
            ));
        }
        return result;
    }


    public <T> Collection<Tuple<ResourceKey<Registry<T>>, Tuple<ResourceKey<T>, T>>> entries() {
        List<Tuple<ResourceKey<Registry<T>>, Tuple<ResourceKey<T>, T>>> result = new ArrayList<>();

        for (Map.Entry<ResourceKey<Registry<Object>>, List<Tuple<ResourceKey<Object>, Object>>> mapEntry : contents.entrySet()) {
            ResourceKey<Registry<T>> registryKey =
                    (ResourceKey<Registry<T>>) (ResourceKey<?>) mapEntry.getKey();

            for (Tuple<ResourceKey<Object>, Object> inner : mapEntry.getValue()) {

                Tuple<ResourceKey<T>, T> typedEntry = new Tuple<>(
                        (ResourceKey<T>) inner.getA(),
                        (T) inner.getB()
                );

                result.add(new Tuple<>(registryKey, typedEntry));
            }
        }

        return result;
    }
}
