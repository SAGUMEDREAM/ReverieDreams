package cc.thonly.reverie_dreams.recipe;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/** A bad resource rejects that recipe only; complete recipes are installed independently. */
public final class RecipeResourceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeResourceLoader.class);

    private RecipeResourceLoader() { }

    public static <R> void load(ResourceManager manager, String directory, Predicate<Identifier> filter,
                                Codec<R> codec, BiConsumer<Identifier, R> install) {
        var resources = manager.listResources(directory, id -> id.getPath().endsWith(".json") && filter.test(id));
        for (var entry : resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            Identifier resourceId = entry.getKey();
            try {
                String path = resourceId.getPath();
                Identifier recipeId = Identifier.fromNamespaceAndPath(resourceId.getNamespace(),
                        path.substring(directory.length() + 1, path.length() - ".json".length()));
                R recipe;
                try (var reader = entry.getValue().openAsReader()) {
                    recipe = codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
                }
                install.accept(recipeId, recipe);
            } catch (Exception failure) {
                LOGGER.error("Skipping invalid {} recipe resource {}", directory, resourceId, failure);
            }
        }
    }
}
