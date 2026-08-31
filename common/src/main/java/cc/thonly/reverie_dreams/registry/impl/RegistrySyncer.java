package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.registry.RegistryTagSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@SuppressWarnings({"unchecked", "ForLoopReplaceableByForEach"})
public abstract class RegistrySyncer<T, D> {
    private final RegistryProvider<T> registry;
    private final Codec<D> dataCodec;
    @Getter
    private final ClientReloadListener<T, D> clientReloadListener;

    public RegistrySyncer(RegistryProvider<T> registry, Codec<D> dataCodec, ClientReloadListener<T, D> clientReloadListener) {
        this.registry = registry;
        this.dataCodec = dataCodec;
        this.clientReloadListener = clientReloadListener;
    }

    public abstract T toT(D d);

    public abstract D toD(T t);

    public static <T> CompoundTag writeNamedToTag(Collection<HolderSet.Named<T>> namedTags) {
        CompoundTag tag = new CompoundTag();
        int size = namedTags.size();
        tag.putInt("size", size);
        ListTag entries = new ListTag();
        for (HolderSet.Named<T> namedTag : namedTags) {
            if (!namedTag.isBound()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("id", namedTag.key().location().toString());
            ListTag values = new ListTag();
            for (Holder<T> tHolder : namedTag) {
                tHolder.unwrapKey().ifPresent(key -> {
                    CompoundTag entryKey = new CompoundTag();
                    entryKey.putString("name", key.identifier().toString());
                    values.add(entryKey);
                });
            }
            entry.put("values", values);
            entries.add(entry);
        }
        tag.put("tags", entries);
        return tag;
    }

    public static List<Tuple<Identifier, RegistryTagSet>> readTagToSet(CompoundTag tag) {
        int size = tag.getIntOr("size", -1);
        if (size < 0) {
            log.error("Error: Tag length abnormal");
            return List.of();
        }

        List<Tuple<Identifier, RegistryTagSet>> list = new ArrayList<>();
        ListTag tags = tag.getListOrEmpty("tags");

        for (Tag entry : tags) {
            if (!(entry instanceof CompoundTag compoundTag)) {
                continue;
            }

            String idString = compoundTag.getStringOr("id", "");
            if (idString.isEmpty()) {
                log.error("Error: Missing tag id");
                continue;
            }

            Identifier id = Identifier.tryParse(idString);
            if (id == null) {
                log.error("Error: Invalid tag identifier: {}", idString);
                continue;
            }

            Optional<RegistryTagSet> optional = RegistryTagSet.CODEC.parse(
                    NbtOps.INSTANCE,
                    compoundTag
            ).resultOrPartial(error ->
                    log.error("Failed to parse tag {}: {}", id, error)
            );

            optional.ifPresent(registryTagSet ->
                    list.add(new Tuple<>(id, registryTagSet))
            );
        }

        if (list.size() != size) {
            log.warn(
                    "Tag length mismatch: expected {}, actually read {}",
                    size,
                    list.size()
            );
        }

        return list;
    }

    public CompoundTag writeToTag(List<Entry<T>> entries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("registry_key", this.getRegistryKey().identifier().toString());
        List<Entry<D>> list = entries.stream().map(entry -> new Entry<>(entry.key(), this.toD(entry.value))).toList();
        ListTag listTag = new ListTag();
        for (Entry<D> entry : list) {
            listTag.add(this.encodeEntry(entry));
        }
        tag.put("values", listTag);
        return tag;
    }

    public List<Entry<D>> readToEntries(CompoundTag tag) {
        List<Entry<D>> result = new ArrayList<>();

        if (!tag.contains("values")) {
            return result;
        }

        ListTag listTag = tag.getListOrEmpty("values");

        for (int i = 0; i < listTag.size(); i++) {
            Tag raw = listTag.get(i);

            if (!(raw instanceof CompoundTag entryTag)) {
                continue;
            }

            Entry<D> entry = this.decodeEntry(entryTag);
            if (entry != null) {
                result.add(entry);
            }
        }

        return result;
    }

    public CompoundTag encodeEntry(Entry<D> entry) {
        CompoundTag tag = new CompoundTag();
        Identifier key = entry.key();
        D data = entry.value();
        tag.putString("id", key.toString());
        tag.put("value", this.encode(data));
        return tag;
    }

    public Entry<D> decodeEntry(CompoundTag tag) {
        Identifier id = Identifier.parse(tag.getStringOr("id", "null"));

        Tag valueTag = tag.get("value");
        if (valueTag == null) {
            return null;
        }

        D value = this.decode(valueTag);
        if (value == null) {
            return null;
        }

        return new Entry<>(id, value);
    }

    public Tag encode(D data) {
        DataResult<Tag> result = this.dataCodec.encodeStart(NbtOps.INSTANCE, data);
        return result.resultOrPartial(error -> {
            log.error("Failed to encode registry data: {}", error);
        }).orElse(new CompoundTag());
    }

    public D decode(Tag tag) {
        DataResult<D> result = this.dataCodec.parse(NbtOps.INSTANCE, tag);
        return result.resultOrPartial(error -> {
            log.error("Failed to decode recipe: {}", error);
        }).orElse(null);
    }

    public ResourceKey<Registry<T>> getRegistryKey() {
        return (ResourceKey<Registry<T>>) this.registry.getRegistryKey();
    }

    public record Entry<Any>(Identifier key, Any value) {
        public static <Any> Entry<Any> of(Identifier key, Any value) {
            return new Entry<>(key, value);
        }
    }

    public interface ClientReloadListener<T, D> {
        void preProcessing(RegistryProvider<T> registry);

        void afterProcessing(RegistryProvider<T> registry);

        T update(Identifier key, @Nullable T old, D data);

        RegistrySyncer<T, D> getSyncer();
    }
}
