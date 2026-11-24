package cc.thonly.reverie_dreams.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;

class RecipeResourceLoaderTest {
    static Resource resource(String json) {
        return new Resource(null, () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    @SuppressWarnings("unchecked")
    static ResourceManager manager(Map<Identifier, Resource> entries) {
        return (ResourceManager) Proxy.newProxyInstance(ResourceManager.class.getClassLoader(), new Class[]{ResourceManager.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("listResources")) throw new UnsupportedOperationException(method.getName());
                    Map<Identifier, Resource> result = new LinkedHashMap<>();
                    entries.forEach((id, value) -> { if (((Predicate<Identifier>) args[1]).test(id)) result.put(id, value); });
                    return result;
                });
    }

    @Test void malformedOutOfRangeAndUnreadableRecipesDoNotStopGoodNeighbors() {
        Map<Identifier, Resource> resources = new LinkedHashMap<>();
        resources.put(Identifier.parse("test:sample_recipe/a" + ".json"), resource("1"));
        resources.put(Identifier.parse("test:sample_recipe/b.json"), resource("{broken"));
        resources.put(Identifier.parse("test:sample_recipe/c.json"), resource("100"));
        resources.put(Identifier.parse("test:sample_recipe/d.json"), new Resource(null, () -> { throw new IOException("fixture unreadable"); }));
        resources.put(Identifier.parse("test:sample_recipe/e.json"), resource("2"));
        Map<Identifier, Integer> loaded = new LinkedHashMap<>();
        assertDoesNotThrow(() -> RecipeResourceLoader.load(manager(resources), "sample_recipe", id -> true, Codec.intRange(1, 64), loaded::put));
        assertEquals(Map.of(Identifier.parse("test:a"), 1, Identifier.parse("test:e"), 2), loaded);
    }

    @Test void codecPartialResultsAreNotInstalled() {
        Codec<Integer> codec = Codec.INT.flatXmap(value -> value < 0 ? DataResult.error(() -> "fixture partial", 1) : DataResult.success(value), DataResult::success);
        Map<Identifier, Integer> loaded = new LinkedHashMap<>();
        RecipeResourceLoader.load(manager(Map.of(Identifier.parse("test:sample_recipe/bad.json"), resource("-1"),
                Identifier.parse("test:sample_recipe/good.json"), resource("2"))), "sample_recipe", id -> true, codec, loaded::put);
        assertEquals(Map.of(Identifier.parse("test:good"), 2), loaded);
    }

    @Test void failureInstallingOneRecipeDoesNotStopTheNext() {
        Map<Identifier, Integer> loaded = new LinkedHashMap<>();
        RecipeResourceLoader.load(manager(Map.of(Identifier.parse("test:sample_recipe/a.json"), resource("1"),
                Identifier.parse("test:sample_recipe/b.json"), resource("2"))), "sample_recipe", id -> true, Codec.INT,
                (id, value) -> { if (value == 1) throw new IllegalArgumentException("fixture rejected"); loaded.put(id, value); });
        assertEquals(Map.of(Identifier.parse("test:b"), 2), loaded);
    }
}
