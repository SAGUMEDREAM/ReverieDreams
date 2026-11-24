package cc.thonly.reverie_dreams.world.gensokyo.storage;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GensokyoChunkCompression {
    private static final ConcurrentMap<Integer, RegionFileVersion> ZSTD_VERSIONS = new ConcurrentHashMap<>();

    private GensokyoChunkCompression() {
    }

    public static boolean isTerrainStorage(RegionStorageInfo info) {
        return info.dimension().equals(RDBuiltinLevels.getGensokyo()) && "chunk".equals(info.type());
    }

    public static RegionFileVersion zstdVersion(int level) {
        return ZSTD_VERSIONS.computeIfAbsent(Math.clamp(level, 1, 19), compressionLevel ->
                // Do not register this instance: id 127 must retain vanilla's custom-codec dispatch.
                // C2ME calls the region's version.wrap directly, bypassing RegionFile's writer.
                new RegionFileVersion(RegionFileVersion.VERSION_CUSTOM.getId(), null,
                        input -> ZstdChunkCodec.decodeCustom(input, unknown -> {
                            throw new IOException("Unsupported custom chunk codec for Zstd stream");
                        }), output -> ZstdChunkCodec.encode(output, compressionLevel)));
    }
}
