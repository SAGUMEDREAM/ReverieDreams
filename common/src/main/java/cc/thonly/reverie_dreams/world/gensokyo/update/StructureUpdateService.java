package cc.thonly.reverie_dreams.world.gensokyo.update;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoStructureGeneration;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Persistent single-landmark maintenance jobs. All live world access stays on the server thread.
 */
public final class StructureUpdateService {
    private static final Map<ServerLevel, StructureUpdateService> SERVICES = Collections.synchronizedMap(new WeakHashMap<>());
    private final ServerLevel level;
    private final StructureUpdateTargets generation;
    private final StructureUpdateStorage storage;
    private final StructureActors actors;
    private final List<Job> jobs = new ArrayList<>();
    private final Map<String, CompletableFuture<StructureBlueprint>> preparing = new HashMap<>();
    private final Set<String> archived = new HashSet<>();
    private final Map<String, CompletableFuture<Void>> archives = new HashMap<>();
    private long nextAutomatic, flushAt;
    private Job active;
    private String loadError;

    public StructureUpdateService(ServerLevel level, StructureUpdateTargets generation) {
        this.level = level;
        this.generation = generation;
        try {
            storage = new StructureUpdateStorage(level);
            actors = new StructureActors(level, storage);
            loadError = storage.readOnlyError;
            Path directory = storage.root.resolve("jobs");
            if (Files.isDirectory(directory))
                try (var paths = Files.list(directory)) {
                    for (Path p : paths.sorted().toList())
                        if (Files.isRegularFile(p.resolve("job.json")))
                            try {
                                jobs.add(new Job(StructureUpdateStorage.readJson(p.resolve("job.json"))));
                            } catch (Exception invalid) {
                                loadError = "Repair the retained structure journal " + p + ": " + invalid;
                                ReverieDreams.LOGGER.error(loadError, invalid);
                            }
                }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open structure update ledger", e);
        }
        SERVICES.put(level, this);
    }

    public static void removed(Entity entity, Entity.RemovalReason reason) {
        if (entity.level() instanceof ServerLevel l) {
            var service = SERVICES.get(l);
            if (service != null) {
                if (l.getServer().isSameThread())
                    service.actors.removed(entity, reason);
                else
                    l.getServer().execute(() -> service.actors.removed(entity, reason));
            }
        }
    }

    public static void loaded(Entity entity) {
        if (entity.level() instanceof ServerLevel level) {
            if (!level.getServer().isSameThread()) {
                level.getServer().execute(() -> loaded(entity));
                return;
            }
            for (var entry : new ArrayList<>(SERVICES.entrySet()))
                if (entry.getKey().getServer() == level.getServer())
                    entry.getValue().actors.loaded(entity);
        }
    }

    public static void reloaded(net.minecraft.server.MinecraftServer server) {
        for (var entry : new ArrayList<>(SERVICES.entrySet()))
            if (entry.getKey().getServer() == server) {
                var service = entry.getValue();
                if (service.generation instanceof GensokyoStructureGeneration authored)
                    authored.reloadCatalogue();
                service.preparing.clear();
                service.nextAutomatic = 0;
            }
    }

    public static void saved(net.minecraft.server.MinecraftServer server) {
        for (var entry : new ArrayList<>(SERVICES.entrySet()))
            if (entry.getKey().getServer() == server) {
                entry.getValue().actors.acknowledgeThawed(true);
                entry.getValue().flush();
            }
    }

    public static void stopped(net.minecraft.server.MinecraftServer server) {
        flush(server);
        SERVICES.entrySet().removeIf(e -> e.getKey().getServer() == server);
    }

    public static void flush(net.minecraft.server.MinecraftServer server) {
        for (var e : new ArrayList<>(SERVICES.entrySet()))
            if (e.getKey().getServer() == server) {
                List<CompletableFuture<?>> writes = new ArrayList<>(e.getValue().archives.values());
                for (Job job : e.getValue().jobs)
                    if (job.io != null)
                        writes.add(job.io);
                for (var write : writes)
                    try {
                        write.join();
                    } catch (CompletionException failure) {
                        ReverieDreams.LOGGER.error("Structure snapshot I/O failed; retaining journal", failure);
                    }
                e.getValue().flush();
            }
    }

