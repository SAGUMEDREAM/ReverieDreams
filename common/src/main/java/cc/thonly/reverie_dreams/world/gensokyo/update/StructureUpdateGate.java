package cc.thonly.reverie_dreams.world.gensokyo.update;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.*;
import java.util.*;

/**
 * Publication gate, restored before levels/players load. Never alters a chunk's generation status.
 */
public final class StructureUpdateGate {
    private static final Map<MinecraftServer, Map<UUID, BoundingBox>> GATES = new WeakHashMap<>();

    private StructureUpdateGate() {
    }

    public static synchronized void set(MinecraftServer server, UUID job, BoundingBox box) {
        GATES.computeIfAbsent(server, k -> new HashMap<>()).put(job, box);
    }

    public static synchronized void clear(MinecraftServer server, UUID job) {
        var gates = GATES.get(server);
        if (gates != null)
            gates.remove(job);
    }

    public static synchronized boolean blocks(ServerLevel level, long key) {
        if (!level.dimension().identifier().toString().equals("reverie_dreams:gensokyo"))
            return false;
        var gates = GATES.get(level.getServer());
        if (gates == null)
            return false;
        ChunkPos p = new ChunkPos((int) key, (int) (key >> 32));
        return gates.values().stream().anyMatch(b -> p.getMinBlockX() <= b.maxX() + 16 && p.getMaxBlockX() >= b.minX() - 16 && p.getMinBlockZ() <= b.maxZ() + 16 && p.getMaxBlockZ() >= b.minZ() - 16);
    }

    public static boolean freezes(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !blocks(level, entity.chunkPosition().pack()))
            return false;
        return entity.entityTags().stream().anyMatch(t -> t.startsWith(StructureActors.LANDMARK) || t.startsWith("rd_scarlet_point:"));
    }

    public static void restore(MinecraftServer server) {
        Path jobs = server.getWorldPath(LevelResource.ROOT).resolve("data/reverie_dreams/structure_updates/jobs");
        if (!Files.isDirectory(jobs))
            return;
        try (var dirs = Files.list(jobs)) {
            for (Path dir : dirs.toList()) {
                Path file = dir.resolve("job.json");
                if (!Files.isRegularFile(file))
                    continue;
                JsonObject j;
                try {
                    j = StructureUpdateStorage.readJson(file);
                } catch (Exception damaged) {
                    j = StructureUpdateStorage.readJson(dir.resolve("gate.json"));
                }
                if (!j.has("gated") || !j.get("gated").getAsBoolean())
                    continue;
                var b = j.getAsJsonArray("bounds");
                set(server, UUID.fromString(j.get("id").getAsString()), new BoundingBox(b.get(0).getAsInt(), b.get(1).getAsInt(), b.get(2).getAsInt(), b.get(3).getAsInt(), b.get(4).getAsInt(), b.get(5).getAsInt()));
            }
        } catch (Exception e) {
            // Keep the rest of the server usable while preventing entry into an uncertain dimension.
            set(server, new UUID(0, 0), new BoundingBox(-30000000, -2048, -30000000, 30000000, 4096, 30000000));
            cc.thonly.reverie_dreams.ReverieDreams.LOGGER.error("Damaged structure journal: Gensokyo entry is gated until the retained journal is repaired", e);
        }
    }
}
