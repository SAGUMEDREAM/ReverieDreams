package cc.thonly.reverie_dreams.world.gensokyo.road;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.Arrays;

/**
 * Immutable sparse road columns, carried inside the ordinary map checkpoint.
 */
public final class GensokyoRoadNetwork {
    public static final GensokyoRoadNetwork EMPTY = new GensokyoRoadNetwork(Map.of(), 0);
    private final Map<Long, Columns> chunks;
    private final int size;

    private GensokyoRoadNetwork(Map<Long, Columns> chunks, int size) {
        this.chunks = Map.copyOf(chunks);
        this.size = size;
    }

    public static GensokyoRoadNetwork decode(JsonObject object, int width, int depth, int originX, int originZ, int minY, int worldHeight) {
        if (object == null)
            return EMPTY;
        if (object.get("version").getAsInt() != 1 || !object.get("encoding").getAsString().equals("gzip-base64-rdr1"))
            throw new IllegalArgumentException("Unsupported road network encoding");
        String text = object.get("data").getAsString();
        if (text.length() > 24_000_000)
            throw new IllegalArgumentException("Road network payload too large");
        try (var in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(text))))) {
            if (in.readInt() != 0x52445231 || in.readInt() != width || in.readInt() != depth
                    || in.readInt() != originX || in.readInt() != originZ)
                throw new IllegalArgumentException("Road network does not match map dimensions/origin");
            int count = in.readInt();
            if (count < 0 || count > 1_000_000 || count != object.get("columns").getAsInt())
                throw new IllegalArgumentException("Invalid road network column count");
            Map<Long, Columns> chunks = new HashMap<>();
            int previous = -1;
            for (int i = 0; i < count; i++) {
                int index = in.readInt();
                int pavement = in.readShort(), from = in.readShort(), to = in.readShort(), role = in.readUnsignedByte();
                if (index <= previous || index >= (long) width * depth || pavement < minY || pavement >= minY + worldHeight
                        || from <= pavement || to < from || to >= minY + worldHeight || role < 1 || role > 4)
                    throw new IllegalArgumentException("Invalid/duplicate road column at " + i);
                previous = index;
                int x = originX + index % width, z = originZ + index / width;
                var columns = chunks.computeIfAbsent(chunkKey(x, z), key -> new Columns());
                int local = (x & 15) | ((z & 15) << 4);
                columns.pavement[local] = (short) pavement;
                columns.from[local] = (short) from;
                columns.to[local] = (short) to;
            }
            if (in.read() != -1)
                throw new IllegalArgumentException("Trailing road data");
            return new GensokyoRoadNetwork(chunks, count);
        } catch (IOException e) {
            throw new IllegalArgumentException("Truncated/corrupt road network", e);
        }
    }

    private static long chunkKey(int x, int z) {
        return ((long) (x >> 4) & 0xffffffffL) | (((long) (z >> 4) & 0xffffffffL) << 32);
    }

    public int size() {
        return this.size;
    }

    public boolean contains(int x, int y, int z) {
        Columns c = this.chunks.get(chunkKey(x, z));
        if (c == null)
            return false;
        int i = (x & 15) | ((z & 15) << 4);
        return c.to[i] != Short.MIN_VALUE && y >= c.pavement[i] - 1 && y <= c.to[i];
    }

    public int pavementY(int x, int z) {
        Columns c = this.chunks.get(chunkKey(x, z));
        if (c == null)
            return Short.MIN_VALUE;
        int i = (x & 15) | ((z & 15) << 4);
        return c.to[i] == Short.MIN_VALUE ? Short.MIN_VALUE : c.pavement[i];
    }

    /**
     * Generation-only reconciliation; this never scans or edits loaded player chunks.
     */
    public void clearHeadroom(ChunkAccess chunk) {
        Columns c = this.chunks.get(chunk.getPos().pack());
        if (c == null)
            return;
        var pos = new BlockPos.MutableBlockPos();
        var floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        var surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int i = 0; i < 256; i++) {
            if (c.to[i] == Short.MIN_VALUE)
                continue;
            int x = i & 15, z = i >> 4;
            for (int y = c.from[i]; y <= c.to[i]; y++) {
                pos.set(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z);
                if (chunk.getBlockState(pos).isAir())
                    continue;
                var air = Blocks.AIR.defaultBlockState();
                chunk.setBlockState(pos, air, Block.UPDATE_NONE);
                floor.update(x, y, z, air);
                surface.update(x, y, z, air);
            }
        }
    }

    private static final class Columns {
        final short[] pavement = new short[256], from = new short[256], to = new short[256];

        Columns() {
            Arrays.fill(this.to, Short.MIN_VALUE);
        }
    }
}
