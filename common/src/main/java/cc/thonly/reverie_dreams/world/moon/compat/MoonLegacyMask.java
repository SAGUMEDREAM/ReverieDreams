package cc.thonly.reverie_dreams.world.moon.compat;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Compact immutable snapshot of chunks which existed before the terrain update.
 */
public final class MoonLegacyMask {
    public static final String FILE = "data/reverie_dreams/moon_legacy_chunks.bin";
    private static final int MAGIC = 0x4d4f4f32;
    public static final MoonLegacyMask EMPTY = new MoonLegacyMask(Map.of());
    private final Map<Long, BitSet> regions;

    private MoonLegacyMask(Map<Long, BitSet> regions) {
        this.regions = Map.copyOf(regions);
    }

    private static long key(int x, int z) {
        return (x & 0xffffffffL) | ((long) z << 32);
    }

    public boolean contains(int chunkX, int chunkZ) {
        BitSet bits = regions.get(key(Math.floorDiv(chunkX, 32), Math.floorDiv(chunkZ, 32)));
        return bits != null && bits.get(Math.floorMod(chunkX, 32) + Math.floorMod(chunkZ, 32) * 32);
    }

    /**
     * 0 at old terrain, smooth ramp to 1 at 64 blocks from any old chunk footprint.
     */
    public double depressionFactor(int blockX, int blockZ) {
        int cx = Math.floorDiv(blockX, 16), cz = Math.floorDiv(blockZ, 16);
        double distance = 64;
        for (int x = cx - 4; x <= cx + 4; x++)
            for (int z = cz - 4; z <= cz + 4; z++) {
                if (!contains(x, z))
                    continue;
                double dx = Math.max(0, Math.max((long) x * 16 - blockX, blockX - ((long) x * 16 + 15)));
                double dz = Math.max(0, Math.max((long) z * 16 - blockZ, blockZ - ((long) z * 16 + 15)));
                distance = Math.min(distance, Math.hypot(dx, dz));
            }
        double t = distance / 64;
        return t * t * (3 - 2 * t);
    }

    public static MoonLegacyMask scan(Path regionFolder) throws IOException {
        Map<Long, BitSet> result = new HashMap<>();
        if (!Files.isDirectory(regionFolder))
            return EMPTY;
        Pattern pattern = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
        try (var files = Files.list(regionFolder)) {
            for (Path file : files.toList()) {
                Matcher match = pattern.matcher(file.getFileName().toString());
                if (!match.matches())
                    continue;
                BitSet bits = new BitSet(1024);
                long bytes = Files.size(file);
                if (bytes < 8192)
                    throw new IOException("Truncated Moon region header: " + file);
                if (bytes % 4096 != 0)
                    throw new IOException("Unaligned Moon region file; refusing preflight writes: " + file);
                BitSet usedSectors = new BitSet();
                usedSectors.set(0, 2);
                try (var in = new DataInputStream(Files.newInputStream(file))) {
                    for (int i = 0; i < 1024; i++) {
                        int offset = in.readInt();
                        if (offset == 0)
                            continue;
                        int start = offset >>> 8, length = offset & 255;
                        if (start < 2 || length == 0 || ((long) start + length) * 4096 > bytes
                                || usedSectors.nextSetBit(start) >= 0 && usedSectors.nextSetBit(start) < start + length)
                            throw new IOException("Invalid/overlapping Moon region allocation at slot " + i + ": " + file);
                        usedSectors.set(start, start + length);
                        bits.set(i);
                    }
                }
                if (!bits.isEmpty())
                    result.put(key(Integer.parseInt(match.group(1)), Integer.parseInt(match.group(2))), bits);
            }
        }
        return new MoonLegacyMask(result);
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (var out = new DataOutputStream(Files.newOutputStream(file))) {
            out.writeInt(MAGIC);
            out.writeInt(regions.size());
            for (long key : new TreeSet<>(regions.keySet())) {
                out.writeLong(key);
                long[] bits = regions.get(key).toLongArray();
                for (int i = 0; i < 16; i++)
                    out.writeLong(i < bits.length ? bits[i] : 0);
            }
        }
    }

    public static MoonLegacyMask load(Path worldRoot) throws IOException {
        Path file = worldRoot.resolve(FILE);
        if (!Files.exists(file))
            return EMPTY;
        Map<Long, BitSet> result = new HashMap<>();
        try (var in = new DataInputStream(Files.newInputStream(file))) {
            if (in.readInt() != MAGIC)
                throw new IOException("Invalid Moon legacy mask: " + file);
            int size = in.readInt();
            if (size < 0 || size > 4_000_000)
                throw new IOException("Invalid Moon mask region count");
            for (int i = 0; i < size; i++) {
                long key = in.readLong();
                long[] bits = new long[16];
                for (int j = 0; j < 16; j++)
                    bits[j] = in.readLong();
                result.put(key, BitSet.valueOf(bits));
            }
            if (in.read() != -1)
                throw new IOException("Trailing Moon legacy mask data");
        }
        return new MoonLegacyMask(result);
    }
}
