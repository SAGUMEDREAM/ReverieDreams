package cc.thonly.reverie_dreams.datagen.generator;

import com.google.common.hash.HashCode;
import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class AbstractJsonElementWriterProvider implements DataProvider {
    protected final Map<String, JsonElement> path2JsonElement = new Object2ObjectLinkedOpenHashMap<>();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;

    public AbstractJsonElementWriterProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        this.configured();
        this.export(cachedOutput);
        return CompletableFuture.completedFuture(null);
    }

    public void export(CachedOutput cachedOutput) {
        Path basePath = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        for (var entry : path2JsonElement.entrySet()) {
            String relativePath = entry.getKey();
            JsonElement element = entry.getValue();

            byte[] bytes = GSON.toJson(element).getBytes(StandardCharsets.UTF_8);
            Path exportPath = basePath.resolve(relativePath);

            try {
                Files.createDirectories(exportPath.getParent());
                cachedOutput.writeIfNeeded(exportPath, bytes, HashCode.fromBytes(bytes));
            } catch (Exception err) {
                log.error("Failed to export JSON file: {}", exportPath, err);
            }
        }
    }

    protected abstract void configured();

    public void addElement(Type type, Identifier location, String subPath, JsonElement element) {
        String relativePath = type.path + location.getNamespace() + "/" + subPath + "/" + location.getPath() + ".json";
        this.path2JsonElement.put(relativePath, element);
    }

    protected JsonElement strToJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return new JsonObject(); // 空对象
        }

        try {
            return JsonParser.parseString(jsonStr);
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    @Override
    public String getName() {
        return "Json Element Writer";
    }

    public enum Type {
        ASSETS("assets/"),
        DATA("data/");

        final String path;
        Type(String path) { this.path = path; }
    }
}