    public void claim(String landmark, String actor, Entity entity) {
        if (loadError != null)
            throw new IllegalStateException(loadError);
        actors.claim(landmark, actor, entity);
    }

    public boolean maySpawn(String landmark, String actor) {
        return loadError == null && actors.maySpawn(landmark, actor);
    }

    public boolean paused(String id) {
        return loadError != null || jobs.stream().anyMatch(j -> j.landmark().equals(id) && j.gated());
    }

    public boolean hasWork() {
        return jobs.stream().anyMatch(j -> !j.terminal());
    }

    public List<String> landmarkIds() {
        return generation.anchors().stream().map(Identifier::toString).sorted().toList();
    }

    private CompletableFuture<StructureBlueprint> prepare(String id) {
        return preparing.computeIfAbsent(id, k -> generation.blueprint(Identifier.parse(k)));
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws Exception;
    }

    private CompletableFuture<Void> io(IoAction action) {
        return CompletableFuture.runAsync(() -> {
            try {
                action.run();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, Util.ioPool());
    }

    private CompletableFuture<Void> archive(StructureBlueprint blueprint) {
        return archives.computeIfAbsent(blueprint.hash(), hash -> io(() -> storage.archive(blueprint)));
    }

    public UUID enqueue(String id, boolean rebuild, boolean automatic) throws IOException {
        if (loadError != null)
            throw new IOException(loadError);
        if (!landmarkIds().contains(id))
            throw new IllegalArgumentException("Unknown template landmark: " + id);
        for (Job existing : jobs)
            if (existing.landmark().equals(id) && (!existing.terminal() || existing.gated()))
                return existing.id();
        if (jobs.stream().anyMatch(j -> j.phase().equals("NEEDS_REVIEW")))
            throw new IllegalStateException("Recover the interrupted structure job before starting another update");
        JsonObject j = new JsonObject();
        j.addProperty("schema", 1);
        j.addProperty("id", UUID.randomUUID().toString());
        j.addProperty("landmark", id);
        j.addProperty("mode", rebuild ? "rebuild" : "update");
        j.addProperty("automatic", automatic);
        j.addProperty("phase", "PREPARE");
        j.addProperty("gated", false);
        j.addProperty("cursor", 0);
        j.addProperty("message", "Queued");
        Job job = new Job(j);
        job.save();
        jobs.add(job);
        prepare(id);
        return job.id();
    }

    public List<String> status(String selector) {
        if (loadError != null)
            return List.of("BLOCKED: " + loadError);
        List<String> lines = new ArrayList<>();
        for (String id : landmarkIds())
            if (selector.equals("*") || selector.equals(id)) {
                JsonObject installed = storage.installations().has(id) ? storage.installations().getAsJsonObject(id) : null;
                var future = prepare(id);
                String target = future.isDone() && !future.isCompletedExceptionally() ? Integer.toString(future.join().version()) : "loading";
                lines.add(id + " installed=" + (installed == null ? "LEGACY_UNKNOWN / MISSING" : installed.get("version")) + " target=" + target + (installed != null && installed.has("partial") ? " conflicts=" + installed.get("partial") : ""));
            }
        for (Job job : jobs)
            if (selector.equals("*") || selector.equals(job.landmark()))
                lines.add(job.describe());
        return lines;
    }

    public List<String> preview(String selector) {
        List<String> lines = new ArrayList<>(status(selector));
        if (loadError != null)
            return lines;
        for (String id : landmarkIds())
            if (selector.equals("*") || selector.equals(id)) {
                var f = prepare(id);
                if (!f.isDone() || f.isCompletedExceptionally())
                    continue;
                var b = f.join();
                long selected = b.chunks().values().stream().mapToLong(s -> s.positions().length).sum();
                long loaded = b.neighborhood().stream().filter(k -> level.getChunkSource().getChunkNow((int) (long) k, (int) (k >> 32)) != null).count();
                lines.add(id + " bounds=" + b.bounds() + " selected=" + selected + " actors=" + b.actors().size() + " chunks=" + b.chunks().size() + " support-loaded=" + loaded + "/" + b.neighborhood().size());
            }
        lines.add("计划预览不加载区块、不写地形。实际方块/玩家改动/容器/实体冲突在执行前完整扫描；此处不是全图冲突检测结果。");
        return lines;
    }

    public String jobStatus(UUID id) {
        return find(id).describe();
    }

    public void resume(UUID id) throws IOException {
        Job job = find(id);
        if (!job.phase().equals("NEEDS_REVIEW"))
            throw new IllegalStateException("Only NEEDS_REVIEW jobs can be resumed");
        job.data.addProperty("cursor", 0);
        job.runtimeReset();
        job.phase("LOAD_APPLY");
        active = job;
    }

    public void cancel(UUID id) throws IOException {
        Job job = find(id);
        if (job.terminal())
            return;
        if (job.io != null && !job.io.isDone()) {
            job.data.addProperty("cancel_requested", true);
            job.save();
            return;
        }
        if (job.mutated()) {
            job.data.addProperty("rollback", true);
            job.data.addProperty("phase", "LOAD_APPLY");
            job.data.addProperty("cursor", 0);
            job.runtimeReset();
            job.message("Cancellation requested; restoring authored changes");
        } else
            job.finish("CANCELLED", "Cancelled before mutation");
        job.save();
    }

    public void rollback(UUID id) throws IOException {
        Job job = find(id);
        if (!job.mutated() || !Files.exists(job.path().resolve("actors.nbt")))
            throw new IllegalStateException("Job has no completed mutation backup");
        if (job.phase().equals("ROLLED_BACK"))
            return;
        if (job.io != null && !job.io.isDone())
            throw new IllegalStateException("Wait for the pending journal write before rollback");
        if (jobs.stream().anyMatch(j -> j != job && j.mutated() && !j.terminal()))
            throw new IllegalStateException("Finish or cancel the active mutation first");
        job.data.addProperty("fresh_rollback", job.phase().equals("SUCCESS"));
        job.data.addProperty("rollback", true);
        job.data.addProperty("phase", "LOAD_APPLY");
        job.data.addProperty("cursor", 0);
        job.data.addProperty("gated", true);
        job.runtimeReset();
        job.save();
        active = job;
    }

    private Job find(UUID id) {
        return jobs.stream().filter(j -> j.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown structure job " + id));
    }

    public void flush() {
        if (loadError != null)
            return;
        try {
            storage.save();
            actors.dirty = false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void tick() {
        if (loadError != null)
            return;
        // A service belongs to one live catalogue. Superseded instances must not race recovery.
        if (SERVICES.get(level) != this)
            return;
        long now = level.getGameTime();
        try {
            if (actors.dirty && now >= flushAt) {
                flush();
                flushAt = now + 40;
            }
            if (active == null || active.terminal())
                active = jobs.stream().filter(j -> !j.terminal()).findFirst().orElse(null);
            if (jobs.stream().anyMatch(j -> j.phase().equals("NEEDS_REVIEW")))
                return;
            if (active != null) {
                active.tick();
                return;
            }
            if (now < nextAutomatic)
                return;
            nextAutomatic = now + 200;
            for (String id : landmarkIds()) {
                if (!ReverieDreams.config().gensokyoStructureAutoUpgrade && !preparing.containsKey(id)) {
                    var existing = generation.preparedBlueprint(Identifier.parse(id));
                    if (existing.isEmpty())
                        continue;
                    preparing.put(id, existing.get());
                }
                var future = prepare(id);
                if (!future.isDone() || future.isCompletedExceptionally())
                    continue;
                StructureBlueprint target = future.join();
                if (!archived.contains(target.hash())) {
                    var saved = archive(target);
                    if (!saved.isDone())
                        continue;
                    saved.join();
                    archived.add(target.hash());
                }
                if (!storage.installations().has(id) && generation.freshRevision(Identifier.parse(id))) {
                    install(target, 0);
                    actors.adopt(target);
                    flush();
                }
                if (!ReverieDreams.config().gensokyoStructureAutoUpgrade || ReverieDreams.config().gensokyoStructureUpdateExcluded.contains(id))
                    continue;
                JsonObject entry = storage.installations().has(id) ? storage.installations().getAsJsonObject(id) : null;
                if (entry == null || entry.get("version").getAsInt() < target.version()) {
                    // A blocked automatic job needs explicit operator review, not one retry per tick.
                    if (jobs.stream().anyMatch(j -> j.landmark().equals(id) && j.phase().startsWith("BLOCKED")))
                        continue;
                    enqueue(id, false, true);
                    break;
                }
            }
        } catch (Exception e) {
            if (active != null)
                active.failed(e);
            else
                ReverieDreams.LOGGER.error("Structure version bookkeeping failed", e);
        }
    }

    private void install(StructureBlueprint blueprint, int conflicts) throws IOException {
        JsonObject entry = new JsonObject();
        entry.addProperty("version", blueprint.version());
        entry.addProperty("hash", blueprint.hash());
        entry.addProperty("partial", conflicts);
        storage.installations().add(blueprint.id(), entry);
        storage.save();
    }

    private void stamp(StructureBlueprint blueprint, String id, List<Long> chunks) {
        for (long key : chunks) {
            var chunk = level.getChunkSource().getChunkNow((int) key, (int) (key >> 32));
            if (chunk == null)
                continue;
            var markers = cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoStructureChunkState.markers(chunk);
            markers.removeIf(s -> s.startsWith("revision-v1:" + id + ":"));
            if (blueprint != null)
                markers.add("revision-v1:" + id + ":" + blueprint.version() + ":" + blueprint.hash());
            chunk.markUnsaved();
        }
    }

    private final class Job {
        final JsonObject data;
        StructureBlueprint target, baseline;
        List<Long> keys, halo;
        final Map<Long, CompletableFuture<?>> loads = new LinkedHashMap<>();
        final Set<Long> tickets = new HashSet<>();
        CompletableFuture<?> io;
        IoAction completion;
        CompletableFuture<StructureBlueprint> baselineRead;
        CompletableFuture<Recovery> recovery;
        StructureDelta.Builder builder;
        StructureDelta delta;
        int verifyCursor, verifyOffset;
        CompoundTag actorBackup;
        CompoundTag rollbackLive;
        int offset;
        long waitStart = -1;

        Job(JsonObject data) {
            this.data = data;
            if (data.get("schema").getAsInt() != 1)
                throw new IllegalArgumentException("Unknown job schema");
            // Disk chunks and journal receipts are not a shared transaction. Revisit every delta
            // after restart; each write accepts only before/after states and is idempotent.
            if (Set.of("APPLY", "ACTORS", "VERIFY", "COMMIT", "ROLLBACK", "RESTORE_ACTORS").contains(phase())) {
                data.addProperty("cursor", 0);
                data.addProperty("phase", "LOAD_APPLY");
            }
        }

        UUID id() {
            return UUID.fromString(data.get("id").getAsString());
        }

        String landmark() {
            return data.get("landmark").getAsString();
        }

        String phase() {
            return data.get("phase").getAsString();
        }

        boolean gated() {
            return data.get("gated").getAsBoolean();
        }

        boolean mutated() {
            return data.has("mutated") && data.get("mutated").getAsBoolean();
        }

        boolean rollback() {
            return data.has("rollback") && data.get("rollback").getAsBoolean();
        }

        boolean terminal() {
            return Set.of("SUCCESS", "UP_TO_DATE", "CANCELLED", "ROLLED_BACK", "BLOCKED", "NEEDS_REVIEW").contains(phase());
        }

        Path path() {
            return storage.job(id());
        }

        int cursor() {
            return data.get("cursor").getAsInt();
        }

        void message(String value) {
            data.addProperty("message", value);
        }

        String describe() {
            return id() + " " + landmark() + " " + phase() + " chunk=" + cursor() + " " + data.get("message").getAsString();
        }

        void save() throws IOException {
            StructureUpdateStorage.writeJson(path().resolve("job.json"), data);
        }

        void phase(String value) throws IOException {
            data.addProperty("phase", value);
            waitStart = -1;
            save();
        }

        void runtimeReset() {
            target = null;
            baseline = null;
            rollbackLive = null;
            keys = null;
            halo = null;
            loads.clear();
            builder = null;
            delta = null;
            offset = 0;
            verifyCursor = 0;
            verifyOffset = 0;
            io = null;
            completion = null;
            baselineRead = null;
            recovery = null;
            waitStart = -1;
        }

        void async(IoAction work, IoAction after) {
            io = StructureUpdateService.this.io(work);
            completion = after;
        }

        BoundingBox bounds() {
            var b = data.getAsJsonArray("bounds");
            return new BoundingBox(b.get(0).getAsInt(), b.get(1).getAsInt(), b.get(2).getAsInt(), b.get(3).getAsInt(), b.get(4).getAsInt(), b.get(5).getAsInt());
        }

        boolean playersNearby() {
            if (!data.has("bounds"))
                return false;
            BoundingBox b = bounds();
            for (ServerPlayer p : level.players()) {
                if (!p.isAlive())
                    continue;
                int distance = Math.max(128, (level.getServer().getPlayerList().getViewDistance() + 2) * 16);
                if (p.getX() >= b.minX() - distance && p.getX() <= b.maxX() + distance && p.getZ() >= b.minZ() - distance && p.getZ() <= b.maxZ() + distance)
                    return true;
            }
            return false;
        }

        void release() {
            for (long key : tickets)
                level.getChunkSource().removeTicketWithRadius(StructureUpdateTickets.UPDATE, new ChunkPos((int) key, (int) (key >> 32)), 0);
            tickets.clear();
            loads.clear();
        }

        void finish(String state, String reason) throws IOException {
            if (actorBackup != null) {
                actors.frozen(rollbackLive == null ? actorBackup : rollbackLive, false);
                flush();
                level.save(null, true, false);
                actors.acknowledgeThawed();
            }
            flush();
            release();
            data.addProperty("gated", false);
            message(reason);
            phase(state);
            StructureUpdateStorage.writeJson(path().resolve("gate.json"), data);
            StructureUpdateGate.clear(level.getServer(), id());
            if (halo != null)
                for (long key : halo) {
                    var c = level.getChunkSource().getChunkNow((int) key, (int) (key >> 32));
                    if (c != null)
                        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(c.getPos(), false))
                            player.connection.chunkSender.markChunkPendingToSend(c);
                }
        }

        boolean loadChunks() {
            int requested = 0;
            for (long key : halo)
                if (!loads.containsKey(key) && requested++ < 4) {
                    ChunkPos c = new ChunkPos((int) key, (int) (key >> 32));
                    tickets.add(key);
                    loads.put(key, level.getChunkSource().addTicketAndLoadWithRadius(StructureUpdateTickets.UPDATE, c, 0));
                }
            for (var future : loads.values())
                if (future.isCompletedExceptionally())
                    future.join();
            return loads.size() == halo.size() && loads.values().stream().allMatch(CompletableFuture::isDone) && halo.stream().allMatch(k -> level.getChunkSource().getChunkNow((int) (long) k, (int) (k >> 32)) != null && level.areEntitiesLoaded(k));
        }

        boolean recoverBlueprints() throws IOException {
            if (target != null)
                return true;
            if (!Files.isRegularFile(path().resolve("target.nbt"))) {
                if (!phase().equals("PREPARE"))
                    throw new IOException("Missing immutable job blueprint");
                return true;
            }
            String old = data.has("old_hash") ? data.get("old_hash").getAsString() : "";
            if (recovery == null)
                recovery = CompletableFuture.supplyAsync(() -> {
                    try {
                        return new Recovery(StructureBlueprint.read(path().resolve("target.nbt"), level), old.isBlank() ? null : StructureBlueprint.read(storage.baseline(old), level), Files.isRegularFile(path().resolve("actors.nbt")) ? StructureActors.read(path().resolve("actors.nbt")) : null, Files.isRegularFile(path().resolve("rollback_live.nbt")) ? StructureActors.read(path().resolve("rollback_live.nbt")) : null);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, Util.ioPool());
            if (!recovery.isDone())
                return false;
            Recovery saved = recovery.join();
            target = saved.target;
            baseline = saved.baseline;
            actorBackup = saved.actors;
            rollbackLive = saved.rollbackLive;
            if (!target.id().equals(landmark()) || !target.hash().equals(data.get("target_hash").getAsString()) || baseline != null && (!baseline.id().equals(landmark()) || !baseline.hash().equals(old)))
                throw new IOException("Job blueprint identity/hash mismatch");
            setupRanges();
            return true;
        }

        void setupRanges() {
            TreeSet<Long> chunks = new TreeSet<>(target.chunks().keySet());
            TreeSet<Long> neighborhood = new TreeSet<>(target.neighborhood());
            if (baseline != null) {
                chunks.addAll(baseline.chunks().keySet());
                neighborhood.addAll(baseline.neighborhood());
            }
            keys = List.copyOf(chunks);
            halo = List.copyOf(neighborhood);
            if (halo.size() > Math.max(1, ReverieDreams.config().gensokyoStructureUpdateMaxChunks))
                throw new IllegalStateException("Landmark exceeds configured chunk budget: " + halo.size());
            BoundingBox b = target.bounds();
            if (baseline != null)
                b = new BoundingBox(Math.min(b.minX(), baseline.bounds().minX()), Math.min(b.minY(), baseline.bounds().minY()), Math.min(b.minZ(), baseline.bounds().minZ()), Math.max(b.maxX(), baseline.bounds().maxX()), Math.max(b.maxY(), baseline.bounds().maxY()), Math.max(b.maxZ(), baseline.bounds().maxZ()));
            for (var other : generation.bounds().entrySet())
                if (!other.getKey().toString().equals(landmark()) && b.intersects(other.getValue()))
                    throw new IllegalStateException("Overlapping landmarks require a joint migration adapter: " + other.getKey());
            JsonArray array = new JsonArray();
            for (int v : new int[]{b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ()})
                array.add(v);
            data.add("bounds", array);
        }

        void tick() throws Exception {
            if (io != null) {
                if (!io.isDone())
                    return;
                io.join();
                io = null;
                IoAction after = completion;
                completion = null;
                if (after != null)
                    after.run();
                return;
            }
            if (data.has("cancel_requested") && data.get("cancel_requested").getAsBoolean()) {
                data.remove("cancel_requested");
                cancel(id());
                return;
            }
            if (!recoverBlueprints())
                return;
            if (gated() && data.has("bounds"))
                StructureUpdateGate.set(level.getServer(), id(), bounds());
            long budget = System.nanoTime() + Math.clamp(ReverieDreams.config().gensokyoStructureUpdateBudgetMs, 1, 20) * 1_000_000L;
            switch (phase()) {
                case "PREPARE" -> {
                    var future = prepare(landmark());
                    if (!future.isDone())
                        return;
                    target = future.join();
                    JsonObject installed = storage.installations().has(landmark()) ? storage.installations().getAsJsonObject(landmark()) : null;
                    if (installed != null) {
                        int version = installed.get("version").getAsInt();
                        String hash = installed.get("hash").getAsString();
                        if (version > target.version())
                            throw new IllegalStateException("Refusing a structure downgrade");
                        if (version == target.version() && !hash.equals(target.hash()))
                            throw new IllegalStateException("Content changed without incrementing content_version");
                        if (hash.equals(target.hash()) && data.get("mode").getAsString().equals("update")) {
                            finish("UP_TO_DATE", "Same content version/hash; no world changes");
                            return;
                        }
                        if (!Files.isRegularFile(storage.baseline(hash)))
                            throw new IllegalStateException("Missing portable baseline " + hash);
                        if (baseline == null) {
                            if (baselineRead == null)
                                baselineRead = CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return StructureBlueprint.read(storage.baseline(hash), level);
                                    } catch (IOException e) {
                                        throw new CompletionException(e);
                                    }
                                }, Util.ioPool());
                            if (!baselineRead.isDone())
                                return;
                            baseline = baselineRead.join();
                        }
                        data.addProperty("old_hash", hash);
                    }
                    setupRanges();
                    data.addProperty("target_hash", target.hash());
                    data.addProperty("cursor", 0);
                    save();
                    StructureBlueprint snapshot = target;
                    io = CompletableFuture.allOf(StructureUpdateService.this.io(() -> StructureUpdateStorage.writeNbt(path().resolve("target.nbt"), snapshot.encode())), archive(snapshot));
                    completion = () -> phase("LOAD_BACKUP");
                }
                case "LOAD_BACKUP" -> {
                    if (playersNearby()) {
                        message("Waiting for players to leave the landmark and its visible chunks");
                        return;
                    }
                    if (!loadChunks())
                        return;
                    StructureUpdateProtection.check(level, landmark(), bounds());
                    long reserve = 64L * 1024 * 1024 + keys.size() * 1024L * 1024;
                    if (Files.getFileStore(storage.root).getUsableSpace() < reserve)
                        throw new IOException("Insufficient free space for a recoverable structural backup (reserve=" + reserve + ")");
                    if (baseline == null && data.get("mode").getAsString().equals("update") && generation.hasLegacyBlocks(Identifier.parse(landmark())) && !generation.freshRevision(Identifier.parse(landmark())))
                        throw new IllegalStateException("LEGACY_UNKNOWN: use explicit rebuild; automatic inference of a historical baseline is disabled");
                    if (baseline == null && generation.hasLegacyBlocks(Identifier.parse(landmark())) && !generation.freshRevision(Identifier.parse(landmark())))
                        actors.adoptLegacy(target);
                    // Back up live dynamic NPC state before setting transient freeze flags.
                    actorBackup = actors.capture(baseline, target);
                    CompoundTag snapshot = actorBackup;
                    async(() -> StructureUpdateStorage.writeNbt(path().resolve("actors.nbt"), snapshot), () -> {
                        data.addProperty("gated", true);
                        save();
                        StructureUpdateStorage.writeJson(path().resolve("gate.json"), data);
                        StructureUpdateGate.set(level.getServer(), id(), bounds());
                        actors.frozen(actorBackup, true);
                        flush();
                        phase("BACKUP");
                    });
                }
                case "BACKUP" -> {
                    if (playersNearby()) {
                        message("Waiting for players before snapshot");
                        return;
                    }
                    if (cursor() >= keys.size()) {
                        release();
                        phase("WAIT_UNLOAD");
                        return;
                    }
                    long key = keys.get(cursor());
                    if (builder == null)
                        builder = new StructureDelta.Builder(key, baseline == null ? null : baseline.chunks().get(key), target.chunks().get(key), data.get("mode").getAsString().equals("rebuild"));
                    if (!builder.step(level, budget))
                        return;
                    StructureDelta snapshot = builder.result();
                    async(() -> StructureUpdateStorage.writeNbt(path().resolve("chunks").resolve(key + ".nbt"), snapshot.encode()), () -> {
                        data.addProperty("conflicts", (data.has("conflicts") ? data.get("conflicts").getAsInt() : 0) + snapshot.conflicts);
                        data.addProperty("cursor", cursor() + 1);
                        builder = null;
                        save();
                    });
                }
                case "WAIT_UNLOAD" -> {
                    if (waitStart < 0)
                        waitStart = level.getGameTime();
                    if (halo.stream().anyMatch(k -> level.getChunkSource().getChunkNow((int) (long) k, (int) (k >> 32)) != null || level.areEntitiesLoaded(k))) {
                        if (level.getGameTime() - waitStart > Math.max(100, ReverieDreams.config().gensokyoStructureUnloadTimeoutTicks))
                            throw new IllegalStateException("Unload blocked by player/foreign tickets; no foreign tickets were removed");
                        return;
                    }
                    data.addProperty("cursor", 0);
                    phase("LOAD_APPLY");
                }
                case "LOAD_APPLY" -> {
                    if (playersNearby()) {
                        message("Waiting for players before structural writes");
                        return;
                    }
                    if (!loadChunks())
                        return;
                    if (rollback() && data.has("fresh_rollback") && data.get("fresh_rollback").getAsBoolean() && rollbackLive == null) {
                        rollbackLive = actors.capture(null, target);
                        CompoundTag snapshot = rollbackLive;
                        async(() -> StructureUpdateStorage.writeNbt(path().resolve("rollback_live.nbt"), snapshot), () -> {});
                        return;
                    }
                    actors.frozen(rollbackLive == null ? actorBackup : rollbackLive, true);
                    phase(rollback() ? "ROLLBACK" : "APPLY");
                }
                case "APPLY", "ROLLBACK" -> {
                    if (!loadChunks())
                        return;
                    if (cursor() >= keys.size()) {
                        phase(rollback() ? "RESTORE_ACTORS" : "ACTORS");
                        return;
                    }
                    long key = keys.get(cursor());
                    if (delta == null)
                        delta = StructureDelta.read(path().resolve("chunks").resolve(key + ".nbt"), level);
                    if (!mutated()) {
                        data.addProperty("mutated", true);
                        save();
                    } // durable intent before the first world write
                    while (offset < delta.changes.size() && System.nanoTime() < budget) {
                        delta.apply(level, offset++, rollback());
                    }
                    if (offset >= delta.changes.size()) {
                        data.addProperty("cursor", cursor() + 1);
                        save();
                        delta = null;
                        offset = 0;
                    }
                }
                case "ACTORS" -> {
                    actors.reconcile(baseline, target);
                    actors.reconcilePoints(actorBackup, baseline, target);
                    flush();
                    phase("VERIFY");
                }
                case "VERIFY" -> {
                    if (verifyCursor < keys.size()) {
                        long key = keys.get(verifyCursor);
                        if (delta == null)
                            delta = StructureDelta.read(path().resolve("chunks").resolve(key + ".nbt"), level);
                        while (verifyOffset < delta.changes.size() && System.nanoTime() < budget) {
                            var c = delta.changes.get(verifyOffset++);
                            if (!level.getBlockState(StructureBlueprint.position(key, c.position())).equals(c.after()))
                                throw new IllegalStateException("Post-write verification failed at " + StructureBlueprint.position(key, c.position()));
                        }
                        if (verifyOffset == delta.changes.size()) {
                            verifyCursor++;
                            verifyOffset = 0;
                            delta = null;
                        }
                        return;
                    }
                    for (var a : target.actors()) {
                        Entity e = level.getEntity(a.uuid());
                        if (e == null)
                            e = level.getEntity(a.legacyUuid());
                        if (e instanceof net.minecraft.world.entity.decoration.BlockAttachedEntity attached && !attached.survives())
                            throw new IllegalStateException("Attachment verification failed: " + a.id());
                    }
                    phase("COMMIT");
                }
                case "COMMIT" -> {
                    int conflicts = data.has("conflicts") ? data.get("conflicts").getAsInt() : 0;
                    generation.markUpdated(Identifier.parse(landmark()), target);
                    stamp(target, landmark(), keys);
                    // Flush actual chunk/entity storage before publishing the installed version.
                    // This one maintenance barrier may take longer than the per-tick edit budget.
                    level.save(null, true, false);
                    install(target, conflicts);
                    flush();
                    data.addProperty("lod_cursor", 0);
                    phase("PUBLISH_LOD");
                }
                case "PUBLISH_LOD" -> {
                    if (!loadChunks())
                        return;
                    int cursor = data.get("lod_cursor").getAsInt();
                    for (int n = 0; n < 2 && cursor < keys.size(); n++, cursor++) {
                        long key = keys.get(cursor);
                        String result = StructureUpdateLod.submit(level, level.getChunkSource().getChunkNow((int) key, (int) (key >> 32)));
                        data.addProperty("lod", result);
                        if (result.startsWith("FAILED") || result.startsWith("UNAVAILABLE"))
                            data.addProperty("lod_warning", result);
                    }
                    data.addProperty("lod_cursor", cursor);
                    save();
                    if (cursor == keys.size())
                        finish("SUCCESS", "Structure committed; retained player edits=" + data.get("conflicts") + "; LOD=" + data.get("lod") + ". Adventure rewards and NPC inventories were not reset.");
                }
                case "RESTORE_ACTORS" -> {
                    actors.restore(actorBackup, target);
                    if (baseline != null)
                        install(baseline, 0);
                    else {
                        storage.installations().remove(landmark());
                        flush();
                    }
                    generation.markUpdated(Identifier.parse(landmark()), baseline);
                    stamp(baseline, landmark(), keys);
                    level.save(null, true, false);
                    flush();
                    for (long key : keys)
                        StructureUpdateLod.submit(level, level.getChunkSource().getChunkNow((int) key, (int) (key >> 32)));
                    finish("ROLLED_BACK", "Structural changes restored; current player/container inventory was not rolled back");
                }
                default -> {
                }
            }
        }

        void failed(Exception error) {
            ReverieDreams.LOGGER.error("Structure job {} failed in {}", id(), phase(), error);
            try {
                if (mutated()) {
                    // Keep the publication gate until a verified rollback/operator recovery completes.
                    message(error.toString());
                    data.addProperty("phase", "NEEDS_REVIEW");
                    data.addProperty("gated", true);
                    save();
                } else {
                    if (actorBackup != null)
                        actors.frozen(actorBackup, false);
                    finish("BLOCKED", error.toString());
                }
            } catch (Exception secondary) {
                ReverieDreams.LOGGER.error("Could not persist structure job failure", secondary);
            }
        }
    }

    private record Recovery(StructureBlueprint target, StructureBlueprint baseline, CompoundTag actors,
                            CompoundTag rollbackLive) {
    }
}
