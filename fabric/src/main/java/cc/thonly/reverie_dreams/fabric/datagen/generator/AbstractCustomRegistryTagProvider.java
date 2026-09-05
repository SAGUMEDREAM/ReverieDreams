package cc.thonly.reverie_dreams.fabric.datagen.generator;

import cc.thonly.reverie_dreams.fabric.util.DataGeneratorUtil;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import cc.thonly.reverie_dreams.registry.RegistryTagSet;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.common.hash.HashCode;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("unchecked")
public abstract class AbstractCustomRegistryTagProvider<E, T> implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final RegistryProvider<E> registryProvider;
    private final Map<TagKey<E>, TagAppender<E, T>> registries = new Object2ObjectOpenHashMap<>();

    public AbstractCustomRegistryTagProvider(RegistryProvider<E> registryProvider, FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.registryProvider = registryProvider;
        this.output = output;
        this.future = future;
    }

    public abstract void addTags(HolderLookup.Provider provider);

    protected TagAppender<E, T> valueLookupBuilder(TagKey<E> tag) {
        return this.getOrCreateRawBuilder(tag);
    }

    private TagAppender<E, T> getOrCreateRawBuilder(TagKey<E> tag) {
        List<TagKey<E>> keys = new ArrayList<>();
        List<ResourceKey<E>> ids = new ArrayList<>();
        RegistryTagSet.Result<E> result = new RegistryTagSet.Result<>(keys, ids);
        return this.registries.computeIfAbsent(tag, inst -> new TagAppender<>(this.registryProvider, tag, result));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.future.thenAcceptAsync(provider -> {
            this.addTags(provider);
            this.outputFile(cache);
        });
    }

    private void outputFile(CachedOutput writer) {
        Path outputDir = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        for (Map.Entry<TagKey<E>, TagAppender<E, T>> entry : this.registries.entrySet()) {
            try {
                TagAppender<E, T> tagAppender = entry.getValue();
                TagKey<E> tag = tagAppender.tag;
                Identifier location = tag.location();
                RegistryTagSet.Result<E> entries = tagAppender.entries;
                RegistryTagSet builder = entries.builder();
                DataResult<JsonElement> result = RegistryTagSet.CODEC.encodeStart(JsonOps.INSTANCE, builder);
                Optional<JsonElement> optional = result.result();
                Path path = DataGeneratorUtil.getData(outputDir, location.getNamespace(), "tags/" + this.registryProvider.key().identifier().getPath(), null);
                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path filePath = path.resolve(location.getPath() + ".json");
                    String jsonString = DataProviderHelper.gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    Files.createDirectories(filePath.getParent());

                    writer.writeIfNeeded(filePath, bytes, HashCode.fromBytes(bytes));
                }
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
    }

    @Override
    public String getName() {
        return "Custom Registry Tag Provider";
    }

    @SuppressWarnings("unused")
    public static class TagAppender<E, T> {
        private final RegistryProvider<E> registryProvider;
        private final TagKey<E> tag;
        private final RegistryTagSet.Result<E> entries;

        public TagAppender(RegistryProvider<E> registryProvider, TagKey<E> tag, RegistryTagSet.Result<E> entries) {
            this.registryProvider = registryProvider;
            this.tag = tag;
            this.entries = entries;
        }

        public TagAppender<E, T> add(E element) {
            this.registryProvider.getResourceKey(element).ifPresent(resourceKey -> this.entries.ids().add(resourceKey));
            return this;
        }

        public TagAppender<E, T> add(final E... elements) {
            return this.addAll(Arrays.stream(elements));
        }

        public TagAppender<E, T> addAll(final Collection<E> elements) {
            elements.forEach(this::add);
            return this;
        }

        public TagAppender<E, T> addAll(final Stream<E> elements) {
            elements.forEach(this::add);
            return this;
        }

        public TagAppender<E, T> addOptional(E element) {
            this.add(element);
            return this;
        }

        public TagAppender<E, T> addTag(TagKey<T> tag) {
            this.entries.keys().add((TagKey<E>) tag);
            return this;
        }

        public TagAppender<E, T> addOptionalTag(TagKey<T> tag) {
            this.entries.keys().add((TagKey<E>) tag);
            return this;
        }
    }
}
