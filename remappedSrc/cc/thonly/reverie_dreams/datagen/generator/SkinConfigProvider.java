package cc.thonly.reverie_dreams.datagen.generator;

import cc.thonly.reverie_dreams.entity.skin.SkinType;
import cc.thonly.reverie_dreams.entity.skin.SkinConfig;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class SkinConfigProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<ResourceLocation, SkinConfig> configList = new Object2ObjectLinkedOpenHashMap<>();

    public SkinConfigProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    public abstract void configured();

    protected void addConfig(ResourceLocation id, SkinConfig config) {
        this.configList.put(id, config);
    }

    protected void addConfig(SkinType skin, SkinConfig config) {
        this.configList.put(skin.getId(), config);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    public void export(CachedOutput writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (Map.Entry<ResourceLocation, SkinConfig> identifierNPCSkinConfigEntry : this.configList.entrySet()) {
                ResourceLocation key = identifierNPCSkinConfigEntry.getKey();
                SkinConfig config = identifierNPCSkinConfigEntry.getValue();
                DataResult<JsonElement> result = SkinConfig.CODEC.encodeStart(JsonOps.INSTANCE, config);
                if (!result.isSuccess()) {
                    continue;
                }
                Path generatePath = DataGeneratorUtil.getData(path, key.getNamespace(), "skin_config/", null);
                Path output = generatePath.resolve(key.getPath() + ".json");
                JsonElement element = result.getOrThrow();
                String jsonString = gson.toJson(element);
                byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                Files.createDirectories(output.getParent());

                writer.writeIfNeeded(output, bytes, HashCode.fromBytes(bytes));
            }
        } catch (Exception err) {
            log.error("Error: ", err);
        }
    }

    @Override
    public String getName() {
        return "Skin config";
    }
}
