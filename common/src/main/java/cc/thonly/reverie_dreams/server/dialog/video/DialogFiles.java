package cc.thonly.reverie_dreams.server.dialog.video;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
public class DialogFiles {
    public static final String STR_PATH = "config/reverie_dreams/video";
    public static final Path PATH = Paths.get(STR_PATH);

    static {
        try {
            if (!Files.exists(PATH)) {
                Files.createDirectories(PATH);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: " + STR_PATH, e);
        }
    }

    @Slf4j
    public static class FilesSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder builder) throws CommandSyntaxException {
            try {
                List<String> files = getFileNames(PATH);
                for (String file : files) {
                    cc.thonly.reverie_dreams.util.command.FilteredSuggestions.file(builder,file);
                }
            } catch (Exception err) {
                log.error("Can't get file suggestions", err);
            }
            return builder.buildFuture();
        }

        public static List<String> getFileNames(Path dir) throws IOException {
            try (var stream = Files.list(dir)) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
            }
        }
    }

    private static final BiMap<String, Entry> FILE_ID2ENTRY = HashBiMap.create();

    public static void reload() {
        FILE_ID2ENTRY.clear();
    }

    public static boolean contain(String filename) {
        Path filePath = Paths.get(DialogFiles.STR_PATH, filename);
        return Files.exists(filePath);
    }

    public static Entry add(Entry entry) {
        try {
            entry.parse();
        } catch (Exception err) {
            log.error("Fail to parse file {}", entry.getFileId(), err);
            return null;
        }
        FILE_ID2ENTRY.put(entry.getFileId(), entry);
        return entry;
    }

    public static Entry getFile(String fileId) {
        return FILE_ID2ENTRY.get(fileId);
    }

    public static Entry getEntry(String filename) {
        for (Map.Entry<String, Entry> entry : FILE_ID2ENTRY.entrySet()) {
            Entry value = entry.getValue();
            if (value.filename.equals(filename)) {
                return value;
            }
        }
        return null;
    }

    public static String getFileId(String filename) {
        for (Map.Entry<String, Entry> entry : FILE_ID2ENTRY.entrySet()) {
            Entry value = entry.getValue();
            if (value.filename.equals(filename)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Getter
    public static class Entry {
        private final LinkedList<LinkedList<String>> data = new LinkedList<>();
        private final String filename;
        private final String fileId;
        private byte[] fileBytes;
        private byte[] decodedBytes;
        private DialogVideoData videoData;
        private boolean videoParsed;

        public Entry(String filename, String fileId) {
            this.filename = filename;
            this.fileId = fileId;
        }

        public void parse() {
            this.bytes();
        }

        private byte[] bytes() {
            if (this.fileBytes != null) {
                return this.fileBytes;
            }
            Path filePath = Paths.get(DialogFiles.STR_PATH, this.filename);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + filePath);
            }
            try {
                this.fileBytes = Files.readAllBytes(filePath);
                return this.fileBytes;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + filePath, e);
            }
        }

        private byte[] decodedBytes() {
            if (this.decodedBytes == null) {
                byte[] bytes = this.bytes();
                if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B) {
                    try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
                         ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(8192, bytes.length * 4))) {
                        gzip.transferTo(out);
                        bytes = out.toByteArray();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to decompress file: " + this.filename, e);
                    }
                }
                this.decodedBytes = bytes;
            }
            return this.decodedBytes;
        }

        public JsonElement json() throws Exception {
            try (InputStream in = new ByteArrayInputStream(this.decodedBytes());
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader);
            } catch (JsonParseException e) {
                throw new RuntimeException("Failed to parse JSON: " + this.filename, e);
            }
        }

        public DialogVideoData video() {
            if (!this.videoParsed) {
                this.videoParsed = true;
                try (JsonReader reader = new JsonReader(
                        new InputStreamReader(new ByteArrayInputStream(this.decodedBytes()), StandardCharsets.UTF_8))) {
                    this.videoData = DialogVideoData.read(reader);
                } catch (IOException | RuntimeException e) {
                    throw new RuntimeException("Failed to parse dialog video: " + this.filename, e);
                }
            }
            return this.videoData;
        }
    }
}
