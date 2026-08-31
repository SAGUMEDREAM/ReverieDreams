package cc.thonly.reverie_dreams.data.craftengine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.utils.GameInstance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("deprecation")
public class CraftEngineProvider {
    public static final Yaml yaml = new Yaml();
    public static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(
                    Modifier.STATIC
            )
            .create();

    public static Object convertJson(JsonElement element) {
        if (element.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();

            element.getAsJsonObject()
                   .entrySet()
                   .forEach(entry ->
                           map.put(
                                   entry.getKey(),
                                   convertJson(entry.getValue())
                           )
                   );

            return map;
        }

        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();

            for (JsonElement e : element.getAsJsonArray()) {
                list.add(convertJson(e));
            }

            return list;
        }

        if (element.isJsonPrimitive()) {
            return element.getAsJsonPrimitive().isBoolean()
                    ? element.getAsBoolean()
                    : element.getAsJsonPrimitive().isNumber()
                    ? element.getAsNumber()
                    : element.getAsString();
        }

        return null;
    }

    public static ItemDefinitionList generateItems(List<Item> entries) {
        return new ItemDefinitionList(entries.stream().map(item -> (ItemLike) item).toList());
    }

    public static BlockDefinitionList generateBlocks(List<Block> entries) {
        return new BlockDefinitionList(entries);
    }

    public static CraftEngineDefinition fromNamespace(String namespace) {
        MinecraftServer server = GameInstance.getServer();
        if (server == null) {
            log.error("The server must be running.");
            return null;
        }
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Holder.Reference<Item> reference = item.builtInRegistryHolder();
            ResourceKey<Item> key = reference.key();
            Identifier identifier = key.identifier();
            if (!identifier.getNamespace().equalsIgnoreCase(namespace)) {
                continue;
            }
            items.add(item);
        }
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Holder.Reference<Block> reference = block.builtInRegistryHolder();
            ResourceKey<Block> key = reference.key();
            Identifier identifier = key.identifier();
            if (!identifier.getNamespace().equalsIgnoreCase(namespace)) {
                continue;
            }
            blocks.add(block);
        }
        return new CraftEngineDefinition(generateItems(items), generateBlocks(blocks));
    }

    @SneakyThrows
    public static void toFile(Object object, Path path) {

        ObjectMapper mapper =
                new ObjectMapper(new YAMLFactory());

        mapper.setSerializationInclusion(
                JsonInclude.Include.NON_EMPTY
        );

        mapper.writeValue(path.toFile(), object);
    }
}
