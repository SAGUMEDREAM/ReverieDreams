package cc.thonly.reverie_dreams.world.gensokyo.update;

import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Portable, world-owned journals and compressed baselines; atomic, flushed metadata replacement.
 */
public final class StructureUpdateStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public final Path root;
    public final JsonObject ledger;
    public final String readOnlyError;

    public StructureUpdateStorage(ServerLevel level) throws IOException {
        root = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data/reverie_dreams/structure_updates").toAbsolutePath().normalize();
        validate(root);
        Files.createDirectories(root);
        Path file = root.resolve("ledger.json");
        JsonObject read;
        String error = null;
        try {
            read = Files.exists(file) ? readJson(file) : new JsonObject();
            if (read.has("schema") && read.get("schema").getAsInt() != 1)
                throw new IOException("Unsupported structure ledger schema");
        } catch (Exception failure) {
            read = new JsonObject();
            error = "Structure ledger is read-only until repaired: " + file + ": " + failure;
        }
        ledger = read;
        readOnlyError = error;
        ledger.addProperty("schema", 1);
        for (String name : List.of("installations", "actors", "thaw"))
            if (!ledger.has(name))
                ledger.add(name, new JsonObject());
    }

    public JsonObject installations() {
        return ledger.getAsJsonObject("installations");
    }

    public JsonObject actors() {
        return ledger.getAsJsonObject("actors");
    }

    public void save() throws IOException {
        if (readOnlyError != null)
            throw new IOException(readOnlyError);
        writeJson(root.resolve("ledger.json"), ledger);
    }

    public Path baseline(String hash) throws IOException {
        if (!hash.matches("[a-f0-9]{64}"))
            throw new IOException("Invalid content hash");
        return root.resolve("baselines").resolve(hash + ".nbt");
    }

    public Path job(UUID id) {
        return root.resolve("jobs").resolve(id.toString());
    }

    public void archive(StructureBlueprint blueprint) throws IOException {
        Path path = baseline(blueprint.hash());
        if (!Files.exists(path))
            writeNbt(path, blueprint.encode());
    }

    public static JsonObject readJson(Path path) throws IOException {
        validate(path);
        if (Files.size(path) > 16 * 1024 * 1024)
            throw new IOException("Oversized structure journal");
        try {
            return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IOException("Invalid structure journal: " + path, e);
        }
    }

    public static void writeJson(Path path, JsonObject data) throws IOException {
        validate(path);
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName() + ".writing");
        byte[] bytes = GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
        try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining())
                channel.write(buffer);
            channel.force(true);
        }
        move(tmp, path);
    }

    public static void writeNbt(Path path, CompoundTag tag) throws IOException {
        validate(path);
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName() + ".writing");
        NbtIo.writeCompressed(tag, tmp);
        try (FileChannel channel = FileChannel.open(tmp, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
        move(tmp, path);
    }

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void validate(Path path) throws IOException {
        for (Path p = path.toAbsolutePath().normalize(); p != null; p = p.getParent())
            if (Files.exists(p, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(p))
                throw new IOException("Linked structure data path: " + p);
    }
}
