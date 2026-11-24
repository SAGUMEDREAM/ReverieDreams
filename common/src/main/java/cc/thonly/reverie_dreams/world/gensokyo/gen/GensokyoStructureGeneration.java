package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.mixin.accessor.StructureTemplateAccessor;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Fixed authored blocks finish in FEATURES, before lighting/FULL/client publication.
 */
public final class GensokyoStructureGeneration implements cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateTargets {
    private final ServerLevel level;
    private final List<Plan> plans = new ArrayList<>();
    private final GensokyoMapData mapData;
    private cc.thonly.reverie_dreams.world.gensokyo.adventure.ScarletAdventure adventure;
    private cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService updates;

    public cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService updates() {
        if (updates == null)
            updates = new cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService(level, this);
        return updates;
    }

    public CompletableFuture<cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint> blueprint(Identifier id) {
        Plan plan = plans.stream().filter(p -> p.anchor.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown structure landmark: " + id));
        return plan.prepare().thenApply(plan::blueprint);
    }

    public Optional<CompletableFuture<cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint>> preparedBlueprint(Identifier id) {
        Plan plan = plans.stream().filter(p -> p.anchor.id().equals(id)).findFirst().orElseThrow();
        return plan.prepared == null ? Optional.empty() : Optional.of(plan.prepared.thenApply(plan::blueprint));
    }

    public boolean hasLegacyBlocks(Identifier id) {
        Plan plan = plans.stream().filter(p -> p.anchor.id().equals(id)).findFirst().orElseThrow();
        for (int x = plan.bounds.minX() >> 4; x <= plan.bounds.maxX() >> 4; x++)
            for (int z = plan.bounds.minZ() >> 4; z <= plan.bounds.maxZ() >> 4; z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk != null && GensokyoStructureChunkState.has(chunk, plan.blocksKey()))
                    return true;
            }
        return false;
    }

    public boolean freshRevision(Identifier id) {
        Plan plan = plans.stream().filter(p -> p.anchor.id().equals(id)).findFirst().orElseThrow();
        if (plan.prepared == null || !plan.prepared.isDone() || plan.prepared.isCompletedExceptionally())
            return false;
        for (long key : plan.prepared.join().keySet()) {
            var chunk = level.getChunkSource().getChunkNow((int) key, (int) (key >> 32));
            if (chunk == null || !GensokyoStructureChunkState.has(chunk, plan.revision.marker(id.toString())))
                return false;
        }
        return true;
    }

    public void markUpdated(Identifier id, cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint revision) {
        Plan plan = plans.stream().filter(p -> p.anchor.id().equals(id)).findFirst().orElseThrow();
        Set<Long> affected = new HashSet<>();
        if (revision != null)
            affected.addAll(revision.chunks().keySet());
        else
            for (int x = plan.bounds.minX() >> 4; x <= plan.bounds.maxX() >> 4; x++)
                for (int z = plan.bounds.minZ() >> 4; z <= plan.bounds.maxZ() >> 4; z++)
                    affected.add(chunkKey(x, z));
        for (long key : affected) {
            var c = level.getChunkSource().getChunkNow((int) key, (int) (key >> 32));
            if (c != null) {
                if (revision == null) {
                    GensokyoStructureChunkState.markers(c).remove(plan.blocksKey());
                    GensokyoStructureChunkState.markers(c).remove(plan.entitiesKey());
                    c.markUnsaved();
                } else {
                    GensokyoStructureChunkState.mark(c, plan.blocksKey());
                    GensokyoStructureChunkState.mark(c, plan.entitiesKey());
                }
            }
        }
    }

    public void tickAdventure() {
        if (adventure == null)
            adventure = new cc.thonly.reverie_dreams.world.gensokyo.adventure.ScarletAdventure(level, mapData);
        if (!updates().paused("reverie_dreams:scarlet_devil_mansion"))
            adventure.tick();
    }

    public void adventureDeath(net.minecraft.world.entity.LivingEntity entity) {
        if (adventure != null)
            adventure.death(entity);
    }

