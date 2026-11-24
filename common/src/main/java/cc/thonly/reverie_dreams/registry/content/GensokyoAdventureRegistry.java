package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoAdventure;
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
public final class GensokyoAdventureRegistry {

    private GensokyoAdventureRegistry() {
    }

    /**
     * Generated configuration is loaded through the shared registry reload lifecycle.
     * Files live under
     * {@code data/<namespace>/gensokyo_adventure/}.
     */
    public static void reload(ResourceManager manager) {
        RegistryProvider<GensokyoAdventure> registry = BuiltInRegistryProviders.GENSOKYO_ADVENTURE;
        registry.clear();
        Map<Identifier, Resource> resources = manager.listResources("gensokyo_adventure", id ->
                id.getPath().endsWith(".json")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String path = fileId.getPath();
            Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(),
                    path.substring("gensokyo_adventure/".length(), path.length() - ".json".length()));
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<GensokyoAdventure> result = GensokyoAdventure.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<GensokyoAdventure> landmark = result.result();
                if (landmark.isPresent()) {
                    BuiltInRegistryProviders.register(registry, id, landmark.get());
                } else {
                    log.error("Failed to parse gensokyo_adventure {}: {}", id,
                            result.error().map(Object::toString).orElse("Unknown error"));
                }
            } catch (IOException | RuntimeException e) {
                log.error("Failed to load gensokyo_adventure {}: {}", id, e.getMessage(), e);
            }
        }
    }

}
