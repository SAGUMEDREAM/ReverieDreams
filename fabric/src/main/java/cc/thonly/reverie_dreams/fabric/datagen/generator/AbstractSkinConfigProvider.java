package cc.thonly.reverie_dreams.fabric.datagen.generator;

import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.fabric.util.DataGeneratorUtil;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
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
public abstract class AbstractSkinConfigProvider implements DataProvider {
    public final FabricPackOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, SkinConfig> configList = new Object2ObjectLinkedOpenHashMap<>();

    public AbstractSkinConfigProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    public abstract void configured(HolderLookup.Provider provider);

    protected void addConfig(Identifier id, SkinConfig config) {
        this.configList.put(id, config);
    }

    protected void addConfig(SkinType skin, SkinConfig config) {
        this.configList.put(skin.getId(), config);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return this.future.thenAcceptAsync(provider->{
            this.configured(provider);
            DataProviderHelper.outputFile(writer, this.configList, SkinConfig.CODEC, (id, config) -> {
                config.bindRegistryKey(id);
                return config;
            }, "skin_config/");
        });
    }

    public void export(CachedOutput writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (Map.Entry<Identifier, SkinConfig> identifierNPCSkinConfigEntry : this.configList.entrySet()) {
                Identifier key = identifierNPCSkinConfigEntry.getKey();
                SkinConfig config = identifierNPCSkinConfigEntry.getValue();
                config.bindRegistryKey(key);
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
