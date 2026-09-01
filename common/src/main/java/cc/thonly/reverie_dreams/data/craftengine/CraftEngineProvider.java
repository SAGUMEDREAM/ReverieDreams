package cc.thonly.reverie_dreams.data.craftengine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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

import java.io.IOException;
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

    /**
     * 每个 YAML 文件包含的 Item 数量。
     */
    private static final int ITEMS_PER_FILE = 100;

    /**
     * 留一点安全余量，避免不同 YAML 内容造成边界问题。
     */
    private static final int YAML_SAFE_LIMIT = 3_000_000;

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
        return new ItemDefinitionList(
                entries.stream()
                        .map(item -> (ItemLike) item)
                        .toList()
        );
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

        List<ItemDefinitionList> itemDefinitionList = new ArrayList<>();

        // 按固定数量分片
        for (int i = 0; i < items.size(); i += ITEMS_PER_FILE) {
            int end = Math.min(
                    i + ITEMS_PER_FILE,
                    items.size()
            );

            List<Item> chunk = items.subList(i, end);

            itemDefinitionList.add(
                    generateItems(chunk)
            );
        }

        List<BlockDefinitionList> blockDefinitionList = new ArrayList<>();
        blockDefinitionList.add(
                generateBlocks(blocks)
        );

        return new CraftEngineDefinition(
                itemDefinitionList,
                blockDefinitionList
        );
    }

    /**
     * 普通 YAML 输出。
     */
    @SneakyThrows
    public static void toFile(Object object, Path path) {
        ObjectMapper mapper = createMapper();

        Files.createDirectories(path.getParent());

        mapper.writeValue(path.toFile(), object);
    }

    /**
     * 将 Items 自动拆成多个 YAML 文件。
     *
     * 例如：
     *
     * items-000.yml
     * items-001.yml
     * items-002.yml
     *
     * 每个文件都会自动控制在安全大小以内。
     */
    @SneakyThrows
    public static void toItemFiles(
            List<ItemDefinitionList> itemDefinitions,
            Path directory
    ) {
        Files.createDirectories(directory);

        ObjectMapper mapper = createMapper();

        // 删除旧文件
        try (var stream = Files.list(directory)) {
            stream.filter(path ->
                            path.getFileName()
                                    .toString()
                                    .matches("items-\\d+\\.yml")
                    )
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error(
                                    "Failed to delete old file: {}",
                                    path,
                                    e
                            );
                        }
                    });
        }

        for (int i = 0; i < itemDefinitions.size(); i++) {
            Path output = directory.resolve(
                    String.format(
                            "items-%03d.yml",
                            i
                    )
            );

            mapper.writeValue(
                    output.toFile(),
                    itemDefinitions.get(i)
            );

            log.info(
                    "Generated {}",
                    output
            );
        }
    }

    /**
     * 递归拆分。
     *
     * 如果当前 chunk 序列化后没有超限，就直接写文件。
     * 如果超限，则从中间拆成两半继续处理。
     */
    private static int splitItems(
            ObjectMapper mapper,
            List<Item> items,
            Path directory,
            String filePrefix,
            int fileIndex
    ) throws IOException {

        if (items.isEmpty()) {
            return fileIndex;
        }

        ItemDefinitionList definition = generateItems(items);

        String yaml = mapper.writeValueAsString(definition);

        int codePoints = yaml.codePointCount(
                0,
                yaml.length()
        );

        // 小于安全限制，直接写
        if (codePoints <= YAML_SAFE_LIMIT || items.size() == 1) {
            Path output = directory.resolve(
                    String.format(
                            "%s-%03d.yml",
                            filePrefix,
                            fileIndex
                    )
            );

            Files.writeString(
                    output,
                    yaml,
                    StandardCharsets.UTF_8
            );

            log.info(
                    "Generated {} ({} items, {} code points)",
                    output,
                    items.size(),
                    codePoints
            );

            return fileIndex + 1;
        }

        // 太大，继续二分
        int middle = items.size() / 2;

        List<Item> left = new ArrayList<>(
                items.subList(0, middle)
        );

        List<Item> right = new ArrayList<>(
                items.subList(middle, items.size())
        );

        fileIndex = splitItems(
                mapper,
                left,
                directory,
                filePrefix,
                fileIndex
        );

        fileIndex = splitItems(
                mapper,
                right,
                directory,
                filePrefix,
                fileIndex
        );

        return fileIndex;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper(
                new YAMLFactory()
        );

        mapper.setSerializationInclusion(
                JsonInclude.Include.NON_EMPTY
        );

        return mapper;
    }
}