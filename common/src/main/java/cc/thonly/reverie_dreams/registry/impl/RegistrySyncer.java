package cc.thonly.reverie_dreams.registry.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SuppressWarnings("unchecked")
public abstract class RegistrySyncer<T, D> {
    private final RegistryImpl<T> registry;
    private final Codec<D> dataCodec;
    @Getter
    private final ClientReloadListener<T, D> clientReloadListener;

    public RegistrySyncer(RegistryImpl<T> registry, Codec<D> dataCodec, ClientReloadListener<T, D> clientReloadListener) {
        this.registry = registry;
        this.dataCodec = dataCodec;
        this.clientReloadListener = clientReloadListener;
    }

    public abstract T toT(D d);

    public abstract D toD(T t);

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
        void preProcessing(RegistryImpl<T> registry);

        void afterProcessing(RegistryImpl<T> registry);

        T update(Identifier key, @Nullable T old, D data);

        RegistrySyncer<T, D> getSyncer();
    }
}
