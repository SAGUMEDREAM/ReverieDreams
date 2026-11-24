package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoStructureMetadata;
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
public final class GensokyoStructureMetadataRegistry {

    private GensokyoStructureMetadataRegistry() {
    }

    /**
     * Generated configuration is loaded through the shared registry reload lifecycle.
     * Files live under
     * {@code data/<namespace>/gensokyo_structure_metadata/}.
     */
    public static void reload(ResourceManager manager) {
        RegistryProvider<GensokyoStructureMetadata> registry = BuiltInRegistryProviders.GENSOKYO_STRUCTURE_METADATA;
        registry.clear();
        Map<Identifier, Resource> resources = manager.listResources("gensokyo_structure_metadata", id ->
                id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String path = fileId.getPath();
            Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(),
                    path.substring("gensokyo_structure_metadata/".length(), path.length() - ".json".length()));
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<GensokyoStructureMetadata> result = GensokyoStructureMetadata.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<GensokyoStructureMetadata> landmark = result.result();
                if (landmark.isPresent()) {
                    BuiltInRegistryProviders.register(registry, id, landmark.get());
                } else {
                    log.error("Failed to parse gensokyo_structure_metadata {}: {}", id,
                            result.error().map(Object::toString).orElse("Unknown error"));
                }
            } catch (IOException | RuntimeException e) {
                log.error("Failed to load gensokyo_structure_metadata {}: {}", id, e.getMessage(), e);
            }
        }
    }

}
