package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.api.registry.callback.RegistryProviderReloadCallback;
import cc.thonly.reverie_dreams.api.registry.callback.RegistryProviderTagReloadCallback;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.server.CookingInputRecipeManager;
import cc.thonly.reverie_dreams.server.ItemCateManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.server.packs.resources.BalmResourceReloadListenerRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public class ResourceReloadManager {
    public static void initialize() {
        Balm.getRuntime().resourceReloadListeners("reverie_dreams", new Consumer<BalmResourceReloadListenerRegistrar>() {
            @Override
            public void accept(BalmResourceReloadListenerRegistrar registrar) {
                registrar.register("data_reload", ResourceReloadManager::reload);
            }
        });
    }

    public static CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier barrier, Executor applyExecutor) {
        ResourceManager manager = sharedState.resourceManager();

        return CompletableFuture.completedFuture(manager).thenCompose(barrier::wait).thenAccept((rm) -> {
            RecipeManager.onReload(rm);
            for (var entry : BuiltInRegistryProviders.ROOT.entrySet()) {
                RegistryProvider<?> registry = entry.getValue();

                if (registry.isReloadable()) {
                    registry.reload(rm);
                    RegistryProviderReloadCallback.EVENT.invoker().onLoad(registry);
                }
                registry.unboundTag();
                reloadTag(registry, rm);
                Stream<? extends HolderSet.Named<?>> namedStream = registry.listTags();
                namedStream.forEach((Consumer<HolderSet.Named>) holders -> {
                    RegistryProviderTagReloadCallback.EVENT.invoker().onLoad((RegistryProvider) registry, holders);
                });
                registry.validate();
            }
            CookingInputRecipeManager.getInstance().clearItems();
            ItemCateManager.getInstance().clearTags();
            BookPageManager.getInstance().reload();
        });
    }

    public static <T> void reloadTag(RegistryProvider<T> registryProvider, ResourceManager manager) {
        Map<TagKey<T>, RegistryTagSet.Result<T>> temp = new Object2ObjectLinkedOpenHashMap<>();

        ResourceKey<? extends Registry<T>> registryKey = registryProvider.key();
        String registryPath = registryKey.identifier().getPath();
        String resourcePath = "tags/%s".formatted(registryPath);

        Map<Identifier, Resource> resources = manager.listResources(resourcePath, id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();

            String prefix = resourcePath + "/";
            String resourceFilePath = resourceId.getPath();

            if (!resourceFilePath.startsWith(prefix)) {
                continue;
            }

            String tagPath = resourceFilePath.substring(prefix.length()).replaceFirst("\\.json$", "");
            Identifier tagId = Identifier.fromNamespaceAndPath(resourceId.getNamespace(), tagPath);
            TagKey<T> tagKey = TagKey.create(registryProvider.key(), tagId);
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open(); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonElement json = JsonParser.parseReader(reader);
                DataResult<RegistryTagSet> dataResult = RegistryTagSet.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<RegistryTagSet> optional = dataResult.resultOrPartial(error -> {
                    log.error("Failed to parse tag {}: {}", tagId, error);
                });
                if (optional.isEmpty()) {
                    continue;
                }
                RegistryTagSet registryTagSet = optional.get();
                RegistryTagSet.Result<T> parsed = registryTagSet.result(registryProvider);
                temp.put(tagKey, parsed);

            } catch (IOException e) {
                log.error("Failed to load tag {}: {}", tagId, e.getMessage(), e);
            }
        }
        Map<TagKey<T>, List<Holder<T>>> result = new Object2ObjectLinkedOpenHashMap<>();
        for (TagKey<T> tagKey : temp.keySet()) {
            List<Holder<T>> holders = resolveTag(tagKey, temp, result, registryProvider, new ArrayList<>());
            result.put(tagKey, holders);
        }
//        System.out.println("Loaded Tag %s".formatted(registryProvider.key()));
//        if (!result.isEmpty()) {
//            System.out.println(result);
//        }
        registryProvider.bindTags(result);
    }

    private static <T> List<Holder<T>> resolveTag(TagKey<T> tagKey, Map<TagKey<T>, RegistryTagSet.Result<T>> temp, Map<TagKey<T>, List<Holder<T>>> result, RegistryProvider<T> registryProvider, List<TagKey<T>> resolving) {
        List<Holder<T>> cached = result.get(tagKey);
        if (cached != null) {
            return cached;
        }
        if (resolving.contains(tagKey)) {
            log.error("Detected circular tag reference: {} -> {}", resolving, tagKey);
            return List.of();
        }
        RegistryTagSet.Result<T> tagResult = temp.get(tagKey);
        if (tagResult == null) {
            return List.of();
        }
        List<TagKey<T>> nextResolving = new ArrayList<>(resolving);
        nextResolving.add(tagKey);
        List<Holder<T>> holders = new ArrayList<>();
        for (ResourceKey<T> id : tagResult.ids()) {
            registryProvider.get(id).ifPresent(holder -> {
                if (!holders.contains(holder)) {
                    holders.add(holder);
                }
            });
        }
        for (TagKey<T> subTagKey : tagResult.keys()) {
            List<Holder<T>> subHolders = resolveTag(subTagKey, temp, result, registryProvider, nextResolving);
            for (Holder<T> holder : subHolders) {
                if (!holders.contains(holder)) {
                    holders.add(holder);
                }
            }
        }

        result.put(tagKey, holders);

        return holders;
    }
}
