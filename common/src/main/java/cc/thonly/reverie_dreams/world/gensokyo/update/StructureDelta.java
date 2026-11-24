package cc.thonly.reverie_dreams.world.gensokyo.update;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Write-ahead, compressed changes for one chunk. Containers are never replaced across block types.
 */
public final class StructureDelta {
    public record Change(int position, BlockState before, BlockState after, CompoundTag beforeNbt,
                         CompoundTag afterNbt) {
    }

    public final long chunk;
    public final List<Change> changes;
    public final int conflicts;

    public StructureDelta(long chunk, List<Change> changes, int conflicts) {
        this.chunk = chunk;
        this.changes = List.copyOf(changes);
        this.conflicts = conflicts;
    }

    public static final class Builder {
        private final long chunk;
        private final Map<Integer, BlockState> before = new HashMap<>(), after = new HashMap<>();
        private final Map<Integer, CompoundTag> nbt;
        private final Set<Integer> terrainRepairs;
        private final Iterator<Integer> iterator;
        private final List<Change> changes = new ArrayList<>();
        private final boolean rebuild;
        private int conflicts;

        public Builder(long chunk, StructureBlueprint.Slice baseline, StructureBlueprint.Slice target, boolean rebuild) {
            this.chunk = chunk;
            this.rebuild = rebuild;
            if (baseline != null)
                for (int i = 0; i < baseline.positions().length; i++)
                    before.put(baseline.positions()[i], baseline.states()[i]);
            if (target != null)
                for (int i = 0; i < target.positions().length; i++)
                    after.put(target.positions()[i], target.states()[i]);
            nbt = target == null ? Map.of() : target.blockEntities();
            terrainRepairs = target == null ? Set.of() : target.terrainRepairs();
            TreeSet<Integer> positions = new TreeSet<>(before.keySet());
            if (baseline != null)
                positions.removeIf(pos -> !after.containsKey(pos)
                        && (before.get(pos).isAir() || baseline.terrainRepairs().contains(pos)));
            positions.addAll(after.keySet());
            iterator = positions.iterator();
        }

        public boolean step(ServerLevel level, long deadline) {
            int batch = 0;
            while (iterator.hasNext()) {
                if (++batch % 128 == 0 && System.nanoTime() >= deadline)
                    return false;
                int packed = iterator.next();
                BlockPos pos = StructureBlueprint.position(chunk, packed);
                BlockState current = level.getBlockState(pos), expected = before.get(packed), target = after.getOrDefault(packed, Blocks.AIR.defaultBlockState());
                BlockEntity be = level.getBlockEntity(pos);
                CompoundTag newNbt = nbt.get(packed);
                if (current.equals(target) && (be != null || newNbt == null))
                    continue;
                if (terrainRepairs.contains(packed) && !current.isAir())
                    continue;
                // Never remove a container/machine or silently convert its persisted capabilities.
                if (be != null && current.getBlock() != target.getBlock())
                    throw new IllegalStateException("Protected block entity at " + pos + ": " + BuiltInRegistries.BLOCK.getKey(current.getBlock()));
                if (be != null && !BuiltInRegistries.BLOCK.getKey(current.getBlock()).getNamespace().equals("minecraft") && !current.equals(target))
                    throw new IllegalStateException("Mod block entity needs an explicit migration adapter at " + pos);
                if (!rebuild && expected != null && !current.equals(expected) && !terrainRepairs.contains(packed)) {
                    conflicts++;
                    continue;
                }
                if (!rebuild && expected == null && !natural(current))
                    throw new IllegalStateException("New footprint contains an unowned block at " + pos + ": " + current);
                protectEntities(level, pos, current, target);
                CompoundTag prior = be == null ? null : be.saveWithFullMetadata(level.registryAccess());
                changes.add(new Change(packed, current, target, prior, be == null ? newNbt : null));
            }
            return true;
        }

        public StructureDelta result() {
            return new StructureDelta(chunk, changes, conflicts);
        }
    }

    private static void protectEntities(ServerLevel level, BlockPos pos, BlockState current, BlockState target) {
        if (current.equals(target))
            return;
        var shape = target.getCollisionShape(level, pos);
        boolean canRemoveSupport = !current.isAir();
        if (shape.isEmpty() && !canRemoveSupport)
            return;
        AABB block = new AABB(pos);
        for (Entity e : level.getEntities((Entity) null, canRemoveSupport ? block.inflate(1) : block,
                entity -> entity.isAlive() && !entity.isSpectator()
                        && (entity instanceof LivingEntity || entity instanceof BlockAttachedEntity)
                        && !StructureUpdateGate.freezes(entity))) {
            boolean collision = shape.toAabbs().stream().anyMatch(box -> box.move(pos).intersects(e.getBoundingBox()));
            if (e instanceof LeashFenceKnotEntity knot && pos.equals(knot.getPos()))
                collision = !target.is(BlockTags.FENCES);
            if (collision || (canRemoveSupport && e instanceof BlockAttachedEntity attached && losesSupport(attached, pos, target)))
                throw new IllegalStateException("Protected entity " + BuiltInRegistries.ENTITY_TYPE.getKey(e.getType())
                        + " (" + e.getUUID() + ") at " + e.blockPosition() + " conflicts with structural write at " + pos);
        }
    }

