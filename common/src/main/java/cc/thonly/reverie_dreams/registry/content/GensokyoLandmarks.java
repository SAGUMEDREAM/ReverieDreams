package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoLandmark;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public final class GensokyoLandmarks {

    private GensokyoLandmarks() {
    }

    /**
     * Landmarks are fully datapack driven; generated files live under
     * {@code data/<namespace>/gensokyo_landmark/}.
     */
    public static void reload(ResourceManager manager) {
        RegistryProvider<GensokyoLandmark> registry = BuiltInRegistryProviders.GENSOKYO_LANDMARK;
        registry.clear();
        Map<Identifier, Resource> resources = manager.listResources("gensokyo_landmark", id ->
                id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String path = fileId.getPath();
            Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(),
                    path.substring("gensokyo_landmark/".length(), path.length() - ".json".length()));
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<GensokyoLandmark> result = GensokyoLandmark.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<GensokyoLandmark> landmark = result.result();
                if (landmark.isPresent()) {
                    BuiltInRegistryProviders.register(registry, id, landmark.get());
                } else {
                    log.error("Failed to parse gensokyo_landmark {}: {}", id,
                            result.error().map(Object::toString).orElse("Unknown error"));
                }
            } catch (IOException | RuntimeException e) {
                log.error("Failed to load gensokyo_landmark {}: {}", id, e.getMessage(), e);
            }
        }
    }

    public static Optional<GensokyoLandmark> get(Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        return BuiltInRegistryProviders.GENSOKYO_LANDMARK.get(id).map(reference -> reference.value());
    }

    /**
     * Dialog list order follows the source map legend, not resource pack order.
     */
    public static List<Map.Entry<Identifier, GensokyoLandmark>> sortedEntries() {
        return BuiltInRegistryProviders.GENSOKYO_LANDMARK.streamIdToValue()
                .sorted(Comparator
                        .comparingInt((Map.Entry<Identifier, GensokyoLandmark> entry) -> entry.getValue().order())
                        .thenComparing(entry -> entry.getKey().toString()))
                .toList();
    }
}