    public boolean readyForAdventure(BlockPos pos, boolean needsNeighborSupport) {
        int chunkX = pos.getX() >> 4, chunkZ = pos.getZ() >> 4;
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null || !level.areEntitiesLoaded(chunk.getPos().pack()))
            return false;
        for (Plan plan : plans) {
            if (plan.bounds.isInside(pos) && GensokyoStructureChunkState.has(chunk, plan.blocksKey()))
                return !needsNeighborSupport || neighborsReady(plan, chunkX, chunkZ);
        }
        return false;
    }

    public GensokyoStructureGeneration(ServerLevel level, GensokyoMapData map) {
        this.level = level;
        this.mapData = map;
        for (var anchor : map.anchors())
            if ("template".equalsIgnoreCase(anchor.type()) && anchor.template() != null)
                plans.add(new Plan(anchor));
    }

    public boolean reserved(BlockPos pos) {
        for (Plan p : plans)
            if (p.bounds.isInside(pos))
                return true;
        return false;
    }

    public void generate(ChunkAccess chunk) {
        for (Plan plan : plans) {
            if (!plan.intersects(chunk.getPos()) || GensokyoStructureChunkState.has(chunk, plan.blocksKey()))
                continue;
            Piece piece = plan.prepare().join().get(chunk.getPos().pack());
            if (piece != null)
                piece.write(chunk, false);
            GensokyoStructureChunkState.mark(chunk, plan.blocksKey());
            GensokyoStructureChunkState.mark(chunk, plan.revision.marker(plan.anchor.id().toString()));
        }
    }

    /**
     * Main-thread decoration pass. Never loads a chunk, and never writes its structural blocks.
     */
    public boolean finishReady(long deadline) {
        boolean complete = true;
        for (Plan plan : plans) {
            if (updates().paused(plan.anchor.id().toString())) {
                complete = false;
                continue;
            }
            boolean planComplete = true;
            for (int cx = plan.bounds.minX() >> 4; cx <= plan.bounds.maxX() >> 4; cx++)
                for (int cz = plan.bounds.minZ() >> 4; cz <= plan.bounds.maxZ() >> 4; cz++) {
                    if (System.nanoTime() >= deadline)
                        return false;
                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null || !GensokyoStructureChunkState.has(chunk, plan.blocksKey())) {
                        planComplete = false;
                        continue;
                    }
                    if (GensokyoStructureChunkState.has(chunk, plan.entitiesKey()))
                        continue;
                    if (!neighborsReady(plan, cx, cz)) {
                        planComplete = false;
                        continue;
                    }
                    var future = plan.prepare();
                    if (!future.isDone()) {
                        planComplete = false;
                        continue;
                    }
                    Piece piece = future.join().get(chunk.getPos().pack());
                    if (piece == null || piece.spawnEntities(level, plan))
                        GensokyoStructureChunkState.mark(chunk, plan.entitiesKey());
                    else
                        planComplete = false;
                }
            if (planComplete)
                level.getDataStorage().computeIfAbsent(cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapInstallState.TYPE).markInstalled(plan.anchor.id().toString());
            complete &= planComplete;
        }
        return complete;
    }

    private boolean neighborsReady(Plan plan, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk neighbor = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
                if (neighbor == null || !level.areEntitiesLoaded(neighbor.getPos().pack()))
                    return false;
                for (Plan other : plans)
                    if (other.intersects(neighbor.getPos()) && !GensokyoStructureChunkState.has(neighbor, other.blocksKey()))
                        return false;
            }
        return true;
    }

    /**
     * Explicit repair of loaded legacy chunks. Normal loading never repaints an edited old building.
     */
    public void repair(Identifier anchorId) {
        try {
            updates().enqueue(anchorId.toString(), true, false);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    public List<Identifier> anchors() {
        return plans.stream().map(p -> p.anchor.id()).toList();
    }

    public Map<Identifier, BoundingBox> bounds() {
        Map<Identifier, BoundingBox> result = new HashMap<>();
        for (Plan p : plans)
            result.put(p.anchor.id(), p.bounds);
        return Map.copyOf(result);
    }

    public void reloadCatalogue() {
        plans.clear();
        for (var anchor : mapData.anchors())
            if ("template".equalsIgnoreCase(anchor.type()) && anchor.template() != null)
                plans.add(new Plan(anchor));
        adventure = null;
    }

    private final class Plan {
        final GensokyoMapData.Anchor anchor;
        final Mirror mirror;
        final Rotation rotation;
        final BoundingBox bounds;
        final String key;
        final cc.thonly.reverie_dreams.world.gensokyo.update.StructureRevision revision;
        private volatile CompletableFuture<Map<Long, Piece>> prepared;

        Plan(GensokyoMapData.Anchor anchor) {
            this.anchor = anchor;
            mirror = parseMirror(anchor.mirror());
            rotation = parseRotation(anchor.rotation());
            int[] size = metadataSize(anchor.template());
            if (size == null) {
                var template = level.getStructureManager().get(anchor.template()).orElseThrow(() -> new IllegalStateException("Missing fixed template " + anchor.template()));
                size = new int[]{template.getSize().getX(), template.getSize().getY(), template.getSize().getZ()};
            }
            float scale = Math.max(.25f, Math.min(4f, anchor.scale()));
            double effective = Math.abs(scale - 1) < 1e-5 ? 1 : Math.min(scale, Math.max(.1, Math.cbrt(1_500_000.0 / ((long) size[0] * size[1] * size[2]))));
            for (int k = 0; k < 3; k++)
                size[k] = Math.max(1, (int) Math.round(size[k] * effective));
            int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
            for (int x : new int[]{0, size[0] - 1})
                for (int y : new int[]{0, size[1] - 1})
                    for (int z : new int[]{0, size[2] - 1}) {
                        BlockPos p = transform(new BlockPos(x, y, z));
                        x0 = Math.min(x0, p.getX());
                        x1 = Math.max(x1, p.getX());
                        y0 = Math.min(y0, p.getY());
                        y1 = Math.max(y1, p.getY());
                        z0 = Math.min(z0, p.getZ());
                        z1 = Math.max(z1, p.getZ());
                    }
            bounds = new BoundingBox(x0, y0, z0, x1, y1, z1);
            key = "fixed-v2:" + UUID.nameUUIDFromBytes((anchor.id() + "|" + anchor.template() + "|" + anchor.pos() + "|" + rotation + "|" + mirror + "|" + scale + "|" + anchor.integrity()).getBytes(StandardCharsets.UTF_8));
            var sourceRevision = cc.thonly.reverie_dreams.world.gensokyo.update.StructureRevision.load(level, anchor.template());
            String hash = com.google.common.hash.Hashing.sha256().hashString(sourceRevision.hash() + "|" + key, StandardCharsets.UTF_8).toString();
            revision = new cc.thonly.reverie_dreams.world.gensokyo.update.StructureRevision(sourceRevision.version(), hash, sourceRevision.metadata());
        }

        cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint blueprint(Map<Long, Piece> pieces) {
            Map<Long, cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint.Slice> chunks = new LinkedHashMap<>();
            List<cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint.Actor> actors = new ArrayList<>();
            for (var entry : pieces.entrySet()) {
                Piece p = entry.getValue();
                chunks.put(entry.getKey(), new cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint.Slice(p.positions, p.states, p.blockEntities, p.terrainRepairs));
                for (Decoration d : p.entities) {
                    CompoundTag tag = d.tag.copy();
                    tag.putString("reverie_structure_rotation", rotation.name());
                    tag.putString("reverie_structure_mirror", mirror.name());
                    actors.add(new cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint.Actor(d.id, d.kind, d.pos, tag, d.uuid, d.legacyUuid));
                }
            }
            Map<String, BlockPos> points = new HashMap<>(cc.thonly.reverie_dreams.world.gensokyo.adventure.ScarletAdventure.maintenancePoints(level, anchor));
            for (var point : revision.metadata().functionPoints()) {
                points.put(point.id(), transform(point.pos()));
            }
            return new cc.thonly.reverie_dreams.world.gensokyo.update.StructureBlueprint(anchor.id().toString(), revision.version(), revision.hash(), bounds, Map.copyOf(chunks), List.copyOf(actors), Map.copyOf(points));
        }

        BlockPos transform(BlockPos local) {
            return StructureTemplate.transform(local, mirror, rotation, BlockPos.ZERO).offset(anchor.pos());
        }

        Vec3 transform(Vec3 local) {
            return StructureTemplate.transform(local, mirror, rotation, BlockPos.ZERO).add(anchor.pos().getX(), anchor.pos().getY(), anchor.pos().getZ());
        }

        String blocksKey() {
            return key + ":blocks";
        }

        cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService updates() {
            return GensokyoStructureGeneration.this.updates();
        }

        String entitiesKey() {
            return key + ":entities";
        }

        boolean supportsReady(Entity entity) {
            AABB box = entity.getBoundingBox().inflate(1);
            for (int cx = ((int) Math.floor(box.minX)) >> 4; cx <= ((int) Math.floor(box.maxX)) >> 4; cx++)
                for (int cz = ((int) Math.floor(box.minZ)) >> 4; cz <= ((int) Math.floor(box.maxZ)) >> 4; cz++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null)
                        return false;
                    for (Plan other : plans)
                        if (other.intersects(chunk.getPos()) && !GensokyoStructureChunkState.has(chunk, other.blocksKey()))
                            return false;
                }
            return true;
        }

        boolean intersects(ChunkPos c) {
            return c.getMinBlockX() <= bounds.maxX() && c.getMaxBlockX() >= bounds.minX() && c.getMinBlockZ() <= bounds.maxZ() && c.getMaxBlockZ() >= bounds.minZ();
        }

        CompletableFuture<Map<Long, Piece>> prepare() {
            var f = prepared;
            if (f != null)
                return f;
            synchronized (this) {
                if (prepared == null)
                    prepared = CompletableFuture.supplyAsync(this::compile, Util.backgroundExecutor().forName("gensokyo_structure"));
                return prepared;
            }
        }

        Map<Long, Piece> compile() {
            var manager = level.getStructureManager();
            StructureTemplate template = manager.get(anchor.template()).orElseThrow(() -> new IllegalStateException("Missing fixed template " + anchor.template()));
            if (Math.abs(anchor.scale() - 1) > 1e-5)
                template = GensokyoMapInstaller.scaledTemplate(level, template, anchor.scale());
            if (template == null)
                throw new IllegalStateException("Could not scale fixed template " + anchor.template());
            var access = (StructureTemplateAccessor) (Object) template;
            Map<Long, Builder> builders = new HashMap<>();
            if (access.reverie$getPalettes().size() != 1)
                throw new IllegalStateException("Fixed templates require a deterministic single palette");
            var addedChests = cc.thonly.reverie_dreams.world.gensokyo.adventure.ScarletAdventure.newTreasurePositions(level, anchor);
            for (var info : access.reverie$getPalettes().getFirst().blocks()) {
                BlockPos pos = transform(info.pos());
                if (anchor.integrity() < 1 && RandomSource.create(net.minecraft.util.Mth.getSeed(pos) ^ level.getSeed()).nextFloat() > anchor.integrity())
                    continue;
                long chunk = chunkKey(pos.getX() >> 4, pos.getZ() >> 4);
                Builder builder = builders.computeIfAbsent(chunk, k -> new Builder());
                int packed = (pos.getY() << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
                builder.positions.add(packed);
                builder.states.add((addedChests.contains(pos) ? net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState() : info.state()).mirror(mirror).rotate(rotation));
                CompoundTag sourceTag = info.nbt();
                if (addedChests.contains(pos)) {
                    sourceTag = new CompoundTag();
                    sourceTag.putString("id", "minecraft:chest");
                }
                if (sourceTag != null) {
                    CompoundTag tag = sourceTag.copy();
                    tag.putInt("x", pos.getX());
                    tag.putInt("y", pos.getY());
                    tag.putInt("z", pos.getZ());
                    builder.blockEntities.put(packed, tag);
                }
            }
            addExteriorTerrainRepairs(builders);
            int index = 0;
            for (var source : access.reverie$getEntities()) {
                Vec3 pos = transform(source.pos);
                BlockPos blockPos = transform(source.blockPos);
                CompoundTag tag = source.nbt.copy();
                ListTag coordinates = new ListTag();
                coordinates.add(DoubleTag.valueOf(pos.x));
                coordinates.add(DoubleTag.valueOf(pos.y));
                coordinates.add(DoubleTag.valueOf(pos.z));
                tag.put("Pos", coordinates);
                tag.putIntArray("block_pos", new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()});
                tag.putInt("TileX", blockPos.getX());
                tag.putInt("TileY", blockPos.getY());
                tag.putInt("TileZ", blockPos.getZ());
                UUID legacyId = UUID.nameUUIDFromBytes((level.getSeed() + "|" + key + "|entity|" + index++).getBytes(StandardCharsets.UTF_8));
                tag.remove("UUID");
                String actorId = tag.getStringOr("reverie_structure_id", "");
                if (actorId.isBlank())
                    throw new IllegalStateException("Structure entity missing stable ID");
                UUID id = UUID.nameUUIDFromBytes((level.getSeed() + "|" + level.dimension().identifier() + "|" + anchor.id() + "|actor|" + actorId).getBytes(StandardCharsets.UTF_8));
                builders.computeIfAbsent(chunkKey(blockPos.getX() >> 4, blockPos.getZ() >> 4), k -> new Builder()).entities.add(new Decoration(pos, tag, id, legacyId, actorId, tag.getStringOr("reverie_structure_kind", "decoration")));
            }
            Map<Long, Piece> result = new HashMap<>();
            builders.forEach((chunk, b) -> result.put(chunk, new Piece(chunk, b.positions.toIntArray(), b.states.toArray(BlockState[]::new),
                    Map.copyOf(b.blockEntities), List.copyOf(b.entities), Set.copyOf(b.terrainRepairs))));
            // The compact per-chunk representation owns copied NBT; retain no giant template object.
            manager.remove(anchor.template());
            return Map.copyOf(result);
        }

        private void addExteriorTerrainRepairs(Map<Long, Builder> builders) {
            var metadata = revision.metadata();
            if (metadata.exteriorGroundColumns().isEmpty() || Math.abs(anchor.scale() - 1) > 1e-5)
                return;
            int width = metadata.size().getX();
            int y = metadata.exteriorGroundY();
            for (var column : metadata.exteriorGroundColumns()) {
                int index = column;
                BlockPos pos = transform(new BlockPos(index % width, y, index / width));
                int x = pos.getX(), z = pos.getZ();
                if (mapData.heightAt(x, z) != pos.getY())
                    continue;
                BlockState state = mapData.surfaceStateAt(x, z);
                if (state.isAir())
                    continue;
                Builder builder = builders.computeIfAbsent(chunkKey(x >> 4, z >> 4), key -> new Builder());
                int packed = (pos.getY() << 8) | ((z & 15) << 4) | (x & 15);
                builder.positions.add(packed);
                builder.states.add(state);
                builder.terrainRepairs.add(packed);
            }
        }
    }

    private int[] metadataSize(Identifier template) {
        return cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders.GENSOKYO_STRUCTURE_METADATA.get(template)
                .map(entry -> entry.value().size())
                .map(size -> new int[]{size.getX(), size.getY(), size.getZ()})
                .orElse(null);
    }

    private static final class Builder {
        final IntArrayList positions = new IntArrayList();
        final List<BlockState> states = new ArrayList<>();
        final Map<Integer, CompoundTag> blockEntities = new HashMap<>();
        final List<Decoration> entities = new ArrayList<>();
        final Set<Integer> terrainRepairs = new HashSet<>();
    }

    private record Decoration(Vec3 pos, CompoundTag tag, UUID uuid, UUID legacyUuid, String id, String kind) {
    }

    private record Piece(long chunkKey, int[] positions, BlockState[] states, Map<Integer, CompoundTag> blockEntities,
                         List<Decoration> entities, Set<Integer> terrainRepairs) {
        void write(ChunkAccess chunk, boolean repair) {
            ChunkPos cp = chunk.getPos();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < positions.length; i++) {
                int packed = positions[i], y = packed >> 8;
                if (terrainRepairs.contains(packed))
                    continue;
                pos.set(cp.getMinBlockX() + (packed & 15), y, cp.getMinBlockZ() + ((packed >> 4) & 15));
                if (y < chunk.getMinY() || y >= chunk.getMinY() + chunk.getHeight())
                    throw new IllegalStateException("Fixed template outside dimension height");
                BlockState old = chunk.getBlockState(pos), state = states[i];
                CompoundTag nbt = blockEntities.get(packed);
                // Keep existing containers' inventories during an explicit repair.
                if (repair && old.getBlock() == state.getBlock() && old.hasBlockEntity() && chunk.getBlockEntity(pos) != null)
                    continue;
                if (!old.equals(state)) {
                    chunk.removeBlockEntity(pos);
                    if (repair && chunk instanceof LevelChunk full)
                        full.getLevel().setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                    else
                        chunk.setBlockState(pos, state, Block.UPDATE_NONE);
                }
                if (nbt != null) {
                    if (repair && chunk instanceof LevelChunk full) {
                        var entity = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos, state, nbt.copy(), full.getLevel().registryAccess());
                        if (entity == null)
                            throw new IllegalStateException("Invalid authored block entity at " + pos);
                        chunk.setBlockEntity(entity);
                    } else
                        chunk.setBlockEntityNbt(nbt.copy());
                }
            }
            chunk.markUnsaved();
        }

        boolean spawnEntities(ServerLevel level, Plan plan) {
            boolean ready = true;
            for (Decoration data : entities) {
                Entity previous = level.getEntity(data.uuid);
                if (previous == null)
                    previous = level.getEntity(data.legacyUuid);
                if (previous != null) {
                    plan.updates().claim(plan.anchor.id().toString(), data.id, previous);
                    continue;
                }
                if (!plan.updates().maySpawn(plan.anchor.id().toString(), data.id))
                    continue;
                Optional<Entity> value = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data.tag.copy()), level, EntitySpawnReason.STRUCTURE);
                if (value.isEmpty())
                    throw new IllegalStateException("Could not deserialize fixed decoration " + data.tag.getStringOr("id", ""));
                Entity entity = value.get();
                float yaw = entity.rotate(plan.rotation);
                yaw += entity.mirror(plan.mirror) - entity.getYRot();
                // Hanging entities already use the transformed attachment block from NBT.
                // Snapping to the visual center moves multi-block paintings off that support.
                if (!(entity instanceof BlockAttachedEntity))
                    entity.snapTo(data.pos.x, data.pos.y, data.pos.z, yaw, entity.getXRot());
                entity.setYBodyRot(yaw);
                entity.setYHeadRot(yaw);
                entity.setUUID(data.uuid);
                boolean existing = level.getEntities(entity, entity.getBoundingBox().inflate(.02), e -> e.getType() == entity.getType() && e.position().distanceToSqr(entity.position()) < .0001).stream().findAny().isPresent();
                if (existing)
                    continue;
                if (entity instanceof BlockAttachedEntity attached && (!plan.supportsReady(entity) || !attached.survives())) {
                    ready = false;
                    continue;
                }
                if (entity instanceof Mob mob)
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(data.pos)), EntitySpawnReason.STRUCTURE, null);
                level.addFreshEntityWithPassengers(entity);
                plan.updates().claim(plan.anchor.id().toString(), data.id, entity);
            }
            return ready;
        }
    }

    private static Rotation parseRotation(String text) {
        try {
            return "180".equals(text) ? Rotation.CLOCKWISE_180 : Rotation.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Rotation.NONE;
        }
    }

    private static long chunkKey(int x, int z) {
        return (x & 0xffffffffL) | ((z & 0xffffffffL) << 32);
    }

    private static Mirror parseMirror(String text) {
        try {
            return Mirror.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Mirror.NONE;
        }
    }
}
