package cc.thonly.reverie_dreams.datagen.generator;

import cc.thonly.reverie_dreams.datagen.entry.SoundEventBuilder;
import cc.thonly.reverie_dreams.sound.JukeBoxEntry;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class SoundProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<RegistryWrapper.WrapperLookup> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, SoundEventBuilder> identifierSoundEventMap = new Object2ObjectOpenHashMap<>();

    public SoundProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
        this.output = output;
        this.future = future;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    public SoundProvider add(Identifier id, SoundEventBuilder entry) {
        this.identifierSoundEventMap.put(id, entry);
        return this;
    }

    public SoundProvider addWithSoundEvent(SoundEvent soundEvent, @Nullable String soundPath) {
        Identifier id = soundEvent.id();
        SoundEventBuilder entry = new SoundEventBuilder(id);
        entry.setSubtitle(id);
        if (soundPath != null) {
            entry.addSounds(soundPath);
        } else {
            entry.addSounds(id.toString());
        }
        this.identifierSoundEventMap.put(id, entry);
        return this;
    }

    public SoundProvider addWithRecords(JukeBoxEntry jukeBoxEntry, @Nullable String soundPath) {
        JukeboxSong ref = jukeBoxEntry.getRef();
        Identifier id = ref.soundEvent().value().id();
        SoundEventBuilder entry = new SoundEventBuilder(id);
        entry.setSubtitle(id);
        if (soundPath != null) {
            entry.addSoundsByName(soundPath);
        } else {
            entry.addSoundsByName(id.getNamespace()  + ":" + "records/"+ id.getPath());
        }
        this.identifierSoundEventMap.put(id, entry);
        return this;
    }

    public abstract void configured();

//    @SuppressWarnings("deprecation")
    public void export(DataWriter writer) {
        try {
            Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
            Map<String, List<SoundEventBuilder>> namespaceToBuild = new LinkedHashMap<>();
            for (var entry : this.identifierSoundEventMap.entrySet()) {
                String namespace = entry.getKey().getNamespace();
                SoundEventBuilder ref = entry.getValue();
                List<SoundEventBuilder> list = namespaceToBuild.computeIfAbsent(namespace, k -> new LinkedList<>());
                list.add(ref);
            }
            for (var entry : namespaceToBuild.entrySet()) {
                String namespace = entry.getKey();
                Path generatePath = DataGeneratorUtil.getAssetsByNullable(path, namespace, null, null);
                Path output = generatePath.resolve("sounds.json");
                Files.createDirectories(generatePath);
                List<SoundEventBuilder> list = entry.getValue();
                JsonObject object = new JsonObject();
                for (var builder : list) {
                    Identifier key = builder.getKey();
                    JsonElement element = builder.toJsonElement();
                    object.add(key.getPath(), element);
                }
                String jsonString = this.gson.toJson(object);
                byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                writer.write(output, bytes, HashCode.fromBytes(bytes));
            }
        } catch (Exception err) {
            log.error("Can't export sounds.json: ", err);
        }
    }

    @Override
    public String getName() {
        return "Sounds JSON Provider";
    }
}
