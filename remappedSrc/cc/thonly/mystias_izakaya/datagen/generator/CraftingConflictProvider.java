package cc.thonly.mystias_izakaya.datagen.generator;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.component.CraftingConflict;
import cc.thonly.reverie_dreams.datagen.generator.DataGeneratorUtil;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class CraftingConflictProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<ResourceLocation, CraftingConflict> registries = new Object2ObjectOpenHashMap<>();

    public CraftingConflictProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
        this.configured();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    protected CraftingConflictProvider registerEntry(CraftingConflict craftingConflict) {
        var id = BuiltInRegistries.ITEM.getKey(craftingConflict.getItem());
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceAll("/","-"));
        return this.registerEntry(id, craftingConflict);
    }

    protected CraftingConflictProvider registerEntry(ResourceLocation key, CraftingConflict craftingConflict) {
        this.registries.put(key, craftingConflict);
        return this;
    }

    protected abstract void configured();

    public void export(CachedOutput writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (var entry : this.registries.entrySet()) {
                ResourceLocation key = entry.getKey();
                CraftingConflict craftingConflict = entry.getValue();
                DataResult<JsonElement> result = CraftingConflict.CODEC.encodeStart(JsonOps.INSTANCE, craftingConflict);
                Optional<JsonElement> optional = result.result();
                Path gPath = DataGeneratorUtil.getData(path, MystiasIzakaya.MOD_ID, "crafting_conflict", null);

                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path output = gPath.resolve(key.getPath() + ".json");
                    String jsonString = this.gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    Files.createDirectories(output.getParent());

                    writer.writeIfNeeded(output, bytes, HashCode.fromBytes(bytes));
                }
            }
        } catch (Exception err) {
            log.error("Error: ", err);
        }
    }
}