    private static boolean losesSupport(BlockAttachedEntity entity, BlockPos pos, BlockState target) {
        if (entity instanceof LeashFenceKnotEntity knot)
            return pos.equals(knot.getPos()) && !target.is(BlockTags.FENCES);
        if (entity instanceof ItemFrame frame) {
            var direction = frame.getDirection();
            return pos.equals(frame.getPos().relative(direction.getOpposite())) && !target.isSolid()
                    && (!direction.getAxis().isHorizontal() || !DiodeBlock.isDiode(target));
        }
        if (entity instanceof HangingEntity hanging) {
            // Match the support rectangle behind vanilla paintings, including multi-block ones.
            AABB support = hanging.getBoundingBox().move(hanging.getDirection().step().mul(-0.5F)).deflate(1.0E-7);
            return new AABB(pos).intersects(support) && !target.isSolid() && !DiodeBlock.isDiode(target);
        }
        return new AABB(pos).intersects(entity.getBoundingBox().inflate(1));
    }

    private static boolean natural(BlockState state) {
        if (state.isAir())
            return true;
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!id.getNamespace().equals("minecraft"))
            return false;
        String n = id.getPath();
        return Set.of("stone", "dirt", "grass_block", "gravel", "sand", "water", "snow", "snow_block", "andesite", "granite", "diorite", "tuff", "clay", "coarse_dirt", "podzol", "rooted_dirt", "short_grass", "tall_grass", "fern", "large_fern", "vine", "moss_block").contains(n)
                || state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.FLOWERS) || state.is(net.minecraft.tags.BlockTags.REPLACEABLE);
    }

    public void apply(ServerLevel level, int index, boolean rollback) {
        Change c = changes.get(index);
        BlockPos pos = StructureBlueprint.position(chunk, c.position);
        BlockState expected = rollback ? c.after : c.before, target = rollback ? c.before : c.after, current = level.getBlockState(pos);
        if (current.equals(target))
            return; // Idempotent recovery after a chunk saved before its receipt.
        if (!current.equals(expected))
            throw new IllegalStateException("Concurrent edit at " + pos + "; expected " + expected + ", found " + current);
        protectEntities(level, pos, current, target);
        BlockEntity existing = level.getBlockEntity(pos);
        if (existing != null && current.getBlock() != target.getBlock())
            throw new IllegalStateException("Protected live block entity at " + pos);
        CompoundTag saved = existing == null ? null : existing.saveWithFullMetadata(level.registryAccess());
        level.setBlock(pos, target, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
        if (saved != null) {
            // The same object normally remains; restore exact data if a block implementation replaced it.
            if (level.getBlockEntity(pos) != existing) {
                BlockEntity restored = BlockEntity.loadStatic(pos, target, saved, level.registryAccess());
                if (restored == null)
                    throw new IllegalStateException("Cannot restore protected block entity at " + pos);
                level.setBlockEntity(restored);
            }
        } else {
            CompoundTag tag = rollback ? c.beforeNbt : c.afterNbt;
            if (tag != null) {
                BlockEntity created = BlockEntity.loadStatic(pos, target, tag.copy(), level.registryAccess());
                if (created == null)
                    throw new IllegalStateException("Cannot load authored block entity at " + pos);
                level.setBlockEntity(created);
            }
        }
        level.getChunkSource().getLightEngine().checkBlock(pos);
    }

    public CompoundTag encode() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema", 1);
        root.putLong("chunk", chunk);
        root.putInt("conflicts", conflicts);
        ListTag palette = new ListTag();
        Map<BlockState, Integer> ids = new LinkedHashMap<>();
        int[] positions = new int[changes.size()], before = new int[changes.size()], after = new int[changes.size()];
        CompoundTag priorNbt = new CompoundTag(), targetNbt = new CompoundTag();
        for (int i = 0; i < changes.size(); i++) {
            Change c = changes.get(i);
            positions[i] = c.position;
            for (BlockState state : List.of(c.before, c.after))
                if (!ids.containsKey(state)) {
                    ids.put(state, ids.size());
                    palette.add(NbtUtils.writeBlockState(state));
                }
            before[i] = ids.get(c.before);
            after[i] = ids.get(c.after);
            if (c.beforeNbt != null)
                priorNbt.put(Integer.toString(i), c.beforeNbt.copy());
            if (c.afterNbt != null)
                targetNbt.put(Integer.toString(i), c.afterNbt.copy());
        }
        root.put("palette", palette);
        root.putIntArray("positions", positions);
        root.putIntArray("before", before);
        root.putIntArray("after", after);
        root.put("before_nbt", priorNbt);
        root.put("after_nbt", targetNbt);
        return root;
    }

    public static StructureDelta read(Path path, ServerLevel level) throws IOException {
        StructureUpdateStorage.validate(path);
        CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.create(64L * 1024 * 1024));
        if (root.getIntOr("schema", 0) != 1)
            throw new IOException("Unknown delta schema");
        List<BlockState> palette = new ArrayList<>();
        for (Tag t : root.getListOrEmpty("palette"))
            palette.add(StructureBlueprint.readState((CompoundTag) t, level));
        int[] pos = root.getIntArray("positions").orElseThrow(), a = root.getIntArray("before").orElseThrow(), b = root.getIntArray("after").orElseThrow();
        if (pos.length != a.length || pos.length != b.length)
            throw new IOException("Corrupt delta");
        CompoundTag prior = root.getCompoundOrEmpty("before_nbt"), target = root.getCompoundOrEmpty("after_nbt");
        List<Change> changes = new ArrayList<>();
        for (int i = 0; i < pos.length; i++) {
            String key = Integer.toString(i);
            changes.add(new Change(pos[i], palette.get(a[i]), palette.get(b[i]), prior.contains(key) ? prior.getCompoundOrEmpty(key) : null, target.contains(key) ? target.getCompoundOrEmpty(key) : null));
        }
        return new StructureDelta(root.getLongOr("chunk", 0), changes, root.getIntOr("conflicts", 0));
    }
}
