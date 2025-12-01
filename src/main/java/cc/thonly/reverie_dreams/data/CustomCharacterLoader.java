package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.util.VirtualZipFS;
import cc.thonly.reverie_dreams.util.skin.SkinFetcher;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

@Slf4j
public class CustomCharacterLoader {
    public static final String STR_PATH = "config/reverie_dreams/custom_character";
    public static final Path PATH = Paths.get(STR_PATH);
    private static final Gson GSON = new Gson();

    static {
        try {
            if (!Files.exists(PATH)) {
                Files.createDirectories(PATH);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: " + STR_PATH, e);
        }
    }

    public static void reload() {
        try (Stream<Path> listStream = Files.list(PATH)) {
            List<Path> list = listStream
                    .filter(pth -> pth.toString().endsWith(".zip"))
                    .toList();

            for (Path path : list) {
                try (ZipFile zipFile = new ZipFile(path.toFile(), ZipFile.OPEN_READ)) {
                    VirtualZipFS virtualZipFS = new VirtualZipFS(zipFile);
                    List<String> skinPaths = virtualZipFS.list("skin/");
                    for (String skinTexturePath : skinPaths) {
                        if (!skinTexturePath.endsWith(".png")) {
                            continue;
                        }
                        String skinConfigPath = skinTexturePath.replace(".png", ".json");
                        if (!(virtualZipFS.exists(skinTexturePath) && virtualZipFS.exists(skinConfigPath))) {
                            continue;
                        }
                        VirtualZipFS.FileEntry skinConfigFileEntry = virtualZipFS.get(skinConfigPath);
                        String baseName = skinConfigFileEntry.filename()
                                .replaceFirst("\\.[^.]+$", "");
                        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ReverieDreams.MOD_ID, baseName);
                        JsonElement json = JsonParser.parseString(new String(skinConfigFileEntry.data(), StandardCharsets.UTF_8));
                        DataResult<SkinConfig> parse = SkinConfig.CODEC.parse(JsonOps.INSTANCE, json);
                        Optional<SkinConfig> result = parse.result();
                        if (result.isEmpty()) {
                            continue;
                        }
                        SkinType skinType = new SkinType(id);
                        SkinConfig skinConfig = result.get();
                        skinType.setConfig(skinConfig);
                        skinConfig.setSkin(skinType);
                        byte[] bytes = virtualZipFS.getBytes(skinTexturePath);
                        Path tempFile = Files.createTempFile(id.getPath(), "_%s".formatted(LocalDateTime.now().hashCode()));
                        Files.write(tempFile, bytes);
                        File file = tempFile.toFile();
                        System.out.println(skinConfig.getType());
                        SkinFetcher.getSkinFromFile(file, skinConfig.getType() == SkinConfig.ModelType.SLIM);
                        RegistryHandlers.set(RegistryHandlers.SKIN_TYPE, id, skinType);
                        RegistryHandlers.set(RegistryHandlers.SKIN_CONFIG, id, skinConfig);
                        file.deleteOnExit();
                    }
                    virtualZipFS.close();
                } catch (Exception e) {
                    log.error("Can't read custom character pack in {}", path, e);
                }
            }
        } catch (Exception e) {
            log.error("Can't read custom characters", e);
        }
    }

}
