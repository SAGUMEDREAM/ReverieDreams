package cc.thonly.reverie_dreams.world.gensokyo.gen;

import com.google.common.collect.MapMaker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-chunk generation markers, persisted with the chunk rather than a later global save.
 */
public final class GensokyoStructureChunkState {
    private static final String TAG = "reverie_dreams:gensokyo_structures";
    private static final Map<ChunkAccess, Set<String>> CHUNKS = new MapMaker().weakKeys().makeMap();
    private static final Map<SerializableChunkData, Set<String>> SERIALIZED = new MapMaker().weakKeys().makeMap();

    private GensokyoStructureChunkState() {
    }

    private static ChunkAccess unwrap(ChunkAccess c) {
        return c instanceof ImposterProtoChunk p ? p.getWrapped() : c;
    }

    public static Set<String> markers(ChunkAccess chunk) {
        return CHUNKS.computeIfAbsent(unwrap(chunk), k -> ConcurrentHashMap.newKeySet());
    }

    public static boolean has(ChunkAccess c, String key) {
        Set<String> s = CHUNKS.get(unwrap(c));
        return s != null && s.contains(key);
    }

    public static void mark(ChunkAccess c, String key) {
        if (markers(c).add(key))
            c.markUnsaved();
    }

    public static void copy(ChunkAccess from, ChunkAccess to) {
        Set<String> s = CHUNKS.get(unwrap(from));
        if (s != null && !s.isEmpty())
            markers(to).addAll(s);
    }

    public static void parsed(SerializableChunkData data, CompoundTag tag) {
        if (data == null)
            return;
        Set<String> s = ConcurrentHashMap.newKeySet();
        for (var value : tag.getListOrEmpty(TAG))
            if (value instanceof StringTag text)
                s.add(text.value());
        if (!s.isEmpty())
            SERIALIZED.put(data, s);
    }

    public static void read(SerializableChunkData data, ChunkAccess chunk) {
        Set<String> s = SERIALIZED.get(data);
        if (s != null)
            markers(chunk).addAll(s);
    }

    public static void captured(SerializableChunkData data, ChunkAccess chunk) {
        Set<String> s = CHUNKS.get(unwrap(chunk));
        if (s != null && !s.isEmpty())
            SERIALIZED.put(data, Set.copyOf(s));
    }

    public static void write(SerializableChunkData data, CompoundTag tag) {
        Set<String> s = SERIALIZED.get(data);
        if (s != null && !s.isEmpty()) {
            ListTag list = new ListTag();
            s.stream().sorted().forEach(v -> list.add(StringTag.valueOf(v)));
            tag.put(TAG, list);
        }
    }
}
