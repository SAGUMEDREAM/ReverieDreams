package cc.thonly.reverie_dreams.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RegistryTagSet {
    public static final Codec<RegistryTagSet> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("values", new ArrayList<>()).forGetter(RegistryTagSet::contents)
    ).apply(instance, RegistryTagSet::new)));
    private final List<String> values = new ArrayList<>();

    public RegistryTagSet() {
    }

    public RegistryTagSet(List<String> values) {
        this();
        this.values.addAll(values);
    }

    public List<String> contents() {
        return this.values;
    }

    public <T> Result<T> result(Registry<T> registry) {
        List<TagKey<T>> keys = new ArrayList<>();
        List<ResourceKey<T>> ids = new ArrayList<>();
        for (String value : this.values) {
            if (value.startsWith("#")) {
                String tagValue = value.substring(1);

                Identifier tagId = Identifier.tryParse(tagValue);

                if (tagId == null) {
                    log.error("Error: {} is not a valid tag identifier", value);
                    continue;
                }

                keys.add(TagKey.create(
                        registry.key(),
                        tagId
                ));

                continue;
            }
            Identifier entryId = Identifier.tryParse(value);
            if (entryId == null) {
                log.error("Error: {} is not a identifier", value);
                continue;
            }
            ids.add(ResourceKey.create(registry.key(), entryId));
        }
        return new Result<>(keys, ids);
    }

    public record Result<T>(
            List<TagKey<T>> keys,
            List<ResourceKey<T>> ids
    ) {
        public RegistryTagSet builder() {
            List<String> values = new ArrayList<>(
                    this.ids.size() + this.keys.size()
            );

            for (ResourceKey<T> id : this.ids) {
                values.add(id.identifier().toString());
            }

            for (TagKey<T> key : this.keys) {
                values.add("#" + key.location().toString());
            }

            return new RegistryTagSet(values);
        }
    }
}
