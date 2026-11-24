package cc.thonly.reverie_dreams.world.gensokyo.update;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Immutable compiled structure. Baselines use palette names, never process-local registry numbers.
 */
public record StructureBlueprint(String id, int version, String hash, BoundingBox bounds,
                                 Map<Long, Slice> chunks, List<Actor> actors, Map<String, BlockPos> points) {
    public record Slice(int[] positions, BlockState[] states, Map<Integer, CompoundTag> blockEntities,
                        Set<Integer> terrainRepairs) {
        public Slice(int[] positions, BlockState[] states, Map<Integer, CompoundTag> blockEntities) {
            this(positions, states, blockEntities, Set.of());
        }

        public Slice {
            if (positions.length != states.length)
                throw new IllegalArgumentException("Invalid slice");
        }

        public BlockState stateAt(int packed) {
            for (int i = 0; i < positions.length; i++)
                if (positions[i] == packed)
                    return states[i];
            return null;
        }
    }

    public record Actor(String id, String kind, Vec3 pos, CompoundTag nbt, UUID uuid, UUID legacyUuid) {
    }

    public static BlockPos position(long chunk, int packed) {
        ChunkPos c = new ChunkPos((int) chunk, (int) (chunk >> 32));
        return new BlockPos(c.getMinBlockX() + (packed & 15), packed >> 8, c.getMinBlockZ() + ((packed >> 4) & 15));
    }

    public static int pack(BlockPos pos) {
        return (pos.getY() << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
    }

    public Set<Long> neighborhood() {
        Set<Long> keys = new TreeSet<>();
        for (long key : chunks.keySet()) {
            ChunkPos c = new ChunkPos((int) key, (int) (key >> 32));
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    keys.add(new ChunkPos(c.x() + dx, c.z() + dz).pack());
        }
        return keys;
    }

    public CompoundTag encode() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema", 1);
        root.putString("id", id);
        root.putInt("version", version);
        root.putString("hash", hash);
        root.putIntArray("bounds", new int[]{bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ()});
        ListTag palette = new ListTag();
        Map<BlockState, Integer> index = new LinkedHashMap<>();
        ListTag slices = new ListTag();
        for (long key : new TreeSet<>(chunks.keySet())) {
            Slice s = chunks.get(key);
            int[] ids = new int[s.states.length];
            for (int i = 0; i < ids.length; i++) {
                BlockState state = s.states[i];
                Integer value = index.get(state);
                if (value == null) {
                    value = index.size();
                    index.put(state, value);
                    palette.add(NbtUtils.writeBlockState(state));
                }
                ids[i] = value;
            }
            CompoundTag part = new CompoundTag();
            part.putLong("chunk", key);
            part.putIntArray("positions", s.positions);
            part.putIntArray("states", ids);
            if (!s.terrainRepairs.isEmpty())
                part.putIntArray("terrain_repairs", s.terrainRepairs.stream().mapToInt(Integer::intValue).sorted().toArray());
            CompoundTag bes = new CompoundTag();
            s.blockEntities.forEach((p, tag) -> bes.put(Integer.toString(p), tag.copy()));
            part.put("block_entities", bes);
            slices.add(part);
        }
        root.put("palette", palette);
        root.put("slices", slices);
        ListTag entities = new ListTag();
        for (Actor a : actors) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", a.id);
            tag.putString("kind", a.kind);
            tag.putDouble("x", a.pos.x);
            tag.putDouble("y", a.pos.y);
            tag.putDouble("z", a.pos.z);
            tag.put("nbt", a.nbt.copy());
            tag.putString("uuid", a.uuid.toString());
            tag.putString("legacy", a.legacyUuid.toString());
            entities.add(tag);
        }
        root.put("actors", entities);
        CompoundTag functions = new CompoundTag();
        points.forEach((key, p) -> functions.putLong(key, p.asLong()));
        root.put("points", functions);
        return root;
    }

    public static StructureBlueprint read(Path path, ServerLevel level) throws IOException {
        StructureUpdateStorage.validate(path);
        CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.create(256L * 1024 * 1024));
        if (root.getIntOr("schema", 0) != 1)
            throw new IOException("Unsupported blueprint schema");
        List<BlockState> palette = new ArrayList<>();
        for (Tag t : root.getListOrEmpty("palette"))
            palette.add(readState((CompoundTag) t, level));
        Map<Long, Slice> chunks = new LinkedHashMap<>();
        for (Tag t : root.getListOrEmpty("slices")) {
            CompoundTag part = (CompoundTag) t;
            int[] pos = part.getIntArray("positions").orElseThrow(), ids = part.getIntArray("states").orElseThrow();
            if (pos.length != ids.length)
                throw new IOException("Corrupt blueprint arrays");
            BlockState[] states = new BlockState[ids.length];
            for (int i = 0; i < ids.length; i++)
                states[i] = palette.get(ids[i]);
            Map<Integer, CompoundTag> bes = new HashMap<>();
            CompoundTag be = part.getCompoundOrEmpty("block_entities");
            for (String key : be.keySet())
                bes.put(Integer.parseInt(key), be.getCompoundOrEmpty(key));
            Set<Integer> repairs = new HashSet<>();
            for (int packed : part.getIntArray("terrain_repairs").orElseGet(() -> new int[0]))
                repairs.add(packed);
            chunks.put(part.getLongOr("chunk", 0), new Slice(pos, states, Map.copyOf(bes), Set.copyOf(repairs)));
        }
        List<Actor> actors = new ArrayList<>();
        for (Tag t : root.getListOrEmpty("actors")) {
            CompoundTag a = (CompoundTag) t;
            actors.add(new Actor(a.getStringOr("id", ""), a.getStringOr("kind", "decoration"), new Vec3(a.getDoubleOr("x", 0), a.getDoubleOr("y", 0), a.getDoubleOr("z", 0)), a.getCompoundOrEmpty("nbt"), UUID.fromString(a.getStringOr("uuid", "")), UUID.fromString(a.getStringOr("legacy", ""))));
        }
        int[] b = root.getIntArray("bounds").orElseThrow();
        if (b.length != 6)
            throw new IOException("Corrupt bounds");
        Map<String, BlockPos> points = new HashMap<>();
        CompoundTag p = root.getCompoundOrEmpty("points");
        for (String key : p.keySet())
            points.put(key, BlockPos.of(p.getLongOr(key, 0)));
        return new StructureBlueprint(root.getStringOr("id", ""), root.getIntOr("version", 0), root.getStringOr("hash", ""), new BoundingBox(b[0], b[1], b[2], b[3], b[4], b[5]), Map.copyOf(chunks), List.copyOf(actors), Map.copyOf(points));
    }

    static BlockState readState(CompoundTag tag, ServerLevel level) throws IOException {
        if (!BuiltInRegistries.BLOCK.containsKey(Identifier.parse(tag.getStringOr("Name", ""))))
            throw new IOException("Missing block registry entry in structure baseline: " + tag);
        BlockState state = NbtUtils.readBlockState(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
        if (!NbtUtils.writeBlockState(state).equals(tag))
            throw new IOException("Unsupported block state in structure baseline: " + tag);
        return state;
    }
}
