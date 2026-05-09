package cc.thonly.reverie_dreams.fabric.datagen.generator;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.JukeboxSong;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class AbstractJukeboxProvider implements DataProvider {
    public final FabricPackOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, JukeboxSong> identifierJukeboxSongMap = new Object2ObjectOpenHashMap<>();

    public AbstractJukeboxProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    public JukeboxSong add(Identifier id, JukeboxSong song) {
        return this.identifierJukeboxSongMap.put(id, song);
    }

    public abstract void configured();

    public void export(CachedOutput writer) {
        try {
            Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
            for (var entry: this.identifierJukeboxSongMap.entrySet()) {
                String namespace = entry.getKey().getNamespace();
                String key = entry.getKey().getPath();
                JukeboxSong ref = entry.getValue();
                Path generatePath = DataGeneratorUtil.getData(path, namespace, Registries.JUKEBOX_SONG, null);

                DataResult<JsonElement> result = JukeboxSong.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, ref);
                Optional<JsonElement> optional = result.result();

                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path output = generatePath.resolve(key + ".json");
                    String jsonString = this.gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    Files.createDirectories(output.getParent());

                    writer.writeIfNeeded(output, bytes, HashCode.fromBytes(bytes));
                }

            }
        } catch (Exception err) {
            log.error("Error: ",err);
        }
    }

    @Override
    public String getName() {
        return "Jukebox Song JSON Provider";
    }
}
