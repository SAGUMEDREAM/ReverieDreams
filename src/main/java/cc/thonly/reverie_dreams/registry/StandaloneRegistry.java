package cc.thonly.reverie_dreams.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("UnusedReturnValue")
@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public final class StandaloneRegistry<T extends RegistrableObject<T>> implements Serializable {
    @Serial
    private static final long serialVersionUID = 199189765401L;
    private final Identifier key;
    private final Map<Integer, T> rawToEntry;
    private final Map<Identifier, T> idToEntry;
    private final Map<Identifier, Reference<T>> idToReference;
    private final Map<Identifier, T> finalIdToEntry = new Object2ObjectOpenHashMap<>();
    private Map<Integer, T> baseRawToEntry = new Object2ObjectLinkedOpenHashMap<>();
    private Map<Identifier, T> baseIdToEntry = new Object2ObjectLinkedOpenHashMap<>();
    private Supplier<T> defaultEntryGetter;
    private T defaultEntry;
    private final List<BootstrapBuilder<T>> builders = new LinkedList<>();
    private final List<ReloadableBootstrap<T>> reloadableBuilders = new LinkedList<>();
    private boolean isFrozen = false;
    private boolean reloadable = false;
    private boolean sync = false;
    private Codec<StandaloneRegistry<T>> entriesCodec;
    private Codec<T> entryCodec;

    public StandaloneRegistry(Identifier key) {
        if (key == null) {
            throw new IllegalArgumentException("Registry require a key, but it is null");
        }
        this.key = key;
        this.rawToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.idToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.idToReference = new Object2ObjectLinkedOpenHashMap<>();
    }

    public StandaloneRegistry(Identifier key, Map<Identifier, T> idToEntry) {
        if (key == null) {
            throw new IllegalArgumentException("Registry require a key, but it is null");
        }
        if (idToEntry == null) {
            throw new IllegalArgumentException("Registry require an idToEntry, but it is null");
        }
        this.key = key;
        this.rawToEntry = new Object2ObjectLinkedOpenHashMap<>();
        this.idToEntry = idToEntry;
        this.idToReference = new Object2ObjectLinkedOpenHashMap<>();
    }

    public static <T extends RegistrableObject<T>> Codec<StandaloneRegistry<T>> createCodec(
            Identifier key, Codec<T> tCodec
    ) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.INT, tCodec).fieldOf("rawToEntry").forGetter(schema -> schema.rawToEntry),
                Codec.unboundedMap(Identifier.CODEC, tCodec).fieldOf("idToEntry").forGetter(schema -> schema.idToEntry)
        ).apply(instance, (rawToEntry, idToEntry) -> {
            StandaloneRegistry<T> registry = new StandaloneRegistry<>(key);
            registry.rawToEntry.putAll(rawToEntry);
            registry.idToEntry.putAll(idToEntry);
            for (var entry : idToEntry.entrySet()) {
                entry.getValue().setId(entry.getKey());
            }
            return registry;
        }));
    }

    public void verify() {
        this.stream().forEach(x -> {
            Identifier key = x.getKey();
            T value = x.getValue();
            if (key == null) {
                log.error("Can't verify key {} in registry {}", x, this.key);
            }
            if (value == null) {
                log.error("Can't verify value {} -> {} in registry {}", key, null, this.key);
            }
            if (key != null && value != null) {
                log.debug("Verified key value {} -> {} in registry {}", key, value, this.key);
            }
        });
    }

    public StandaloneRegistry<T> codec(Codec<T> tCodec) {
        this.entryCodec = tCodec;
        this.entriesCodec = createCodec(this.key, tCodec);
        return this;
    }

    public T add(Identifier key, T value) {
        if (this.idToEntry.containsKey(key)) {
            return null;
        }

        this.rawToEntry.put(this.rawToEntry.size(), value);
        this.idToEntry.put(key, value);
        value.setId(key);
        if (!value.isDirect()) {
            this.finalIdToEntry.put(key, value);
        }
        return value;
    }

    public Map<Identifier, T> add(Map<Identifier, T> idToEntry) {
        for (var entry : idToEntry.entrySet()) {
            this.add(entry.getKey(), entry.getValue());
            entry.getValue().setId(entry.getKey());
        }
        return idToEntry;
    }

    public T add(Integer rawId, Identifier key, T value) {
        if (this.rawToEntry.containsKey(rawId) || this.idToEntry.containsKey(key)) {
            return null;
        }

        this.rawToEntry.put(rawId, value);
        this.idToEntry.put(key, value);
        value.setId(key);
        if (!value.isDirect()) {
            this.finalIdToEntry.put(key, value);
        }
        return value;
    }

    public T set(Integer rawId, Identifier key, T value) {
        if (!this.rawToEntry.containsKey(rawId) || !this.idToEntry.containsKey(key)) {
            return null;
        }

        this.rawToEntry.put(rawId, value);
        this.idToEntry.put(key, value);
        value.setId(key);
        if (!value.isDirect()) {
            this.finalIdToEntry.put(key, value);
        }
        return value;
    }

    public T set(Identifier key, T value) {
        if (!this.idToEntry.containsKey(key)) {
            return null;
        }

        this.idToEntry.put(key, value);

        Integer rawId = this.getRawIdByKey(key);
        if (rawId != null) {
            this.rawToEntry.put(rawId, value);
        }
        value.setId(key);
        if (!value.isDirect()) {
            this.finalIdToEntry.put(key, value);
        }
        return value;
    }

    public T get(Integer rawId) {
        return this.rawToEntry.get(rawId);
    }

    public T get(Identifier key) {
        return this.idToEntry.get(key);
    }

    public Integer getRawId(T value) {
        for (Map.Entry<Integer, T> entry : this.rawToEntry.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public Identifier getId(T value) {
        for (Map.Entry<Identifier, T> entry : this.idToEntry.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public T getOrDefault(Integer rawId) {
        return this.rawToEntry.getOrDefault(rawId, this.defaultEntry != null ? this.defaultEntry : this.defaultEntryGetter.get());
    }

    public T getOrDefault(Identifier key) {
        return this.idToEntry.getOrDefault(key, this.defaultEntry != null ? this.defaultEntry : this.defaultEntryGetter.get());
    }

    public T getOrDefault(Identifier key, T defaultEntry) {
        return this.idToEntry.getOrDefault(key, defaultEntry);
    }

    public Optional<T> getOptional(Integer rawId) {
        return Optional.ofNullable(this.get(rawId));
    }

    public Optional<T> getOptional(Identifier key) {
        return Optional.ofNullable(this.get(key));
    }

    public T getOrThrow(Integer rawId) {
        return Optional.ofNullable(this.get(rawId))
                .orElseThrow(() -> new NoSuchElementException("No found for raw ID to value: " + rawId));
    }

    public T getOrThrow(Identifier key) {
        return Optional.ofNullable(this.get(key))
                .orElseThrow(() -> new NoSuchElementException("No found for id to value: " + key));
    }

    public Set<Map.Entry<Integer, T>> rawEntrySet() {
        return this.rawToEntry.entrySet();
    }

    public Set<Map.Entry<Identifier, T>> entrySet() {
        return this.idToEntry.entrySet();
    }

    public Set<Integer> rawIds() {
        return this.rawToEntry.keySet();
    }

    public Stream<Map.Entry<Identifier, T>> stream() {
        return this.idToEntry.entrySet().stream();
    }

    public Set<Identifier> keys() {
        return this.idToEntry.keySet();
    }

    public Collection<T> rawValues() {
        return this.rawToEntry.values();
    }

    public Collection<T> values() {
        return this.idToEntry.values();
    }

    public boolean fromDatapack(Identifier key) {
        return this.idToEntry.containsKey(key);
    }

    public boolean fromDatapack(T value) {
        return this.idToEntry.containsValue(value);
    }

    public boolean fromDatapack(Integer rawId) {
        return this.rawToEntry.containsKey(rawId);
    }

    public void reset() {
        this.idToEntry.clear();
        this.rawToEntry.clear();
        if (this.isFrozen()) {
            this.idToEntry.putAll(this.baseIdToEntry);
            this.rawToEntry.putAll(this.baseRawToEntry);
            this.finalIdToEntry.forEach(this::add);
        }
    }

    public StandaloneRegistry<T> buildReference() {
        this.idToEntry.forEach((id, value) -> {
            Reference<T> tReference = this.createEntryReference(value);
            this.idToReference.put(id, tReference);
        });
        return this;
    }

    public Reference<T> createEntryReference(T value) {
        return Reference.of(value.getId(), value);
    }

    public StandaloneRegistry<T> defaultEntry(T defaultEntry) {
        this.defaultEntry = defaultEntry;
        return this;
    }

    public StandaloneRegistry<T> defaultEntry(Supplier<T> getter) {
        this.defaultEntryGetter = getter;
        return this;
    }

    public StandaloneRegistry<T> freeze() {
        this.isFrozen = true;
        return this;
    }

    public StandaloneRegistry<T> unfreeze() {
        this.isFrozen = false;
        return this;
    }

    public JsonElement encode() {
        JsonObject element = new JsonObject();
        Object2ObjectOpenHashMap<Identifier, T> registries = new Object2ObjectOpenHashMap<>(this.idToEntry);
        Set<Map.Entry<Identifier, T>> entries = registries.entrySet();
        Codec<T> codec = this.entryCodec;
        if (codec == null) {
            return element;
        }
        for (Map.Entry<Identifier, T> entry : entries) {
            T value = entry.getValue();
            DataResult<JsonElement> dataResult = codec.encodeStart(JsonOps.INSTANCE, value);
            Optional<JsonElement> result = dataResult.result();
            result.ifPresent((e) -> {
                element.add(value.getId().toString(), e);
            });
        }
        return element;
    }

    public List<T> decode(JsonElement element) {
        List<T> list = new LinkedList<>();
        Codec<T> codec = this.entryCodec;

        if (codec == null) {
            return list;
        }

        if (!(element instanceof JsonObject jsonObject)) {
            return list;
        }

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            Identifier id;
            try {
                id = Identifier.of(key);
            } catch (Exception e) {
                log.error("Can't parse Identifier {}", key, e);
                continue;
            }

            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, value);
            DataResult<T> parseResult = codec.parse(dynamic);

            parseResult.resultOrPartial(error -> {
                log.error("Can't parse {} -> {}", key, error);
            }).ifPresent(r -> {
                r.setId(id);
                list.add(r);
            });
        }

        return list;
    }

    public StandaloneRegistry<T> sync() {
        this.sync = true;
        return this;
    }

    public StandaloneRegistry<T> build() {
        this.builders.add(registry -> {
        });
        this.defaultEntry = this.defaultEntryGetter.get();
        return this;
    }

    public StandaloneRegistry<T> reloadable() {
        this.reloadable = true;
        return this;
    }

    @SafeVarargs
    public final StandaloneRegistry<T> reloadable(ReloadableBootstrap<T>... builders) {
        this.reloadable = true;
        this.reloadableBuilders.addAll(Arrays.asList(builders));
        return this;
    }

    @SafeVarargs
    public final StandaloneRegistry<T> build(BootstrapBuilder<T>... builders) {
        this.builders.addAll(Arrays.asList(builders));
        return this;
    }

    public StandaloneRegistry<T> apply() {
        if (!this.isFrozen) {
            this.builders.forEach(builder -> builder.bootstrap(this));
            this.baseRawToEntry = new Object2ObjectLinkedOpenHashMap<>(this.rawToEntry);
            this.baseIdToEntry = new Object2ObjectLinkedOpenHashMap<>(this.idToEntry);
            this.isFrozen = true;
        }
        return this;
    }

    private Integer getRawIdByKey(Identifier key) {
        for (Map.Entry<Integer, T> entry : this.rawToEntry.entrySet()) {
            if (this.idToEntry.get(key).equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Getter
    public static class Reference<T> {
        private final Identifier key;
        private final T value;

        protected Reference(Identifier key, T value) {
            this.key = key;
            this.value = value;
        }

        public static <T> Reference<T> of(Identifier key, T value) {
            return new Reference<>(key, value);
        }
    }

    @FunctionalInterface
    public interface BootstrapBuilder<T extends RegistrableObject<T>> {
        void bootstrap(StandaloneRegistry<T> registry);
    }

    @FunctionalInterface
    public interface ReloadableBootstrap<T extends RegistrableObject<T>> {
        void reload(ResourceManager manager);
    }
}